package com.snow.al.dd.application.pack.config;

import com.snow.al.dd.core.batch.pack.BatchDdRequestConsumer;
import com.snow.al.dd.core.batch.pack.BatchDdRequestConsumerBuilder;
import com.snow.al.dd.core.batch.pack.BatchDdRequestExtractor;
import com.snow.al.dd.core.batch.pack.DdRequestExtractResult;
import com.snow.al.dd.core.mongo.model.DdRequest;
import com.snow.al.dd.core.mongo.model.db.DdMsg;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.util.Pair;
import redis.clients.jedis.JedisPool;

import java.util.UUID;

@Configuration
public class DdBatchPackConfig {

    @Bean
    public BatchDdRequestConsumer batchDdRequestConsumer(MongoTemplate mongoTemplate) {
        JedisPool jedisPool = new JedisPool("localhost", 6379);
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        RedissonClient redissonClient = org.redisson.Redisson.create(config);
        return new BatchDdRequestConsumerBuilder()
                .mongoTemplate(mongoTemplate)
                .batchDdRequestExtractor(new BatchDdRequestExtractor() {
                    @Override
                    public DdRequestExtractResult extract(DdRequest ddRequest) {
                        var dd = new DdRequestExtractResult();
                        dd.setPackMaxBatchSize(10);
                        dd.setPackMaxBatchAmount(888888889999999L);
                        dd.setPackMaxTimeoutSecond(90L);
                        dd.setPackBatchTag("ASDASDASDDSA");
                        dd.setVendorCode("CCB(Direct Linkage)");
                        dd.setBankCode("CCB");
                        dd.setVendorFeedbackMode("QUERY");
                        dd.setDdMsgId(UUID.randomUUID().toString());
                        dd.setDdMsgAmount(10L);
                        return dd;
                    }

                    @Override
                    public DdRequestExtractResult extract(DdMsg ddMsg) {
                        var dd = new DdRequestExtractResult();
                        dd.setPackMaxBatchSize(10);
                        dd.setPackMaxBatchAmount(8888888899999999L);
                        dd.setPackMaxTimeoutSecond(90L);
                        dd.setPackBatchTag("ASDASDASDDSA");
                        dd.setVendorCode("CCB(Direct Linkage)");
                        dd.setBankCode("CCB");
                        dd.setVendorFeedbackMode("QUERY");
                        dd.setDdMsgId(UUID.randomUUID().toString());
                        dd.setDdMsgAmount(10L);
                        return dd;
                    }

                    @Override
                    public Pair<Boolean, String> eligibleCheck(DdRequest ddRequest) {
                        return Pair.of(true, "eligible");
                    }
                }).redisClient(jedisPool, redissonClient).build();

    }

}
