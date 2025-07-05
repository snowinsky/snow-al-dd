package com.snow.al.dd.core.distributed.ratelimiter;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
public class RedissonRateLimiterTest {

    @Test
    public void tryAcquire() throws IOException, InterruptedException {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                RedissonClient client = Redisson.create(config);
                RedissonRateLimiter rateLimiter = new RedissonRateLimiter(client, "test", 1, Duration.ofSeconds(5));
                for (int j = 0; j < 10; j++) {
                    rateLimiter.acquire();
                    log.info("{}", LocalDateTime.now());
                }
            }).start();
        }
        Thread.sleep(1000000);
    }
}