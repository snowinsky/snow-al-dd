package com.snow.al.dd.core.distributed.ratelimiter;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.time.Duration;

@RequiredArgsConstructor
public class RedissonRateLimiter implements DdRateLimiter {

    private final RedissonClient redissonClient;
    private final RRateLimiter rateLimiter;

    public RedissonRateLimiter(RedissonClient redissonClient, String key, long newTokenCountPerRateInterval, Duration rateInterval) {
        this.redissonClient = redissonClient;
        this.rateLimiter = redissonClient.getRateLimiter(key);
        this.rateLimiter.setRate(RateType.OVERALL, newTokenCountPerRateInterval, rateInterval.toMillis(), RateIntervalUnit.MILLISECONDS);
    }

    public boolean tryAcquire() {
        return rateLimiter.tryAcquire();
    }

    public void acquire() {
        rateLimiter.acquire();
    }


}
