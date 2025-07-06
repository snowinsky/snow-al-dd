package com.snow.al.dd.core.single.exec.timeout;

import com.snow.al.dd.core.single.exec.DdSingleExecutor;
import com.snow.al.timeoutcenter.DeadLetterHandleFactory;
import com.snow.al.timeoutcenter.HandleFactory;
import com.snow.al.timeoutcenter.TimeoutTask;
import com.snow.al.timeoutcenter.redis.jedis.sync.RedisJedisTimeoutCenterBootstrap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import redis.clients.jedis.JedisPool;

@RequiredArgsConstructor
public class DdSingleExecTimeoutCenter {
    private final JedisPool jedisPool;
    @Getter
    private final String bizTag = "singleDdExecTimeout";
    private final int slotCount = 8;
    @Setter
    private DdSingleExecutor ddSingleExecutor;
    private RedisJedisTimeoutCenterBootstrap timeoutCenterBootstrap;

    public void start() {
        DeadLetterHandleFactory deadLetterHandleFactory = timeoutTask -> {
            ddSingleExecutor.executePendingStatus(timeoutTask.getTaskFromId());
        };
        HandleFactory handleFactory = timeoutTask -> {
            ddSingleExecutor.executePendingStatus(timeoutTask.getTaskFromId());
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
