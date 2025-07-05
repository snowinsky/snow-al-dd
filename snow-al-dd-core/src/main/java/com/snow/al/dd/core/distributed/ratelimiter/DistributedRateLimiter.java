package com.snow.al.dd.core.distributed.ratelimiter;

import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@RequiredArgsConstructor
public class DistributedRateLimiter {

    private final MongoTemplate mongoTemplate;
    private final LeakyBucketLimiter leakyBucket;
    private final TokenBucketLimiter tokenBucket;

    public DistributedRateLimiter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.leakyBucket = new LeakyBucketLimiter(mongoTemplate);
        this.tokenBucket = new TokenBucketLimiter(mongoTemplate);
    }


    /**
     * 漏桶限流
     *
     * @param key
     * @param rate     微妙数，多少微妙漏一个
     * @param capacity 容量
     * @return
     */
    public boolean tryAcquireLeakyBucket(String key, long rate, long capacity) {
        return leakyBucket.tryAcquire(key, capacity, rate);
    }

    /**
     * 令牌桶限流
     *
     * @param key
     * @param rate            微妙数，多少微妙补充一个
     * @param capacity        容量
     * @param tokensRequested 请求的令牌数
     * @return
     */
    public boolean tryAcquireTokenBucket(String key, long rate, long capacity, long tokensRequested) {
        return tokenBucket.tryAcquire(key, capacity, rate, tokensRequested);
    }


    @RequiredArgsConstructor
    public static class LeakyBucketLimiter {
        private final MongoTemplate mongoTemplate;

        /**
         * 比如 leakRateMicrosPerRequest = 1000000， 则每秒漏一个
         *
         * @param key
         * @param capacity
         * @param leakRateMicrosPerRequest
         * @return
         */
        public boolean tryAcquire(String key, long capacity, long leakRateMicrosPerRequest) {
            final long currentTime = System.currentTimeMillis() * 1000; // 微秒时间戳

            // 尝试获取或创建桶
            Query query = new Query(Criteria.where("_id").is(key));
            LeakyBucket bucket = mongoTemplate.findOne(query, LeakyBucket.class);

            if (bucket == null) {
                bucket = new LeakyBucket(key, capacity, capacity, currentTime, leakRateMicrosPerRequest);
                try {
                    mongoTemplate.save(bucket);
                } catch (Exception e) {
                    // 并发创建时可能被其他线程创建，重试获取
                    bucket = mongoTemplate.findOne(query, LeakyBucket.class);
                    if (bucket == null) {
                        bucket = new LeakyBucket(key, capacity, capacity, currentTime, leakRateMicrosPerRequest);
                    }
                }
            }

            // 计算漏水
            long timePassed = currentTime - bucket.getLastLeakTime();
            long leaked = timePassed / leakRateMicrosPerRequest;
            long newRemaining = Math.min(capacity, bucket.getRemaining() + leaked);
            long newLeakTime = currentTime - (timePassed % leakRateMicrosPerRequest);

            // 尝试消耗
            if (newRemaining > 0) {
                // 原子操作：减少剩余空间并更新时间
                Query updateQuery = Query.query(Criteria.where("_id").is(key)
                        .and("lastLeakTime").is(bucket.getLastLeakTime())); // 乐观锁

                Update update = new Update()
                        .set("remaining", newRemaining - 1)
                        .set("lastLeakTime", newLeakTime);

                UpdateResult result = mongoTemplate.updateFirst(updateQuery, update, LeakyBucket.class);
                return result.getModifiedCount() > 0;
            }

            // 桶已满，更新漏水时间但不消耗
            Update update = new Update().set("lastLeakTime", newLeakTime);
            mongoTemplate.updateFirst(query, update, LeakyBucket.class);
            return false;
        }
    }

    @RequiredArgsConstructor
    public static class TokenBucketLimiter {
        private final MongoTemplate mongoTemplate;

        /**
         * 比如 refillRateMicrosPerToken = 1000000， tokensRequested = 1， 则每秒补充一个令牌
         *
         * @param key
         * @param capacity
         * @param refillRateMicrosPerToken
         * @param tokensRequested
         * @return
         */
        public boolean tryAcquire(String key, long capacity, long refillRateMicrosPerToken, long tokensRequested) {
            final long currentTime = System.currentTimeMillis() * 1000; // 微秒时间戳

            // 尝试获取或创建桶
            Query query = new Query(Criteria.where("_id").is(key));
            TokenBucket bucket = mongoTemplate.findOne(query, TokenBucket.class);

            if (bucket == null) {
                bucket = new TokenBucket(key, capacity, capacity, currentTime, refillRateMicrosPerToken);
                try {
                    mongoTemplate.save(bucket);
                } catch (Exception e) {
                    // 并发创建时可能被其他线程创建，重试获取
                    bucket = mongoTemplate.findOne(query, TokenBucket.class);
                    if (bucket == null) {
                        bucket = new TokenBucket(key, capacity, capacity, currentTime, refillRateMicrosPerToken);
                    }
                }
            }

            // 计算令牌补充
            long timePassed = currentTime - bucket.getLastRefillTime();
            long tokensToAdd = timePassed / refillRateMicrosPerToken;
            long newTokens = Math.min(capacity, bucket.getTokens() + tokensToAdd);
            long newRefillTime = currentTime - (timePassed % refillRateMicrosPerToken);

            // 尝试消耗令牌
            if (newTokens >= tokensRequested) {
                // 原子操作：减少令牌数并更新时间
                Query updateQuery = Query.query(Criteria.where("_id").is(key)
                        .and("lastRefillTime").is(bucket.getLastRefillTime())); // 乐观锁

                Update update = new Update()
                        .set("tokens", newTokens - tokensRequested)
                        .set("lastRefillTime", newRefillTime);

                UpdateResult result = mongoTemplate.updateFirst(updateQuery, update, TokenBucket.class);
                return result.getModifiedCount() > 0;
            }

            // 令牌不足，更新补充时间但不消耗
            Update update = new Update().set("lastRefillTime", newRefillTime);
            mongoTemplate.updateFirst(query, update, TokenBucket.class);
            return false;
        }

    }
}
