package com.snow.al.dd.core.distributed.lock;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class MongoTemplateDistributedLock implements DdLock {
    private final MongoTemplate mongoTemplate;
    private final ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);


    @PostConstruct
    public void initIndex() {
        try {
            IndexOperations indexOps = mongoTemplate.indexOps("distributed_locks");
            // 1. 确保 lockName 唯一索引
            indexOps.ensureIndex(
                    new Index().on("lockName", Sort.Direction.ASC)
                            .unique()
                            .named("lockName_unique_idx")
            );
            // 2. 创建 TTL 索引（expireAfterSeconds = 0）
            indexOps.ensureIndex(
                    new Index().on("expireAt", Sort.Direction.ASC)
                            .expire(0L)
                            .named("expireAt_ttl_idx")
            );
            log.info("分布式锁集合索引初始化完成: {}", "distributed_locks");
        } catch (Exception e) {
            log.error("分布式锁索引初始化失败", e);
        }
    }


    public void acquireLock(String lockName, String clientId, Duration lockDuration, Duration renewInterval) {
        long expire = System.currentTimeMillis() + lockDuration.toMillis();
        while (System.currentTimeMillis() < expire) {
            if (tryAcquireLock(lockName, clientId, lockDuration)) return;
        }
        throw new IllegalStateException("lock timeout");
    }


    public boolean tryAcquireLock(String lockName, String clientId, Duration lockDuration) {
        Instant now = Instant.now();
        Instant expireAt = now.plus(lockDuration);

        DistributedLock lock = new DistributedLock();
        lock.setLockName(lockName);
        lock.setClientId(clientId);
        lock.setAcquiredAt(now);
        lock.setExpireAt(expireAt);

        try {
            // 尝试创建新锁（如果锁不存在）
            mongoTemplate.insert(lock);
            startLockRenewal(lockName, clientId, lockDuration);
            return true;
        } catch (DuplicateKeyException e) {
            // 锁已存在
            return handleExistingLock(lockName, clientId, lockDuration, now, expireAt);
        }
    }

    private boolean handleExistingLock(String lockName, String clientId, Duration lockDuration,
                                       Instant now, Instant expireAt) {
        // 检查现有锁是否过期
        Query query = new Query(Criteria.where("lockName").is(lockName)
                .and("expireAt").lt(now));

        Update update = new Update()
                .set("clientId", clientId)
                .set("acquiredAt", now)
                .set("expireAt", expireAt)
                .set("lockCount", 1);

        // 尝试获取过期的锁
        DistributedLock updated = mongoTemplate.findAndModify(
                query, update, DistributedLock.class);

        if (updated != null) {
            startLockRenewal(lockName, clientId, lockDuration);
            return true;
        }

        // 检查是否可重入（当前客户端已经持有锁）
        query = new Query(Criteria.where("lockName").is(lockName)
                .and("clientId").is(clientId));

        update = new Update()
                .set("expireAt", expireAt)
                .inc("lockCount", 1);

        updated = mongoTemplate.findAndModify(query, update, DistributedLock.class);

        return updated != null;
    }

    private void startLockRenewal(String lockName, String clientId, Duration lockDuration) {
        // 每 lockDuration/3 时间续期一次
        long interval = lockDuration.toMillis() / 3;

        Runnable renewTask = () -> {
            try {
                Query query = new Query(Criteria.where("lockName").is(lockName)
                        .and("clientId").is(clientId));

                Update update = new Update().set("expireAt", Instant.now().plus(lockDuration));

                mongoTemplate.updateFirst(query, update, DistributedLock.class);

            } catch (Exception e) {
                log.error("锁续期失败: {}", lockName, e);
            }
        };

        scheduler.scheduleAtFixedRate(renewTask, interval, interval, TimeUnit.MILLISECONDS);
    }

    public void releaseLock(String lockName, String clientId) {
        Query query = new Query(Criteria.where("lockName").is(lockName)
                .and("clientId").is(clientId));

        DistributedLock lock = mongoTemplate.findOne(query, DistributedLock.class);

        if (lock == null) return;

        if (lock.getLockCount() > 1) {
            // 减少可重入计数
            Update update = new Update().inc("lockCount", -1);
            mongoTemplate.updateFirst(query, update, DistributedLock.class);
        } else {
            // 完全释放锁
            mongoTemplate.remove(query, DistributedLock.class);
        }
    }


    @Override
    public boolean tryLock(String key, Duration waitInterval) {
        //TODO
        return false;
    }

    @Override
    public void unlock(String key) {
        //TODO
    }

    @Override
    public void tryLock(String key, Duration waitInterval, Runnable runnable) {
        //TODO
    }
}
