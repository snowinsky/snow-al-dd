package com.snow.al.dd.core.batch.exec.state;

import com.snow.al.dd.core.batch.exec.timeout.DdBatchExecTimeoutCenter;
import com.snow.al.dd.core.batch.exec.vendor.VendorSendRequest;
import com.snow.al.dd.core.batch.exec.vendor.VendorSendResponse;
import com.snow.al.dd.core.batch.exec.vendor.VendorSendResponse.SendNextStep;
import com.snow.al.dd.core.mongo.db.DdMsgBatch;
import com.snow.al.dd.core.mongo.db.DdMsgBatchStatus;
import com.snow.al.timeoutcenter.TimeLongUtil;
import com.snow.al.timeoutcenter.TimeoutTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class ReadyToVendorSendState implements DdBatchExecuteState {

    @Override
    public void doExecute(DdBatchExecuteContext context) {
        doExecuteCore(context, log, batch -> {
            if (batch == null || !batch.getStatus().equals(DdMsgBatchStatus.READY_TO_VENDOR_SEND.getStatus())) {
                log.error("batchId:{} 状态异常，当前状态为：{}", Optional.ofNullable(batch).map(DdMsgBatch::getId).orElse(null), Optional.ofNullable(batch).map(DdMsgBatch::getStatus).orElse(null));
                context.setState(null);
                return;
            }
            var vendorBizTimeFacade = context.getVendorExecuteAdapter().getVendorBizTimeFacade(batch);
            vendorBizTimeFacade.executeInActiveBizTime(batch.getVendorCode(), batch.getBankCode(),
                    () -> {
                        VendorSendResponse vendorSendResponse = context.getVendorExecuteAdapter().sendWithBlockingRateLimiter(batch, new VendorSendRequest(batch));
                        vendorSendResponse.getSendNextStep().accept(vendorSendResponse, context);
                    }, a -> {
                        DdBatchExecTimeoutCenter ddBatchExecTimeoutCenter = context.getDdBatchExecTimeoutCenter();
                        MongoTemplate mongoTemplate = context.getMongoTemplate();
                        LocalDateTime latestBizTime = vendorBizTimeFacade.getLatestBizTime(batch.getVendorCode(), batch.getBankCode(), LocalDateTime.now());
                        TimeoutTask tt = new TimeoutTask();
                        tt.setTaskFrom(ddBatchExecTimeoutCenter.getBizTag());
                        tt.setTaskFromId(batch.getId());
                        tt.setTaskTimeout(TimeLongUtil.currentTimeMillis(latestBizTime));
                        ddBatchExecTimeoutCenter.publish(tt);
                        mongoTemplate.findAndModify(
                                new Query(Criteria.where("id").is(batch.getId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_SEND.getStatus())),
                                new Update().set("status", DdMsgBatchStatus.WAIT_TO_SEND.getStatus()),
                                DdMsgBatch.class);
                        context.setState(null);
                    }, () -> {
                        VendorSendResponse vendorSendResponse = new VendorSendResponse("CANCEL", "NO BIZ TIME");
                        vendorSendResponse.setSendNextStep(SendNextStep.FINAL_FAIL);
                        vendorSendResponse.getSendNextStep().accept(vendorSendResponse, context);
                    });
        });
    }
}
