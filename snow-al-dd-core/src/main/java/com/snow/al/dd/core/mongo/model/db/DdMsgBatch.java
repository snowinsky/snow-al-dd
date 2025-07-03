package com.snow.al.dd.core.mongo.model.db;

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
    private String batchFileName;
    private Integer currentBatchSize;
    private Long currentBatchAmount;
    private Integer maxBatchSize;
    private Long maxBatchAmount;
    private Instant createdAt;
    private Instant expiredAt;
    private List<DbMsgOfBatch> batchMsgs;
    private String status;



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DbMsgOfBatch {
        private String id;
        private String ddMsgId;
        private String ddMsgBody;

        public DbMsgOfBatch(DdMsg ddMsg) {
            this.id = ddMsg.getId();
            this.ddMsgId = ddMsg.getDdMsgId();
            this.ddMsgBody = ddMsg.getDdMsgBody();
        }
    }
}
