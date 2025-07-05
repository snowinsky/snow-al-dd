package com.snow.al.dd.core.distributed.ratelimiter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("leaky_buckets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeakyBucket {
    @Id
    private String id;  // 限流器标识
    private long capacity;  // 桶容量
    private long remaining; // 剩余空间
    private long lastLeakTime; // 上次漏水时间
    private long leakRate;    // 漏水速率（微秒/单位请求）


}
