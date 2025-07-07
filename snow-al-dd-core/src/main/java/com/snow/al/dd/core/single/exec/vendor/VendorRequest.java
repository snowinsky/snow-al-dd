package com.snow.al.dd.core.single.exec.vendor;

import com.snow.al.dd.core.mongo.db.DdMsgSingle;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class VendorRequest<T extends VendorResponse> {

    protected final DdMsgSingle single;

    public abstract Class<T> getResponseClass();

}
