package com.snow.al.dd.core.batch.exec.state;


import com.snow.al.dd.core.batch.exec.timeout.DdBatchExecTimeoutCenter;
import com.snow.al.dd.core.batch.exec.vendor.VendorExecuteAdapter;
import com.snow.al.dd.core.distributed.lock.DdLock;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.MongoTemplate;


@RequiredArgsConstructor
@Getter
public class DdBatchExecuteContext {
    private final String batchId;
    private final MongoTemplate mongoTemplate;
    private final DdLock distributedLock;
    private final VendorExecuteAdapter vendorExecuteAdapter;
    private final DdBatchExecTimeoutCenter ddBatchExecTimeoutCenter;
    @Setter
    private DdBatchExecuteState state;

    public void perform() {
        while (state != null) {
            state.doExecute(this);
        }
    }
}
