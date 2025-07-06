package com.snow.al.dd.core.mongo.model.db;

import com.snow.al.dd.core.single.exec.state.DdSingleExecuteState;
import com.snow.al.dd.core.single.exec.state.ReadyToResNotifyState;
import com.snow.al.dd.core.single.exec.state.ReadyToVendorQueryState;
import com.snow.al.dd.core.single.exec.state.ReadyToVendorSendState;
import lombok.Getter;

import static com.snow.al.dd.core.single.exec.state.DdSingleExecuteState.NULL_STATE;

public enum DdMsgSingleStatus {

    READY_TO_SEND("readyToSend", new ReadyToVendorSendState()),
    PENDING_TO_SEND("pendingToSend", NULL_STATE),
    READY_TO_QUERY("readyToQuery", new ReadyToVendorQueryState()),
    PENDING_TO_QUERY("pendingToQuery", NULL_STATE),
    READY_TO_NOTIFY("readyToNotify", new ReadyToResNotifyState()),
    NOTIFY_COMPLETED("notifyCompleted", NULL_STATE);

    @Getter
    private String status;
    @Getter
    private DdSingleExecuteState state;

    DdMsgSingleStatus(String code, DdSingleExecuteState state) {
        this.status = code;
        this.state = state;
    }

    public static DdSingleExecuteState getByCode(String code) {
        for (DdMsgSingleStatus status : values()) {
            if (status.getStatus().equals(code)) {
                return status.getState();
            }
        }
        return NULL_STATE;
    }
}
