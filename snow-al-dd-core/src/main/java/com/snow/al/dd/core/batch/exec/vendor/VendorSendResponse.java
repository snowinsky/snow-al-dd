package com.snow.al.dd.core.batch.exec.vendor;

import com.snow.al.dd.core.batch.exec.state.DdBatchExecuteContext;
import com.snow.al.dd.core.mongo.db.DdMsgBatch;
import com.snow.al.dd.core.mongo.db.DdMsgBatchStatus;
import com.snow.al.timeoutcenter.TimeLongUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class VendorSendResponse extends VendorResponse {
    String mchTradeNo;
    String vendorTradeNo;
    String sendResLog;
    Duration nextStepDelayInterval;
    SendNextStep sendNextStep;

    public VendorSendResponse(String returnCode, String returnMsg) {
        super(returnCode, returnMsg);
    }


    public enum SendNextStep implements VendorResponseConsumer<VendorSendResponse> {
        RAPID_QUERY {
            @Override
            public void accept(VendorSendResponse response, DdBatchExecuteContext context) {
                context.getMongoTemplate().findAndModify(
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_SEND.getStatus())),
                        new Update().set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("sendResLog", response.getSendResLog())
                                .set("status", DdMsgBatchStatus.READY_TO_VENDOR_QUERY.getStatus()),
                        DdMsgBatch.class);
                context.setState(DdMsgBatchStatus.READY_TO_VENDOR_QUERY.getState());
            }
        },
        QUERY {
            @Override
            public void accept(VendorSendResponse response, DdBatchExecuteContext context) {
                context.getMongoTemplate().findAndModify(
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_SEND.getStatus())),
                        new Update().set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("sendResLog", response.getSendResLog())
                                .set("status", DdMsgBatchStatus.WAIT_TO_QUERY.getStatus()),
                        DdMsgBatch.class);
                context.getDdBatchExecTimeoutCenter().publish(context.getBatchId(), TimeLongUtil.currentTimeMillis(LocalDateTime.now().plus(response.nextStepDelayInterval)));
                context.setState(null);
            }
        },
        FINAL_SUCCESS {
            @Override
            public void accept(VendorSendResponse response, DdBatchExecuteContext context) {
                DdMsgBatch ddMsgBatch = context.getMongoTemplate().findOne(new Query(Criteria.where("id").is(context.getBatchId())), DdMsgBatch.class);
                List<DdMsgBatch.DbMsgResOfBatch> l = Optional.ofNullable(ddMsgBatch.getBatchReqMsgList())
                        .map(reqMsgList -> reqMsgList.stream().map(a -> {
                            DdMsgBatch.DbMsgResOfBatch dbMsgResOfBatch = new DdMsgBatch.DbMsgResOfBatch(a);
                            dbMsgResOfBatch.setRequestAmount(a.getDdMsgAmount());
                            dbMsgResOfBatch.setSuccessAmount(a.getDdMsgAmount());
                            dbMsgResOfBatch.setReturnCode("0000");
                            dbMsgResOfBatch.setReturnMsg("success");
                            return dbMsgResOfBatch;
                        }).toList())
                        .orElse(Collections.emptyList());
                context.getMongoTemplate().findAndModify(
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_SEND.getStatus())),
                        new Update().set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("sendResLog", response.getSendResLog())
                                .set("batchResMsgList", l)
                                .set("status", DdMsgBatchStatus.READY_TO_RESFILE_NOTIFY.getStatus()),
                        DdMsgBatch.class);
                context.setState(DdMsgBatchStatus.READY_TO_RESFILE_NOTIFY.getState());
            }
        },
        FINAL_FAIL {
            @Override
            public void accept(VendorSendResponse response, DdBatchExecuteContext context) {
                DdMsgBatch ddMsgBatch = context.getMongoTemplate().findOne(new Query(Criteria.where("id").is(context.getBatchId())), DdMsgBatch.class);
                List<DdMsgBatch.DbMsgResOfBatch> l = Optional.ofNullable(ddMsgBatch.getBatchReqMsgList())
                        .map(reqMsgList -> reqMsgList.stream().map(a -> {
                            DdMsgBatch.DbMsgResOfBatch dbMsgResOfBatch = new DdMsgBatch.DbMsgResOfBatch(a);
                            dbMsgResOfBatch.setRequestAmount(a.getDdMsgAmount());
                            dbMsgResOfBatch.setSuccessAmount(a.getDdMsgAmount());
                            dbMsgResOfBatch.setReturnCode(response.getReturnCode());
                            dbMsgResOfBatch.setReturnMsg(response.getReturnMsg());
                            return dbMsgResOfBatch;
                        }).toList())
                        .orElse(Collections.emptyList());
                context.getMongoTemplate().findAndModify(
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_SEND.getStatus())),
                        new Update().set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("sendResLog", response.getSendResLog())
                                .set("batchResMsgList", l)
                                .set("status", DdMsgBatchStatus.READY_TO_RESFILE_NOTIFY.getStatus()),
                        DdMsgBatch.class);
                context.setState(DdMsgBatchStatus.READY_TO_RESFILE_NOTIFY.getState());
            }
        },
        CALLBACK {
            @Override
            public void accept(VendorSendResponse response, DdBatchExecuteContext context) {
                context.getMongoTemplate().findAndModify(
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_SEND.getStatus())),
                        new Update().set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("sendResLog", response.getSendResLog())
                                .set("status", DdMsgBatchStatus.READY_TO_VENDOR_CALLBACK.getStatus()),
                        DdMsgBatch.class);
                context.setState(null);
            }
        },
        SEND_AGAIN {
            @Override
            public void accept(VendorSendResponse response, DdBatchExecuteContext context) {
                context.getMongoTemplate().findAndModify(
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_SEND.getStatus())),
                        new Update().set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("sendResLog", response.getSendResLog())
                                .set("status", DdMsgBatchStatus.READY_TO_VENDOR_SEND.getStatus()),
                        DdMsgBatch.class);
                context.setState(DdMsgBatchStatus.READY_TO_VENDOR_SEND.getState());
            }
        },
        SEND_DELAY {
            @Override
            public void accept(VendorSendResponse response, DdBatchExecuteContext context) {
                context.getMongoTemplate().findAndModify(
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_SEND.getStatus())),
                        new Update().set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("sendResLog", response.getSendResLog())
                                .set("status", DdMsgBatchStatus.WAIT_TO_SEND.getStatus()),
                        DdMsgBatch.class);
                context.setState(null);
                context.getDdBatchExecTimeoutCenter().publish(context.getBatchId(), TimeLongUtil.currentTimeMillis(LocalDateTime.now().plus(response.nextStepDelayInterval)));
            }
        },
        SEND_RENAME {
            @Override
            public void accept(VendorSendResponse response, DdBatchExecuteContext context) {
                context.getMongoTemplate().findAndModify(
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_SEND.getStatus())),
                        new Update().set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("sendResLog", response.getSendResLog())
                                .set("status", DdMsgBatchStatus.READY_TO_REQFILE_GENERATE.getStatus()),
                        DdMsgBatch.class);
                context.setState(DdMsgBatchStatus.READY_TO_REQFILE_GENERATE.getState());
            }
        }
    }

}
