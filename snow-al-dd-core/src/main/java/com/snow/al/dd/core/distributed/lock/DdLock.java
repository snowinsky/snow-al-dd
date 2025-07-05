package com.snow.al.dd.core.distributed.lock;

import java.time.Duration;

public interface DdLock {

    boolean tryLock(String key, Duration waitInterval);

    void unlock(String key);

    void tryLock(String key, Duration waitInterval, Runnable runnable);
}
