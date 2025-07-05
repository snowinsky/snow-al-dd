package com.snow.al.dd.core.distributed.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class RedissonLock implements DdLock {
    private final RedissonClient redissonClient;

    @Override
    public boolean tryLock(String key, Duration waitTime) {
        RLock lock = redissonClient.getLock(key);
        try {
            return lock.tryLock(waitTime.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("redisson lock 线程被中断", e);
            return false;
        }
    }

    @Override
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(key);
        lock.unlock();
    }

    @Override
    public void tryLock(String key, Duration waitTime, Runnable runnable) {
        RLock lock = redissonClient.getLock(key);
        boolean isLock = false;
        try {
            isLock = lock.tryLock(waitTime.toMillis(), TimeUnit.MILLISECONDS);
            if (isLock) {
                runnable.run();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("redisson lock 线程被中断", e);
        } finally {
            if (isLock) {
                lock.unlock();
            }
        }
    }

}
