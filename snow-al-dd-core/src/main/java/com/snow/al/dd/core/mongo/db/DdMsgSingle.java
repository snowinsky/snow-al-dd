package com.snow.al.dd.core.mongo.db;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document(collection = "dd_msg_single")
public class DdMsgSingle {
    @Id
    private String id;
    @Indexed(unique = true, name = "dd_msg_id_uk")
    private String ddMsgId;
    private String ddMsgBody;
    private String vendorCode;
    private String bankCode;
    private String mchTradeNo;
    private String vendorTradeNo;
    private String vendorFeedbackMode;
    private String errorMsg;
    private String status;
    private Long requestAmount;
    private Long successAmount;
    private Integer sendCount;
    private Integer queryCount;
    private String sendResLog;
    private String queryResLog;
    private String callbackLog;
    private String returnCode;
    private String returnMsg;
    private String returnCodeGroup;
    private String returnCodeGroupDesc;
    private Instant createAt;
    private Instant expireAt;

}
