package com.snow.al.dd.core.batch.pack;

import com.snow.al.dd.core.batch.pack.timeout.BatchDdPackTimeoutCenter;
import com.snow.al.dd.core.distributed.lock.RedissonLock;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import redis.clients.jedis.JedisPool;

@Slf4j
public class BatchDdRequestConsumerBuilder {

    private MongoTemplate mongoTemplate;
    private JedisPool jedisPool;
    private RedissonClient redissonClient;
    private BatchDdRequestExtractor batchDdRequestExtractor;

    public BatchDdRequestConsumerBuilder batchDdRequestExtractor(BatchDdRequestExtractor batchDdRequestExtractor) {
        this.batchDdRequestExtractor = batchDdRequestExtractor;
        return this;
    }

    public BatchDdRequestConsumerBuilder mongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        return this;
    }

    public BatchDdRequestConsumerBuilder redisClient(JedisPool jedisPool, RedissonClient redissonClient) {
        this.jedisPool = jedisPool;
        this.redissonClient = redissonClient;
        return this;
    }


    public BatchDdRequestConsumer build() {
        var distributedLock = new RedissonLock(redissonClient);
        var ddMsgBatchPackService = new DdMsgBatchPackService(mongoTemplate, distributedLock, batchDdRequestExtractor);
        ddMsgBatchPackService.startRestoreUnpackedDdMsg();
        new BatchDdPackTimeoutCenter(jedisPool, ddMsgBatchPackService).start();
        return new BatchDdRequestConsumer(mongoTemplate, ddMsgBatchPackService, batchDdRequestExtractor);
    }


}
