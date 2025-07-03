package com.snow.al.dd.core.batch.pack;

import com.snow.al.dd.core.batch.pack.batchtag.BatchTag;
import com.snow.al.dd.core.mongo.model.DdRequest;
import com.snow.al.dd.core.mongo.model.db.DdMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.util.Pair;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class BatchDdRequestConsumer {

    private final MongoTemplate mongoTemplate;
    private final DdMsgBatchPackFilter ddMsgBatchPackFilter;
    private final DdMsgBatchPackService ddMsgBatchPackService;


    public void consume(List<DdRequest> ddRequests) {
        log.info("start to consume the batch dd requests size {}", ddRequests.size());
        ddRequests.stream().filter(ddRequest -> {
            Pair<Boolean, String> eligibleCheck = ddMsgBatchPackFilter.eligibleCheck(ddRequest);
            ddRequest.setPassEligibleCheck(eligibleCheck.getFirst());
            ddRequest.setEligibleCheckError(eligibleCheck.getSecond());
            return true;
        }).filter(ddRequest -> {
            if (ddRequest.isPassEligibleCheck()) {
                String ddBatchTag = BatchTag.md5(ddMsgBatchPackFilter.batchTag(ddRequest));
                String ddMsgId = ddMsgBatchPackFilter.ddMsgId(ddRequest);
                Long ddMsgAmount = ddMsgBatchPackFilter.ddMsgAmount(ddRequest);
                ddRequest.setDdBatchTag(ddBatchTag);
                ddRequest.setDdMsgId(ddMsgId);
                ddRequest.setDdMsgAmount(ddMsgAmount);
            }
            return true;
        }).forEach(ddRequest -> {
            if (ddRequest.isPassEligibleCheck()) {
                ddMsgBatchPackService.pack(ddRequest);
            } else {
                mongoTemplate.insert(Optional.of(ddRequest).map(a -> DdMsg.builder()
                                .ddMsgId(a.getDdMsgId())
                                .createdAt(Instant.now())
                                .expiredAt(Instant.MAX)
                                .status("eligibleCheckError")
                                .errorMsg(a.getEligibleCheckError())
                                .ddMsgBody(a.getDdMsgBody())
                                .batchTag(a.getDdBatchTag())
                                .build())
                        .orElseThrow(() -> new RuntimeException("ddMsgId is null")));
            }
        });
    }

}
