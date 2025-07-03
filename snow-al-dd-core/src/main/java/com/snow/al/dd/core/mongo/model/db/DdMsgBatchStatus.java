package com.snow.al.dd.core.mongo.model.db;

public enum DdMsgBatchStatus {

    PACKING("packing"),
    READY_TO_SEND("readyToSend"),
    PROCESSING("processing"),
    READY_TO_DOWNLOAD("readyToDownload"),
    READY_TO_LOAD("readyToLoad"),
    READY_TO_RESPONSE("readyToResponse");

    private final String status;

    DdMsgBatchStatus(String status) {
        this.status = status;
    }

}
