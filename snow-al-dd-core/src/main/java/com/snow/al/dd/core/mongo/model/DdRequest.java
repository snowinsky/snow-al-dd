package com.snow.al.dd.core.mongo.model;

import com.snow.al.dd.core.batch.pack.BatchDdRequestExtractor;
import com.snow.al.dd.core.batch.pack.batchtag.BatchTag;
import com.snow.al.dd.core.mongo.db.DdMsg;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Data
@NoArgsConstructor
public class DdRequest implements Serializable {

    private String ddMsgId;
    private String ddMsgBody;
    private boolean passEligibleCheck;
    private String eligibleCheckError;
    private String ddBatchTag;
    private Long ddMsgAmount;
    private String vendorCode;
    private String bankCode;
    private String vendorFeedbackMode;

    public DdRequest(String ddMsgBody) {
        this.ddMsgBody = ddMsgBody;
    }

    public void parseDdMsgBody(BatchDdRequestExtractor batchDdRequestExtractor) {
        var checkResult = batchDdRequestExtractor.eligibleCheck(this);
        var ss = batchDdRequestExtractor.extract(this);
        this.ddMsgId = ss.getDdMsgId();
        this.passEligibleCheck = checkResult.getFirst();
        this.eligibleCheckError = checkResult.getSecond();
        this.ddBatchTag = BatchTag.md5(ss.getPackBatchTag());
        this.ddMsgAmount = ss.getDdMsgAmount();
        this.vendorCode = ss.getVendorCode();
        this.bankCode = ss.getBankCode();
        this.vendorFeedbackMode = ss.getVendorFeedbackMode();
    }

    public DdMsg newDdMsg() {
        var ddMsg = new DdMsg();
        ddMsg.setId(null);
        ddMsg.setStatus(passEligibleCheck ? "waitToPack" : "eligibleCheckError");
        ddMsg.setCreatedAt(Date.from(Instant.now()));
        ddMsg.setExpiredAt(Date.from(Instant.now().plus(Duration.ofDays(1))));
        ddMsg.setErrorMsg(this.eligibleCheckError);

        ddMsg.setDdMsgId(ddMsgId);
        ddMsg.setDdMsgBody(ddMsgBody);
        ddMsg.setDdMsgAmount(ddMsgAmount);
        ddMsg.setBatchTag(ddBatchTag);
        return ddMsg;
    }

}
