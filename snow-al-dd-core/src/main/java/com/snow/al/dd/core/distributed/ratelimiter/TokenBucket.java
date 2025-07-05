package com.snow.al.dd.core.distributed.ratelimiter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "token_buckets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenBucket {
    @org.springframework.data.annotation.Id
    private String id;
    private long capacity;
    private long tokens;
    private long lastRefillTime;
    private long refillRate;

}
