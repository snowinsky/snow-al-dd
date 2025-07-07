package com.snow.al.dd.core.mongo.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "dd_msg_batch")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DdMsgBatch {
    @Id
    private String id;
    private String batchTag;
    private String vendorCode;
    private String bankCode;
    private String vendorFeedbackMode;
    private Integer currentBatchSize;
    private Long currentBatchAmount;
    private Integer maxBatchSize;
    private Long maxBatchAmount;
    private Instant createdAt;
    private Instant expiredAt;
    private List<DbMsgOfBatch> batchReqMsgList;
    private String status;

    private List<DdMsgBatch.DbMsgOfBatch> batchResMsgList;
    private String batchName;
    private String mchTradeNo;
    private String vendorTradeNo;
    private String pathReqFile;
    private String pathResFile;
    private String sendResLog;
    private String queryResLog;
    private String callbackLog;
    private String returnCode;
    private String returnMsg;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DbMsgOfBatch {
        private String id;
        private String ddMsgId;
        private String ddMsgBody;
        private Long ddMsgAmount;


        public DbMsgOfBatch(DdMsg ddMsg) {
            this.id = ddMsg.getId();
            this.ddMsgId = ddMsg.getDdMsgId();
            this.ddMsgBody = ddMsg.getDdMsgBody();
            this.ddMsgAmount = ddMsg.getDdMsgAmount();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DbMsgResOfBatch {
        private String id;
        private String ddMsgId;
        private Long requestAmount;
        private Long successAmount;
        private String returnCode;
        private String returnMsg;

        public DbMsgResOfBatch(DbMsgOfBatch ddMsg) {
            this.id = ddMsg.getId();
            this.ddMsgId = ddMsg.getDdMsgId();
            this.requestAmount = ddMsg.getDdMsgAmount();
        }
    }
}
