package com.snow.al.dd.core.single.exec.vendor;

import com.snow.al.dd.core.batch.exec.vendor.VendorResponse;
import com.snow.al.dd.core.single.exec.state.DdSingleExecuteContext;

public interface VendorResponseConsumer<T extends VendorResponse> {
    void accept(T response, DdSingleExecuteContext context);
}
