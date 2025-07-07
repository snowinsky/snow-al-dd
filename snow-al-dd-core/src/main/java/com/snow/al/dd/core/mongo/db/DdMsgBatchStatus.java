package com.snow.al.dd.core.mongo.db;

import com.snow.al.dd.core.batch.exec.state.*;
import lombok.Getter;

import static com.snow.al.dd.core.batch.exec.state.DdBatchExecuteState.NULL_STATE;

public enum DdMsgBatchStatus {

    PACKING("packing", NULL_STATE),
    READY_TO_REQFILE_GENERATE("readyToReqFileGenerate", new ReadyToReqFileGenerateState()),
    READY_TO_VENDOR_SEND("readyToVendorSend", new ReadyToVendorSendState()),
    WAIT_TO_SEND("waitToSend", NULL_STATE),
    READY_TO_VENDOR_QUERY("readyToVendorQuery", new ReadyToVendorQueryState()),
    READY_TO_VENDOR_CALLBACK("readyToVendorCallback", NULL_STATE),
    WAIT_TO_QUERY("waitToQuery", NULL_STATE),
    READY_TO_VENDOR_DOWNLOAD("readyToVendorDownload", new ReadyToVendorDownloadState()),
    READY_TO_RESFILE_PARSE("readyToResFileParse", new ReadyToResFileParseState()),
    READY_TO_RESFILE_NOTIFY("readyToResFileNotify", new ReadyToResFileNotifyState()),
    BATCH_NOTIFY_COMPLETED("batchNotifyCompleted", NULL_STATE);


    @Getter
    private final String status;
    @Getter
    private final DdBatchExecuteState state;

    DdMsgBatchStatus(String status, DdBatchExecuteState state) {
        this.status = status;
        this.state = state;
    }

    public static DdBatchExecuteState getStateByStatus(String status) {
        for (DdMsgBatchStatus value : DdMsgBatchStatus.values()) {
            if (value.getStatus().equals(status)) {
                return value.getState();
            }
        }
        return NULL_STATE;
    }

}
