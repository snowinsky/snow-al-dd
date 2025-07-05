package com.snow.al.dd.core.batch.exec.vendor;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public abstract class VendorResponse {
    private final String returnCodeGroup;
    private final String returnCodeGroupDesc;
    private String returnCode;
    private String returnMsg;
}
