package com.snow.al.dd.application.pack.controller;

import com.snow.al.dd.core.batch.pack.BatchDdRequestConsumer;
import com.snow.al.dd.core.distributedlock.MongoTemplateDistributedLock;
import com.snow.al.dd.core.distributedratelimiter.DistributedRateLimiter;
import com.snow.al.dd.core.mongo.model.DdRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestMongoController {

    private final MongoTemplate mongoTemplate;
    private final BatchDdRequestConsumer batchDdRequestConsumer;
    private final DistributedRateLimiter distributedRateLimiter;


    @GetMapping("/test")
    public void test() {
        for (int i = 0; i < 11; i++) {
            new Thread(() -> {
                List<DdRequest> a = new ArrayList<>();
                a.add(new DdRequest("abc" + System.currentTimeMillis()));
                batchDdRequestConsumer.consume(a);
            }).start();
        }
    }

    @GetMapping("/testLimit")
    public void testLimit(){
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                while(true) {
                    boolean b = distributedRateLimiter.tryAcquireTokenBucket("test", 10000000, 10, 1);
                    if(b) {
                        log.info("{}", b);
                    }
                }
            }).start();
        }
    }

    @GetMapping("/testLock")
    public void testDistributedLock() throws InterruptedException {
        new Thread(() -> {
            // 创建锁实例
            MongoTemplateDistributedLock lock = new MongoTemplateDistributedLock(mongoTemplate);
            // 尝试获取锁
            while (lock.tryAcquireLock("myLock", "clientId", Duration.ofSeconds(10))) {
                try {
                    log.info("aLock success. Performing business logic...");
                    Thread.sleep(11000); // 模拟业务耗时
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    // 释放锁
                    lock.releaseLock("myLock", "clientId");
                    log.info("aLock released.");
                }
            }
        }).start();

        new Thread(() -> {
            // 创建锁实例
            MongoTemplateDistributedLock lock = new MongoTemplateDistributedLock(mongoTemplate);
            // 尝试获取锁
            while (lock.tryAcquireLock("myLock", "clientId", Duration.ofSeconds(10))) {
                try {
                    log.info("bLock success. Performing business logic...");
                    Thread.sleep(13000); // 模拟业务耗时
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    // 释放锁
                    lock.releaseLock("myLock", "clientId");
                    log.info("bLock released.");
                }
            }
        }).start();

        Thread.sleep(30000);
    }

}