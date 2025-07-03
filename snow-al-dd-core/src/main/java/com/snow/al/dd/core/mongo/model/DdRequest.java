package com.snow.al.dd.core.mongo.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class DdRequest implements Serializable {

    public DdRequest(String ddMsgBody) {
        this.ddMsgBody = ddMsgBody;
    }

    private String ddMsgId;
    private String ddMsgBody;

    private boolean passEligibleCheck;
    private String eligibleCheckError;
    private String ddBatchTag;
    private Long ddMsgAmount;
}
