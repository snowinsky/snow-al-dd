package com.snow.al.dd.core.batch.exec;

import com.snow.al.dd.core.batch.exec.state.DdBatchExecuteContext;
import com.snow.al.dd.core.batch.exec.timeout.DdBatchExecTimeoutCenter;
import com.snow.al.dd.core.batch.exec.vendor.VendorExecuteAdapter;
import com.snow.al.dd.core.distributed.lock.DdLock;
import com.snow.al.dd.core.mongo.db.DdMsgBatch;
import com.snow.al.dd.core.mongo.db.DdMsgBatchStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Duration;

@RequiredArgsConstructor
@Slf4j
public class DdBatchExecutor {

    private final MongoTemplate mongoTemplate;
    private final DdLock ddLock;
    private final DdBatchExecTimeoutCenter ddBatchExecTimeoutCenter;
    private final VendorExecuteAdapter vendorExecuteAdapter;

    public void executeNormalStatus(String batchId) {
        DdMsgBatch ddMsgBatch = mongoTemplate.findOne(new Query(Criteria.where("id").is(batchId)), DdMsgBatch.class);
        if (ddMsgBatch == null) {
            return;
        }
        String batchStatus = ddMsgBatch.getStatus();
        DdBatchExecuteContext context = new DdBatchExecuteContext(
                batchId,
                mongoTemplate,
                ddLock,
                vendorExecuteAdapter,
                ddBatchExecTimeoutCenter);
        context.setState(DdMsgBatchStatus.getStateByStatus(batchStatus));
        context.perform();
    }

    public void executePendingStatus(String batchId) {
        ddLock.tryLock(batchId, Duration.ofSeconds(10), () -> {
            var send = mongoTemplate.updateMulti(new Query(Criteria.where("id").is(batchId).and("status").is(DdMsgBatchStatus.WAIT_TO_SEND.getStatus())),
                    new Update().set("status", DdMsgBatchStatus.READY_TO_VENDOR_SEND.getStatus()), DdMsgBatch.class);
            log.info("try to change batchId:{} from waitToSend to readyToVendorSend, result:{}", batchId, send.getModifiedCount());

            var query = mongoTemplate.updateMulti(new Query(Criteria.where("id").is(batchId)
                            .and("status").is(DdMsgBatchStatus.WAIT_TO_QUERY.getStatus())
                            .and("vendorFeedbackMode").is("QUERY")),
                    new Update().set("status", DdMsgBatchStatus.READY_TO_VENDOR_QUERY.getStatus()), DdMsgBatch.class);
            log.info("try to change batchId:{} from waitToQuery to readyToVendorQuery, result:{}", batchId, query.getModifiedCount());
        });
        executeNormalStatus(batchId);
    }

}
