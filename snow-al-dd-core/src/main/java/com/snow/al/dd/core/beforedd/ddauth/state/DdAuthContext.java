package com.snow.al.dd.core.beforedd.ddauth.state;

import com.snow.al.dd.core.beforedd.ddauth.vendor.VendorAuthAdapter;
import com.snow.al.dd.core.distributed.lock.DdLock;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.MongoTemplate;

@RequiredArgsConstructor
@Getter
public class DdAuthContext {
    private final MongoTemplate mongoTemplate;
    private final DdLock ddLock;
    private final VendorAuthAdapter vendorAuthAdapter;
    @Setter
    private DdAuthState state;

    public void doAuth() {
        do {
            state.doAuth(this);
        } while (state != null);
    }
}
