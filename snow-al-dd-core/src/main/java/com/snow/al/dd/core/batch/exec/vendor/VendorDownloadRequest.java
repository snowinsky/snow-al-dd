package com.snow.al.dd.core.batch.exec.vendor;

import com.snow.al.dd.core.mongo.model.db.DdMsgBatch;

public class VendorDownloadRequest extends VendorRequest<VendorDownloadResponse> {
    public VendorDownloadRequest(DdMsgBatch batch) {
        super(batch);
    }

    @Override
    public Class<VendorDownloadResponse> getResponseClass() {
        return VendorDownloadResponse.class;
    }
}
