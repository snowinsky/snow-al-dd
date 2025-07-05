package com.snow.al.dd.core.batch.exec;

import com.snow.al.dd.core.batch.exec.timeout.DdBatchExecTimeoutCenter;
import com.snow.al.dd.core.batch.exec.vendor.VendorExecuteAdapter;
import com.snow.al.dd.core.distributed.lock.RedissonLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import redis.clients.jedis.JedisPool;

public class DdBatchExecutorBuilder {
    private MongoTemplate mongoTemplate;
    private JedisPool jedisPool;
    private RedissonClient redissonClient;
    private VendorExecuteAdapter vendorExecuteAdapter;

    public DdBatchExecutorBuilder mongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        return this;
    }

    public DdBatchExecutorBuilder jedisPool(JedisPool jedisPool, RedissonClient redissonClient) {
        this.jedisPool = jedisPool;
        this.redissonClient = redissonClient;
        return this;
    }

    public DdBatchExecutorBuilder vendorExecuteAdapter(VendorExecuteAdapter vendorExecuteAdapter) {
        this.vendorExecuteAdapter = vendorExecuteAdapter;
        return this;
    }


    public DdBatchExecutor build() {
        DdBatchExecTimeoutCenter ddBatchExecTimeoutCenter = new DdBatchExecTimeoutCenter(jedisPool);
        var dd = new DdBatchExecutor(mongoTemplate, new RedissonLock(redissonClient), ddBatchExecTimeoutCenter, vendorExecuteAdapter);
        ddBatchExecTimeoutCenter.setDdBatchExecutor(dd);
        ddBatchExecTimeoutCenter.start();
        return dd;
    }
}
