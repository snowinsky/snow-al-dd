package com.snow.al.dd.core.distributed.lock;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "distributed_locks")
@Data
public class DistributedLock {

    @Id
    private String id;
    @Indexed(name = "lockName_unique_idx", unique = true)
    private String lockName;
    private String clientId;
    @Indexed(name = "expireAt_ttl_idx", expireAfterSeconds = 0)
    private Instant expireAt;
    private Instant acquiredAt;
    private int lockCount = 1;
}
