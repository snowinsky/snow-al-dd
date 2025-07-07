package com.snow.al.dd.core.batch.exec.vendor;

import com.snow.al.dd.core.mongo.db.DdMsgBatch;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class VendorRequest<T extends VendorResponse> {

    protected final DdMsgBatch batch;

    public abstract Class<T> getResponseClass();

}
