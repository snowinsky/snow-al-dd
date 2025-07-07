package com.snow.al.dd.core.batch.exec.state;

import com.snow.al.dd.core.mongo.db.DdMsgBatch;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

public interface DdBatchExecuteState {

    DdBatchExecuteState NULL_STATE = context -> context.setState(null);

    void doExecute(DdBatchExecuteContext context);

    default void doExecuteCore(DdBatchExecuteContext context,
                               Logger log,
                               Consumer<DdMsgBatch> consumer) {
        String batchId = context.getBatchId();
        context.getDistributedLock().tryLock(batchId, Duration.ofSeconds(30), () -> {
            DdMsgBatch batch = context.getMongoTemplate().findById(batchId, DdMsgBatch.class);
            log.info("batchId:{} 开始执行批次操作，当前状态:{}", batchId, Optional.ofNullable(batch).map(DdMsgBatch::getStatus).orElse(null));
            consumer.accept(batch);
        });
    }
}
