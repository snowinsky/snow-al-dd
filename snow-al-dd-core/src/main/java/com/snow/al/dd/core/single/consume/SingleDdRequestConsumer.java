package com.snow.al.dd.core.single.consume;

import com.snow.al.dd.core.mongo.db.DdMsgSingle;
import com.snow.al.dd.core.mongo.model.DdRequest;
import com.snow.al.dd.core.single.exec.DdSingleExecutor;
import com.snow.al.dd.core.single.exec.vendor.VendorSingleExecuteAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class SingleDdRequestConsumer {

    private final MongoTemplate mongoTemplate;
    private final DdSingleExecutor ddSingleExecutor;
    private final VendorSingleExecuteAdapter vendorSingleExecuteAdapter;


    public void consume(List<DdRequest> ddRequests) {
        log.info("start to consume the single dd request {}", ddRequests.size());
        ddRequests.stream().filter(ddRequest -> {
            ddRequest.parseDdMsgBody(vendorSingleExecuteAdapter);
            return true;
        }).forEach(ddRequest -> {
            if (ddRequest.isPassEligibleCheck()) {
                DdMsgSingle ddMsgSingle = mongoTemplate.insert(ddRequest.newDdMsgSingle());
                if (Optional.of(ddMsgSingle).map(DdMsgSingle::getId).isPresent()) {
                    ddSingleExecutor.executeNormalStatus(ddMsgSingle.getId());
                }
            } else {
                mongoTemplate.insert(ddRequest.newDdMsgSingle());
            }
        });
    }

}
