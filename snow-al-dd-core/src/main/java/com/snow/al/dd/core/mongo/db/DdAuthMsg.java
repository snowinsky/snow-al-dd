package com.snow.al.dd.core.mongo.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "dd_auth_msg")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DdAuthMsg {
    @Id
    private String id;
    @Version
    private String version;
    private Instant createdAt;
    private Instant updatedAt;
    private String sourceFrom;
    private String sourceSeqNo;
    @Indexed(unique = true, name = "idx_mch_trade_no")
    private String mchTradeNo;
    private String vendorTradeNo;
    private String vendorUniqueCode;
    private String vendorAuthCode;
    private Instant validFrom;
    private Instant validTo;
    private String clientId;
    private String vendorGroup;
    private String vendorPreAuthLog;
    private String vendorQueryAuthLog;
    private String vendorAuthSignLog;
    private String vendorCallbackLog;
    private String status;
    private String errorCode;
    private String errorMsg;

}
