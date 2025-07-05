package com.snow.al.dd.core.distributed.ratelimiter;

public interface DdRateLimiter {
    boolean tryAcquire();

    void acquire();
}
