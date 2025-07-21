package com.snow.al.dd.core.batch.pack;

import com.snow.al.dd.core.mongo.model.DdRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class BatchDdRequestConsumer {

    private final MongoTemplate mongoTemplate;
    private final DdMsgBatchPackService ddMsgBatchPackService;
    private final BatchDdRequestExtractor batchDdRequestExtractor;


    public void consume(List<DdRequest> ddRequests) {
        log.info("start to consume the batch dd requests size {}", ddRequests.size());
        ThreadLocal<Long> ll = new ThreadLocal<>();
        ll.set(System.currentTimeMillis());
        ddRequests.stream().filter(ddRequest -> {
            ddRequest.parseDdMsgBody(batchDdRequestExtractor);
            return true;
        }).forEach(ddRequest -> {
            if (ddRequest.isPassEligibleCheck()) {
                ddMsgBatchPackService.pack(ddRequest);
            } else {
                mongoTemplate.insert(ddRequest.newDdMsg());
            }
        });
    }

}
