package com.snow.al.dd.core.batch.exec.state;

import com.snow.al.dd.core.batch.exec.vendor.VendorDownloadRequest;
import com.snow.al.dd.core.batch.exec.vendor.VendorDownloadResponse;
import com.snow.al.dd.core.mongo.db.DdMsgBatch;
import com.snow.al.dd.core.mongo.db.DdMsgBatchStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ReadyToVendorDownloadState implements DdBatchExecuteState {

    @Override
    public void doExecute(DdBatchExecuteContext context) {
        doExecuteCore(context, log, batch -> {
            if (batch == null || !batch.getStatus().equals(DdMsgBatchStatus.READY_TO_VENDOR_DOWNLOAD.getStatus())) {
                log.error("batchId:{} 状态异常，当前状态为：{}", Optional.ofNullable(batch).map(DdMsgBatch::getId).orElse(null), Optional.ofNullable(batch).map(DdMsgBatch::getStatus).orElse(null));
                context.setState(null);
                return;
            }
            VendorDownloadResponse vendorDownloadResponse = context.getVendorExecuteAdapter().download(new VendorDownloadRequest(batch));
            vendorDownloadResponse.getDownloadNextStep().accept(vendorDownloadResponse, context);
        });
    }
}
