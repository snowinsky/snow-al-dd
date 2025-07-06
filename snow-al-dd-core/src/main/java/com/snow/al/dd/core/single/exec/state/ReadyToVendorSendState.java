package com.snow.al.dd.core.single.exec.state;

import com.snow.al.dd.core.batch.exec.timeout.DdBatchExecTimeoutCenter;
import com.snow.al.dd.core.batch.exec.vendor.VendorSendRequest;
import com.snow.al.dd.core.batch.exec.vendor.VendorSendResponse;
import com.snow.al.dd.core.mongo.model.db.DdMsgBatch;
import com.snow.al.dd.core.mongo.model.db.DdMsgSingleStatus;
import com.snow.al.timeoutcenter.TimeLongUtil;
import com.snow.al.timeoutcenter.TimeoutTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public class ReadyToVendorSendState  implements DdSingleExecuteState{
    @Override
    public void doExecute(DdSingleExecuteContext context) {
        doExecuteCore(context, log, batch -> {
            if (batch == null || !batch.getStatus().equals(DdMsgSingleStatus.READY_TO_VENDOR_SEND.getStatus())) {
                log.error("batchId:{} 状态异常，当前状态为：{}", Optional.ofNullable(batch).map(DdMsgBatch::getId).orElse(null), Optional.ofNullable(batch).map(DdMsgBatch::getStatus).orElse(null));
                context.setState(null);
                return;
            }
            var vendorBizTimeFacade = context.getVendorExecuteAdapter().getVendorBizTimeFacade(batch);
            if (vendorBizTimeFacade.isBizTime(batch.getVendorCode(), batch.getBankCode(), LocalDateTime.now())) {
                VendorSendResponse vendorSendResponse = context.getVendorExecuteAdapter().sendWithBlockingRateLimiter(batch, new VendorSendRequest(batch));
                vendorSendResponse.getSendNextStep().accept(vendorSendResponse, context);
            } else {
                DdBatchExecTimeoutCenter ddBatchExecTimeoutCenter = context.getDdBatchExecTimeoutCenter();
                MongoTemplate mongoTemplate = context.getMongoTemplate();
                LocalDateTime latestBizTime = vendorBizTimeFacade.getLatestBizTime(batch.getVendorCode(), batch.getBankCode(), LocalDateTime.now());
                TimeoutTask tt = new TimeoutTask();
                tt.setTaskFrom(ddBatchExecTimeoutCenter.getBizTag());
                tt.setTaskFromId(batch.getId());
                tt.setTaskTimeout(TimeLongUtil.currentTimeMillis(latestBizTime));
                ddBatchExecTimeoutCenter.publish(tt);
                mongoTemplate.findAndModify(
                        new Query(Criteria.where("id").is(batch.getId()).and("status").is(DdMsgSingleStatus.READY_TO_VENDOR_SEND.getStatus())),
                        new Update().set("status", DdMsgSingleStatus.WAIT_TO_SEND.getStatus()),
                        DdMsgBatch.class);
                context.setState(null);
            }
        });
    }
}
