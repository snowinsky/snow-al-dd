package com.snow.al.dd.core.single.exec.state;

import com.snow.al.dd.core.mongo.model.db.DdMsgSingle;
import com.snow.al.dd.core.mongo.model.db.DdMsgSingleStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class ReadyToVendorQueryState  implements DdSingleExecuteState{
    @Override
    public void doExecute(DdSingleExecuteContext context) {
        doExecuteCore(context, log, single -> {
            if (single == null || !single.getStatus().equals(DdMsgSingleStatus.READY_TO_QUERY.getStatus())) {
                log.error("ddMsgId:{} 状态异常，当前状态为：{}", Optional.ofNullable(single).map(DdMsgSingle::getId).orElse(null), Optional.ofNullable(single).map(DdMsgSingle::getStatus).orElse(null));
                context.setState(null);
                return;
            }
            var vendorExecuteAdapter = context.getVendorSingleExecuteAdapter();
            var vendorQueryResponse = vendorExecuteAdapter.query(single);
            vendorQueryResponse.getQueryNextStep().accept(vendorQueryResponse, context);
        });
    }
}
