package com.snow.al.dd.core.mongo.model.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "dd_msg_batch_exec")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DdMsgBatchExec {
    @Id
    private String id;
    private Instant createdAt;
    private Instant expiredAt;
    private List<DdMsgBatch.DbMsgOfBatch> batchReqMsgList;
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

    private List<DdMsgBatchExec.DbResMsgOfBatch> batchResMsgList;

    private String status;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DbResMsgOfBatch {
        private String id;
        private String ddMsgId;
        private String ddMsgBody;
    }

}
