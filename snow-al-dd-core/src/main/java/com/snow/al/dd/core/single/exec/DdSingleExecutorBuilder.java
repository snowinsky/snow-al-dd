package com.snow.al.dd.core.single.exec;

import com.snow.al.dd.core.distributed.lock.DdLock;
import com.snow.al.dd.core.distributed.lock.RedissonLock;
import com.snow.al.dd.core.single.exec.timeout.DdSingleExecTimeoutCenter;
import com.snow.al.dd.core.single.exec.vendor.VendorSingleExecuteAdapter;
import org.redisson.api.RedissonClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import redis.clients.jedis.JedisPool;

public class DdSingleExecutorBuilder {

    private JedisPool jedisPool;
    private RedissonClient redissonClient;
    private MongoTemplate mongoTemplate;
    private VendorSingleExecuteAdapter vendorSingleExecuteAdapter;

    public DdSingleExecutorBuilder redis(JedisPool jedisPool, RedissonClient redissonClient) {
        this.jedisPool = jedisPool;
        this.redissonClient = redissonClient;
        return this;
    }

    public DdSingleExecutorBuilder mongo(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        return this;
    }

    public DdSingleExecutorBuilder vendor(VendorSingleExecuteAdapter vendorSingleExecuteAdapter) {
        this.vendorSingleExecuteAdapter = vendorSingleExecuteAdapter;
        return this;
    }

    public DdSingleExecutor build() {
        DdLock ddLock = new RedissonLock(redissonClient);
        DdSingleExecTimeoutCenter ddSingleExecTimeoutCenter = new DdSingleExecTimeoutCenter(jedisPool);
        DdSingleExecutor ddSingleExecutor = new DdSingleExecutor(ddLock, mongoTemplate, vendorSingleExecuteAdapter, ddSingleExecTimeoutCenter);
        ddSingleExecTimeoutCenter.setDdSingleExecutor(ddSingleExecutor);
        ddSingleExecTimeoutCenter.start();
        return ddSingleExecutor;
    }


}
