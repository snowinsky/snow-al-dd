package com.snow.al.dd.core.single.exec.vendor;

import lombok.Data;

import java.time.Instant;

@Data
public class SingleSendResponse {

    private String ddMsgId;
    private String mchTradeNo;
    private String vendorTradeNo;
    private Long successAmount;
    private String sendResLog;
    private String returnCode;
    private String returnMsg;
    private String returnCodeGroup;
    private String returnCodeGroupDesc;
    private Instant expireTime;

}
