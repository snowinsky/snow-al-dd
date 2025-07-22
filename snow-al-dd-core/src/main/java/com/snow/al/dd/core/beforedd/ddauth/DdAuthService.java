package com.snow.al.dd.core.beforedd.ddauth;

import com.snow.al.dd.core.beforedd.ddauth.vendor.VendorAuthAdapter;
import com.snow.al.dd.core.distributed.lock.DdLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;

@RequiredArgsConstructor
@Slf4j
public class DdAuthService {

    private final MongoTemplate mongoTemplate;
    private final DdLock ddLock;
    private final VendorAuthAdapter vendorAuthAdapter;


    void doAuth(DoAuthReq req, DoActionListener listener) {

    }

    QueryAuthRes queryAuth(QueryAuthReq req) {
        return null;
    }


}
