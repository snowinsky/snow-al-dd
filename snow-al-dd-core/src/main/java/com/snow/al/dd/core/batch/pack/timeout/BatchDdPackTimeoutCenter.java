package com.snow.al.dd.core.batch.pack.timeout;

import com.snow.al.dd.core.batch.pack.DdMsgBatchPackService;
import com.snow.al.timeoutcenter.DeadLetterHandleFactory;
import com.snow.al.timeoutcenter.HandleFactory;
import com.snow.al.timeoutcenter.SnowTimeoutCenter;
import com.snow.al.timeoutcenter.TimeoutTask;
import com.snow.al.timeoutcenter.redis.jedis.sync.RedisJedisTimeoutCenterBootstrap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPool;

@Slf4j
@RequiredArgsConstructor
public class BatchDdPackTimeoutCenter {
    private final JedisPool jedisPool;
    private final String bizTag = "batchDdPackTimeout";
    private final int potSize = 2;
    private final DdMsgBatchPackService ddMsgBatchPackService;

    private SnowTimeoutCenter packTimeoutCenter;

    public void start() {
        DeadLetterHandleFactory deadLetterHandleFactory = timeoutTask -> {
            log.info("batchDdPackTimeoutCenter handleTimeoutTask:{}", timeoutTask);
            ddMsgBatchPackService.closeBatch(timeoutTask.getTaskFromId());
        };

        HandleFactory handleFactory = timeoutTask -> {
            try {
                ddMsgBatchPackService.closeBatch(timeoutTask.getTaskFromId());
                return true;
            } catch (Exception e) {
                log.error("batchDdPackTimeoutCenter performTask error", e);
                return false;
            }
        };

        packTimeoutCenter = new RedisJedisTimeoutCenterBootstrap(jedisPool, bizTag, potSize, deadLetterHandleFactory, handleFactory);
        packTimeoutCenter.start();

    }

    public void publish(TimeoutTask timeoutTask) {
        if (packTimeoutCenter != null) {
            packTimeoutCenter.publish(timeoutTask);
        }
    }

}
