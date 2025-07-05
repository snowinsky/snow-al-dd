package com.snow.al.dd.core.batch.pack;

import lombok.Data;

@Data
public class DdRequestExtractResult {
    private Integer packMaxBatchSize;
    private Long packMaxBatchAmount;
    private Long packMaxTimeoutSecond;

    private String packBatchTag;
    private String vendorCode;
    private String bankCode;
    private String vendorFeedbackMode;
    private String ddMsgId;
    private Long ddMsgAmount;
}
