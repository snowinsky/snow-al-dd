package com.snow.al.dd.core.batch.exec.vendor;

import com.snow.al.dd.core.biztime.domain.SendTimeService;
import com.snow.al.dd.core.distributed.ratelimiter.DdRateLimiter;
import com.snow.al.dd.core.mongo.db.DdMsgBatch;

public interface VendorExecuteAdapter {
    void generateReqFile(DdMsgBatch batch);

    void parseResFile(DdMsgBatch batch);

    void notifyResFile(DdMsgBatch batch);

    DdRateLimiter getRateLimiter(DdMsgBatch batch);

    SendTimeService getVendorBizTimeFacade(DdMsgBatch batch);

    default VendorSendResponse sendWithBlockingRateLimiter(DdMsgBatch batch, VendorRequest<VendorSendResponse> request) {
        DdRateLimiter rateLimiter = getRateLimiter(batch);
        if (rateLimiter == null) {
            return send(request);
        }
        rateLimiter.acquire();
        return send(request);
    }

    default VendorSendResponse sendWithNonBlockingRateLimiter(DdMsgBatch batch, VendorRequest<VendorSendResponse> request) {
        DdRateLimiter rateLimiter = getRateLimiter(batch);
        if (rateLimiter == null) {
            return send(request);
        }
        if (rateLimiter.tryAcquire()) {
            return send(request);
        } else {
            return new VendorSendResponse("429", "请求过于频繁");
        }
    }


    VendorSendResponse send(VendorRequest<VendorSendResponse> request);

    VendorQueryResponse query(VendorRequest<VendorQueryResponse> request);

    VendorDownloadResponse download(VendorRequest<VendorDownloadResponse> request);

    //<T extends VendorResponse> T callback(VendorRequest<T> request);
}
