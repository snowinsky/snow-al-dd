package com.snow.al.dd.core.batch.exec.state;

import com.snow.al.dd.core.batch.exec.vendor.VendorExecuteAdapter;
import com.snow.al.dd.core.batch.exec.vendor.VendorQueryRequest;
import com.snow.al.dd.core.batch.exec.vendor.VendorQueryResponse;
import com.snow.al.dd.core.mongo.model.db.DdMsgBatch;
import com.snow.al.dd.core.mongo.model.db.DdMsgBatchStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ReadyToVendorQueryState implements DdBatchExecuteState {

    @Override
    public void doExecute(DdBatchExecuteContext context) {
        doExecuteCore(context, log, batch -> {
            if (batch == null || !batch.getStatus().equals(DdMsgBatchStatus.READY_TO_VENDOR_QUERY.getStatus())) {
                log.error("batchId:{} 状态异常，当前状态为：{}", Optional.ofNullable(batch).map(DdMsgBatch::getId).orElse(null), Optional.ofNullable(batch).map(DdMsgBatch::getStatus).orElse(null));
                context.setState(null);
                return;
            }
            VendorExecuteAdapter vendorExecuteAdapter = context.getVendorExecuteAdapter();
            VendorQueryResponse vendorQueryResponse = vendorExecuteAdapter.query(new VendorQueryRequest(batch));
            vendorQueryResponse.getQueryNextStep().accept(vendorQueryResponse, context);
        });
    }
}
