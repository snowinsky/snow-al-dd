package com.snow.al.dd.core.batch.exec.timeout;

import com.snow.al.dd.core.batch.exec.DdBatchExecutor;
import com.snow.al.timeoutcenter.DeadLetterHandleFactory;
import com.snow.al.timeoutcenter.HandleFactory;
import com.snow.al.timeoutcenter.TimeoutTask;
import com.snow.al.timeoutcenter.redis.jedis.sync.RedisJedisTimeoutCenterBootstrap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import redis.clients.jedis.JedisPool;

@RequiredArgsConstructor
public class DdBatchExecTimeoutCenter {
    private final JedisPool jedisPool;
    @Getter
    private final String bizTag = "batchDdExecTimeout";
    private final int slotCount = 10;
    @Setter
    private DdBatchExecutor ddBatchExecutor;
    private RedisJedisTimeoutCenterBootstrap timeoutCenterBootstrap;

    public void start() {
        DeadLetterHandleFactory deadLetterHandleFactory = timeoutTask -> {
            ddBatchExecutor.executePendingStatus(timeoutTask.getTaskFromId());
        };
        HandleFactory handleFactory = timeoutTask -> {
            ddBatchExecutor.executePendingStatus(timeoutTask.getTaskFromId());
            return true;
        };
        timeoutCenterBootstrap = new RedisJedisTimeoutCenterBootstrap(jedisPool, bizTag, slotCount, deadLetterHandleFactory, handleFactory);
        timeoutCenterBootstrap.start();
    }

    public void publish(TimeoutTask timeoutTask) {
        timeoutCenterBootstrap.publish(timeoutTask);
    }

    public void publish(String batchId, long timeoutSeconds) {
        TimeoutTask timeoutTask = new TimeoutTask();
        timeoutTask.setTaskFromId(batchId);
        timeoutTask.setTaskFrom(bizTag);
        timeoutTask.setTaskTimeout(timeoutSeconds);
        publish(timeoutTask);
    }
}
