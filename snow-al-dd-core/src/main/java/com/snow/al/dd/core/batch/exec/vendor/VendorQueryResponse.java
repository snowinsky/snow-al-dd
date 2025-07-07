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
public class VendorQueryResponse extends VendorResponse {
    private String mchTradeNo;
    private String vendorTradeNo;
    private String tradeStatus;
    private String tradeStatusDesc;
    private String queryResLog;
    private Duration nextStepDelayInterval;
    private QueryNextStep queryNextStep;
    public VendorQueryResponse(String returnCode, String returnMsg) {
        super(returnCode, returnMsg);
    }


    public enum QueryNextStep implements VendorResponseConsumer<VendorQueryResponse> {
        QUERY {
            @Override
            public void accept(VendorQueryResponse response, DdBatchExecuteContext context) {
                context.getMongoTemplate().findAndModify(
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_QUERY.getStatus())),
                        new Update().set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("queryResLog", response.getQueryResLog())
                                .set("status", DdMsgBatchStatus.WAIT_TO_QUERY.getStatus()),
                        DdMsgBatch.class);
                context.setState(null);
                context.getDdBatchExecTimeoutCenter().publish(context.getBatchId(), TimeLongUtil.currentTimeMillis(LocalDateTime.now().plus(response.nextStepDelayInterval)));
            }
        },
        READY_TO_DOWNLOAD {
            @Override
            public void accept(VendorQueryResponse response, DdBatchExecuteContext context) {
                context.getMongoTemplate().findAndModify(
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_QUERY.getStatus())),
                        new Update().set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("queryResLog", response.getQueryResLog())
                                .set("status", DdMsgBatchStatus.READY_TO_VENDOR_DOWNLOAD.getStatus()),
                        DdMsgBatch.class);
                context.setState(DdMsgBatchStatus.READY_TO_VENDOR_DOWNLOAD.getState());
            }
        },
        SEND_AGAIN {
            @Override
            public void accept(VendorQueryResponse response, DdBatchExecuteContext context) {
                context.getMongoTemplate().findAndModify(
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_QUERY.getStatus())),
                        new Update().set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("queryResLog", response.getQueryResLog())
                                .set("status", DdMsgBatchStatus.WAIT_TO_SEND.getStatus()),
                        DdMsgBatch.class);
                context.setState(null);
                context.getDdBatchExecTimeoutCenter().publish(context.getBatchId(), TimeLongUtil.currentTimeMillis(LocalDateTime.now().plus(response.nextStepDelayInterval)));
            }
        },
        SEND_RENAME {
            @Override
            public void accept(VendorQueryResponse response, DdBatchExecuteContext context) {
                context.getMongoTemplate().findAndModify(
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_QUERY.getStatus())),
                        new Update().set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("queryResLog", response.getQueryResLog())
                                .set("status", DdMsgBatchStatus.READY_TO_REQFILE_GENERATE.getStatus()),
                        DdMsgBatch.class);
                context.setState(null);
                context.getDdBatchExecTimeoutCenter().publish(context.getBatchId(), TimeLongUtil.currentTimeMillis(LocalDateTime.now().plus(response.nextStepDelayInterval)));
            }
        },
        FINAL_FAIL {
            @Override
            public void accept(VendorQueryResponse response, DdBatchExecuteContext context) {
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
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_QUERY.getStatus())),
                        new Update().set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("queryResLog", response.getQueryResLog())
                                .set("batchResMsgList", l)
                                .set("status", DdMsgBatchStatus.READY_TO_RESFILE_NOTIFY.getStatus()),
                        DdMsgBatch.class);
                context.setState(DdMsgBatchStatus.READY_TO_RESFILE_NOTIFY.getState());
            }
        },
        FINAL_SUCCESS {
            @Override
            public void accept(VendorQueryResponse response, DdBatchExecuteContext context) {
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
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_QUERY.getStatus())),
                        new Update().set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("queryResLog", response.getQueryResLog())
                                .set("batchResMsgList", l)
                                .set("status", DdMsgBatchStatus.READY_TO_RESFILE_NOTIFY.getStatus()),
                        DdMsgBatch.class);
                context.setState(DdMsgBatchStatus.READY_TO_RESFILE_NOTIFY.getState());
            }
        }
    }
}
