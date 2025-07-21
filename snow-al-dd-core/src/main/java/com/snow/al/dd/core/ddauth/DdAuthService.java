package com.snow.al.dd.core.ddauth;

import com.snow.al.dd.core.distributed.lock.DdLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Duration;

@RequiredArgsConstructor
@Slf4j
public class DdAuthService {

    private final MongoTemplate mongoTemplate;
    private final DdLock ddLock;


    public void auth() {
        ddLock.tryLock("key", Duration.ofSeconds(10), () -> {
            log.info("do something");
        });
    }
}
