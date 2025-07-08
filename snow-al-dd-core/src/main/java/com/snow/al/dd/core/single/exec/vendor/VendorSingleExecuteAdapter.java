package com.snow.al.dd.core.single.exec.vendor;

import com.snow.al.dd.core.biztime.domain.SendTimeService;
import com.snow.al.dd.core.distributed.ratelimiter.DdRateLimiter;
import com.snow.al.dd.core.mongo.db.DdMsgSingle;
import com.snow.al.dd.core.mongo.model.DdRequest;
import com.snow.al.dd.core.single.exec.DdRequestExtractResult;
import org.springframework.data.util.Pair;

public interface VendorSingleExecuteAdapter {

    SingleSendResponse send(DdMsgSingle request);

    SingleQueryResponse query(DdMsgSingle request);

    void notify(DdMsgSingle request);

    DdRateLimiter getRateLimiter(DdMsgSingle batch);

    SendTimeService getVendorBizTimeFacade(DdMsgSingle batch);

    default SingleSendResponse sendWithBlockingRateLimiter(DdMsgSingle request) {
        DdRateLimiter rateLimiter = getRateLimiter(request);
        if (rateLimiter == null) {
            return send(request);
        }
        rateLimiter.acquire();
        return send(request);
    }

    Pair<Boolean, String> eligibleCheck(DdRequest ddRequest);

    DdRequestExtractResult extract(DdRequest ddRequest);
}
