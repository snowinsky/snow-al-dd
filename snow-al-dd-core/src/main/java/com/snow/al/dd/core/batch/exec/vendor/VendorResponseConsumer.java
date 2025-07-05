package com.snow.al.dd.core.batch.exec.vendor;

import com.snow.al.dd.core.batch.exec.state.DdBatchExecuteContext;

public interface VendorResponseConsumer<T extends VendorResponse> {
    void accept(T response, DdBatchExecuteContext context);
}
