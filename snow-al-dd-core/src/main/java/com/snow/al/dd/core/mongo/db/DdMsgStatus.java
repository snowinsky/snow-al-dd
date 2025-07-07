package com.snow.al.dd.core.mongo.db;

public enum DdMsgStatus {

    WAIT_TO_PACK("waitToPack"),
    PACKING("packing");

    private final String status;

    DdMsgStatus(String status) {
        this.status = status;
    }
}
