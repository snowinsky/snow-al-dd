package com.snow.al.dd.core.batch.pack;

import com.snow.al.dd.core.distributedlock.MongoTemplateDistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

@Slf4j
public class BatchDdRequestConsumerBuilder {

    private DdMsgBatchPackFilter ddMsgBatchPackFilter;
    private MongoTemplate mongoTemplate;
    private MongoTemplateDistributedLock distributedLock;
    private BatchDdPackConfig batchDdPackConfig;

    public BatchDdRequestConsumerBuilder batchDdPackInterface(BatchDdPackInterface batchDdPackInterface) {
        this.ddMsgBatchPackFilter = batchDdPackInterface;
        this.batchDdPackConfig = batchDdPackInterface;
        return this;
    }

    public BatchDdRequestConsumerBuilder mongoTemplate(MongoTemplate mongoTemplate) {
        this.distributedLock = new MongoTemplateDistributedLock(mongoTemplate);
        this.mongoTemplate = mongoTemplate;
        return this;
    }


    public BatchDdRequestConsumer build() {
        DdMsgBatchPackService ddMsgBatchPackService = new DdMsgBatchPackService(mongoTemplate, distributedLock, batchDdPackConfig);
        ddMsgBatchPackService.startRestoreUnpackedDdMsg();
        return new BatchDdRequestConsumer(mongoTemplate, ddMsgBatchPackFilter, ddMsgBatchPackService);
    }


}
