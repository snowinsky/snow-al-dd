package com.snow.al.dd.core.mongo.model.db;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "dd_msg")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DdMsg {
    @Id
    private String id;
    @Indexed(name = "ddMsgId_unique_idx", unique = true)
    private String ddMsgId;
    private Long ddMsgAmount;
    private String ddMsgBody;
    private String status;
    @CreatedDate
    private Instant createdAt;
    private Instant expiredAt;
    @Version
    private Long version;
    private String errorMsg;
    private String batchTag;
}
