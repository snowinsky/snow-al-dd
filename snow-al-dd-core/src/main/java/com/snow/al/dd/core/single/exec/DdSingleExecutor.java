package com.snow.al.dd.core.single.exec;

import com.snow.al.dd.core.distributed.lock.DdLock;
import com.snow.al.dd.core.mongo.db.DdMsgSingle;
import com.snow.al.dd.core.mongo.db.DdMsgSingleStatus;
import com.snow.al.dd.core.single.exec.state.DdSingleExecuteContext;
import com.snow.al.dd.core.single.exec.timeout.DdSingleExecTimeoutCenter;
import com.snow.al.dd.core.single.exec.vendor.VendorSingleExecuteAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Duration;

@RequiredArgsConstructor
@Slf4j
public class DdSingleExecutor {

    private final DdLock ddLock;
    private final MongoTemplate mongoTemplate;
    private final VendorSingleExecuteAdapter vendorExecuteAdapter;
    private final DdSingleExecTimeoutCenter ddSingleExecTimeoutCenter;

    public void executeNormalStatus(String ddMsgSingleId) {
        DdMsgSingle ddMsgSingle = mongoTemplate.findOne(new Query(Criteria.where("id").is(ddMsgSingleId)), DdMsgSingle.class);
        if (ddMsgSingle == null) {
            return;
        }
        String singleStatus = ddMsgSingle.getStatus();
        DdSingleExecuteContext context = new DdSingleExecuteContext(
                ddMsgSingleId,
                mongoTemplate,
                ddLock,
                vendorExecuteAdapter,
                ddSingleExecTimeoutCenter);
        context.setState(DdMsgSingleStatus.getByCode(singleStatus));
        context.perform();
    }

    public void executePendingStatus(String ddMsgSingleId) {
        ddLock.tryLock(ddMsgSingleId, Duration.ofSeconds(10), () -> {
            var send = mongoTemplate.updateMulti(new Query(Criteria.where("id").is(ddMsgSingleId).and("status").is(DdMsgSingleStatus.PENDING_TO_SEND.getStatus())),
                    new Update().set("status", DdMsgSingleStatus.READY_TO_SEND.getStatus()), DdMsgSingle.class);
            log.info("try to change ddMsgId:{} from pendingToSend to readyToSend, result:{}", ddMsgSingleId, send.getModifiedCount());

            var query = mongoTemplate.updateMulti(new Query(Criteria.where("id").is(ddMsgSingleId)
                            .and("status").is(DdMsgSingleStatus.PENDING_TO_QUERY.getStatus())
                            .and("vendorFeedbackMode").is("QUERY")),
                    new Update().set("status", DdMsgSingleStatus.READY_TO_QUERY.getStatus()), DdMsgSingle.class);
            log.info("try to change ddMsgId:{} from pendingToQuery to readyToQuery, result:{}", ddMsgSingleId, query.getModifiedCount());
        });
        executeNormalStatus(ddMsgSingleId);
    }
}
