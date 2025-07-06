package com.snow.al.dd.core.single.exec.state;

import com.snow.al.dd.core.mongo.model.db.DdMsgSingle;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

public interface DdSingleExecuteState {

    DdSingleExecuteState NULL_STATE = context -> context.setState(null);

    void doExecute(DdSingleExecuteContext context);

    default void doExecuteCore(DdSingleExecuteContext context,
                               Logger log,
                               Consumer<DdMsgSingle> consumer) {
        String singleDdId = context.getDdMsgId();
        context.getDdLock().tryLock(singleDdId, Duration.ofSeconds(30), () -> {
            DdMsgSingle single = context.getMongoTemplate().findById(singleDdId, DdMsgSingle.class);
            log.info("singleDdId:{} 开始执行单笔操作，当前状态:{}", singleDdId, Optional.ofNullable(single).map(DdMsgSingle::getStatus).orElse(null));
            consumer.accept(single);
        });
    }
}
