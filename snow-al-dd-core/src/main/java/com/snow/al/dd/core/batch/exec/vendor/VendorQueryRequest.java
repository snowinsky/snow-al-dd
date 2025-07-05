package com.snow.al.dd.core.batch.exec.vendor;

import com.snow.al.dd.core.mongo.model.db.DdMsgBatch;

public class VendorQueryRequest extends VendorRequest<VendorQueryResponse> {
    public VendorQueryRequest(DdMsgBatch batch) {
        super(batch);
    }

    @Override
    public Class<VendorQueryResponse> getResponseClass() {
        return VendorQueryResponse.class;
    }
}
