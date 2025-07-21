package com.snow.al.dd.core.mongo.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "client_dd_auth")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDdAuth {
    @Id
    private String id;
    private String clientId;
    private String vendorGroup;
    private String vendorAuthCode;
    private Instant validFrom;
    private Instant validTo;
}
