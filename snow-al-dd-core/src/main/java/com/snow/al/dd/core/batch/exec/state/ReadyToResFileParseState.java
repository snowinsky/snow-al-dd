package com.snow.al.dd.core.batch.exec.state;

import com.snow.al.dd.core.mongo.db.DdMsgBatch;
import com.snow.al.dd.core.mongo.db.DdMsgBatchStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ReadyToResFileParseState implements DdBatchExecuteState {


    @Override
    public void doExecute(DdBatchExecuteContext context) {
        doExecuteCore(context, log, batch -> {
            if (batch == null || !batch.getStatus().equals(DdMsgBatchStatus.READY_TO_RESFILE_PARSE.getStatus())) {
                log.error("batchId:{} 状态异常，当前状态为：{}", Optional.ofNullable(batch).map(DdMsgBatch::getId).orElse(null), Optional.ofNullable(batch).map(DdMsgBatch::getStatus).orElse(null));
                context.setState(null);
                return;
            }
            context.getVendorExecuteAdapter().parseResFile(batch);
            context.getMongoTemplate().findAndModify(
                    new Query(Criteria.where("id").is(batch.getId()).and("status").is(DdMsgBatchStatus.READY_TO_RESFILE_PARSE.getStatus())),
                    new Update().set("status", DdMsgBatchStatus.READY_TO_RESFILE_NOTIFY.getStatus()),
                    DdMsgBatch.class);
            context.setState(DdMsgBatchStatus.READY_TO_RESFILE_NOTIFY.getState());
        });
    }
}
