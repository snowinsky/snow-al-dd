package com.snow.al.dd.core.batch.exec.vendor;

import com.snow.al.dd.core.mongo.model.db.DdMsgBatch;

public class VendorSendRequest extends VendorRequest<VendorSendResponse> {
    public VendorSendRequest(DdMsgBatch batch) {
        super(batch);
    }

    @Override
    public Class<VendorSendResponse> getResponseClass() {
        return VendorSendResponse.class;
    }
}
