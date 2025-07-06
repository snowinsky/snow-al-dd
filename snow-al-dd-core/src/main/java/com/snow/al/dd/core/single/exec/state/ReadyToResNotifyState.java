package com.snow.al.dd.core.single.exec.state;

import com.snow.al.dd.core.mongo.model.db.DdMsgSingle;
import com.snow.al.dd.core.mongo.model.db.DdMsgSingleStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

@Slf4j
public class ReadyToResNotifyState implements DdSingleExecuteState {
    @Override
    public void doExecute(DdSingleExecuteContext context) {
        doExecuteCore(context, log, single -> {
            if (single == null || !single.getStatus().equals(DdMsgSingleStatus.READY_TO_NOTIFY.getStatus())) {
                log.error("ddMsgId:{} 状态异常，当前状态为：{}", Optional.ofNullable(single).map(DdMsgSingle::getId).orElse(null), Optional.ofNullable(single).map(DdMsgSingle::getStatus).orElse(null));
                context.setState(null);
                return;
            }
            context.getVendorSingleExecuteAdapter().notify(single);
            context.getMongoTemplate().findAndModify(
                    new Query(Criteria.where("id").is(single.getId()).and("status").is(DdMsgSingleStatus.READY_TO_NOTIFY.getStatus())),
                    new Update().set("status", DdMsgSingleStatus.NOTIFY_COMPLETED.getStatus()),
                    DdMsgSingle.class);
            context.setState(null);
        });
    }
}
