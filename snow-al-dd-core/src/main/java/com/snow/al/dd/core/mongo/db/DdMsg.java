package com.snow.al.dd.core.mongo.db;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

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
    private Date createdAt;
    private Date expiredAt;
    @Version
    private Long version;
    private String errorMsg;
    private String batchTag;
}
