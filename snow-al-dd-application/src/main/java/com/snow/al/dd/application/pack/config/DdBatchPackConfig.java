package com.snow.al.dd.application.pack.config;

import com.snow.al.dd.core.batch.pack.BatchDdPackInterface;
import com.snow.al.dd.core.batch.pack.BatchDdRequestConsumer;
import com.snow.al.dd.core.batch.pack.BatchDdRequestConsumerBuilder;
import com.snow.al.dd.core.distributedratelimiter.DistributedRateLimiter;
import com.snow.al.dd.core.mongo.model.DdRequest;
import com.snow.al.dd.core.mongo.model.db.DdMsg;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.util.Pair;

import java.time.LocalTime;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
public class DdBatchPackConfig {

    @Bean
    public BatchDdRequestConsumer batchDdRequestConsumer(MongoTemplate mongoTemplate) {
        return new BatchDdRequestConsumerBuilder()
                .mongoTemplate(mongoTemplate)
                .batchDdPackInterface(new BatchDdPackInterface() {
                    @Override
                    public Integer getMaxBatchSize(DdMsg ddMsg) {
                        return 10;
                    }

                    @Override
                    public Long getMaxBatchAmount(DdMsg ddMsg) {
                        return 9999999999999999L;
                    }

                    @Override
                    public Long getExpiredSeconds(DdMsg ddMsg) {
                        return 3600L;
                    }

                    @Override
                    public Pair<Boolean, String> eligibleCheck(DdRequest ddRequest) {
                        return Pair.of(true, "");
                    }

                    @Override
                    public String batchTag(DdRequest ddRequest) {
                        return "ABBBCC";
                    }

                    @Override
                    public String ddMsgId(DdRequest ddRequest) {
                        return System.nanoTime() + "" + ThreadLocalRandom.current().nextLong(99999L);
                    }

                    @Override
                    public Long ddMsgAmount(DdRequest ddRequest) {
                        return (long) LocalTime.now().getNano() / 1000;
                    }

                }).build();

    }


    @Bean
    public DistributedRateLimiter distributedRateLimiter(MongoTemplate mongoTemplate) {
        return new DistributedRateLimiter(mongoTemplate);
    }


}
