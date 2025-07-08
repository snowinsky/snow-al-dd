package com.snow.al.dd.core.single.exec;

import lombok.Data;

@Data
public class DdRequestExtractResult {

    private String vendorCode;
    private String bankCode;
    private String vendorFeedbackMode;
    private String ddMsgId;
    private Long ddMsgAmount;
}
