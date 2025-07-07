package com.snow.al.dd.core.single.exec.state;

import com.snow.al.dd.core.mongo.db.DdMsgBatch;
import com.snow.al.dd.core.mongo.db.DdMsgSingle;
import com.snow.al.dd.core.mongo.db.DdMsgSingleStatus;
import com.snow.al.dd.core.single.exec.vendor.SingleSendResponse;
import com.snow.al.dd.core.single.exec.vendor.SingleSendResponse.SendNextStep;
import com.snow.al.timeoutcenter.TimeLongUtil;
import com.snow.al.timeoutcenter.TimeoutTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

@Slf4j
public class ReadyToVendorSendState implements DdSingleExecuteState {
    @Override
    public void doExecute(DdSingleExecuteContext context) {
        doExecuteCore(context, log, single -> {
            if (single == null || !single.getStatus().equals(DdMsgSingleStatus.READY_TO_SEND.getStatus())) {
                log.error("batchId:{} 状态异常，当前状态为：{}", Optional.ofNullable(single).map(DdMsgSingle::getId).orElse(null), Optional.ofNullable(single).map(DdMsgSingle::getStatus).orElse(null));
                context.setState(null);
                return;
            }
            var vendorBizTimeFacade = context.getVendorSingleExecuteAdapter().getVendorBizTimeFacade(single);
            vendorBizTimeFacade.executeInActiveBizTime(single.getVendorCode(), single.getBankCode(),
                    () -> {
                        var vendorSendResponse = context.getVendorSingleExecuteAdapter().sendWithBlockingRateLimiter(single);
                        vendorSendResponse.getSendNextStep().accept(vendorSendResponse, context);
                    },
                    a -> {
                        var ddSingleExecTimeoutCenter = context.getDdSingleExecTimeoutCenter();
                        MongoTemplate mongoTemplate = context.getMongoTemplate();
                        TimeoutTask tt = new TimeoutTask();
                        tt.setTaskFrom(ddSingleExecTimeoutCenter.getBizTag());
                        tt.setTaskFromId(single.getId());
                        tt.setTaskTimeout(TimeLongUtil.currentTimeMillis(a));
                        ddSingleExecTimeoutCenter.publish(tt);
                        mongoTemplate.findAndModify(
                                new Query(Criteria.where("id").is(single.getId()).and("status").is(DdMsgSingleStatus.READY_TO_SEND.getStatus())),
                                new Update().set("status", DdMsgSingleStatus.PENDING_TO_SEND.getStatus()),
                                DdMsgBatch.class);
                        context.setState(null);
                    },
                    () -> {
                        var vendorSendResponse = new SingleSendResponse("", "");
                        vendorSendResponse.setDdMsgId(single.getId());
                        vendorSendResponse.setSuccessAmount(0L);
                        vendorSendResponse.setReturnCode("CANCEL");
                        vendorSendResponse.setReturnMsg("NO BUSINESS TIME");
                        vendorSendResponse.setReturnCodeGroup("CANCEL");
                        vendorSendResponse.setReturnCodeGroupDesc("NO BIZ TIME");
                        vendorSendResponse.setSendNextStep(SendNextStep.FINAL_FAIL);
                        vendorSendResponse.getSendNextStep().accept(vendorSendResponse, context);
                    });
        });
    }
}
