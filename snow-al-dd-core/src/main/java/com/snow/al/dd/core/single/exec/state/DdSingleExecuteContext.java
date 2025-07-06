package com.snow.al.dd.core.single.exec.state;

import com.snow.al.dd.core.distributed.lock.DdLock;
import com.snow.al.dd.core.single.exec.timeout.DdSingleExecTimeoutCenter;
import com.snow.al.dd.core.single.exec.vendor.VendorSingleExecuteAdapter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.MongoTemplate;

@RequiredArgsConstructor
@Getter
public class DdSingleExecuteContext {
    private final String ddMsgId;
    private final MongoTemplate mongoTemplate;
    private final DdLock ddLock;
    private final VendorSingleExecuteAdapter vendorSingleExecuteAdapter;
    private final DdSingleExecTimeoutCenter ddSingleExecTimeoutCenter;
    @Setter
    private DdSingleExecuteState state;

    public void perform() {
        while (state != null) {
            state.doExecute(this);
        }
    }

}
