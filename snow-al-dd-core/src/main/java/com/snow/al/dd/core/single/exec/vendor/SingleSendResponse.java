package com.snow.al.dd.core.single.exec.vendor;

import com.snow.al.dd.core.mongo.db.DdMsgSingle;
import com.snow.al.dd.core.mongo.db.DdMsgSingleStatus;
import com.snow.al.dd.core.single.exec.state.DdSingleExecuteContext;
import com.snow.al.timeoutcenter.TimeLongUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
public class SingleSendResponse extends VendorResponse {

    private String ddMsgId;
    private String mchTradeNo;
    private String vendorTradeNo;
    private Long successAmount;
    private String sendResLog;
    private String returnCode;
    private String returnMsg;
    private String returnCodeGroup;
    private String returnCodeGroupDesc;
    private Duration delayTime;

    private SendNextStep sendNextStep;

    public SingleSendResponse(String returnCodeGroup, String returnCodeGroupDesc) {
        super(returnCodeGroup, returnCodeGroupDesc);
    }

    public enum SendNextStep implements VendorResponseConsumer<SingleSendResponse> {
        FINAL_SUCCESS {
            @Override
            public void accept(SingleSendResponse response, DdSingleExecuteContext context) {
                context.getMongoTemplate().updateMulti(
                        new Query(new Criteria("id").is(response.getDdMsgId()).and("status").is(DdMsgSingleStatus.READY_TO_SEND.getStatus())),
                        new Update().set("status", DdMsgSingleStatus.READY_TO_NOTIFY.getStatus())
                                .set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("successAmount", response.getSuccessAmount())
                                .set("sendResLog", response.getSendResLog())
                                .set("returnCode", response.getReturnCode()),
                        DdMsgSingle.class);
                context.setState(DdMsgSingleStatus.READY_TO_NOTIFY.getState());
            }
        },
        FINAL_FAIL {
            @Override
            public void accept(SingleSendResponse response, DdSingleExecuteContext context) {
                context.getMongoTemplate().updateMulti(
                        new Query(new Criteria("id").is(response.getDdMsgId()).and("status").is(DdMsgSingleStatus.READY_TO_SEND.getStatus())),
                        new Update().set("status", DdMsgSingleStatus.READY_TO_NOTIFY.getStatus())
                                .set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("successAmount", response.getSuccessAmount())
                                .set("sendResLog", response.getSendResLog())
                                .set("returnCode", response.getReturnCode()),
                        DdMsgSingle.class);
                context.setState(DdMsgSingleStatus.READY_TO_NOTIFY.getState());
            }
        },
        SEND_AGAIN {
            @Override
            public void accept(SingleSendResponse response, DdSingleExecuteContext context) {
                context.getMongoTemplate().updateMulti(
                        new Query(new Criteria("id").is(response.getDdMsgId()).and("status").is(DdMsgSingleStatus.READY_TO_SEND.getStatus())),
                        new Update().set("status", DdMsgSingleStatus.READY_TO_SEND.getStatus())
                                .set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("successAmount", response.getSuccessAmount())
                                .set("sendResLog", response.getSendResLog())
                                .set("returnCode", response.getReturnCode()),
                        DdMsgSingle.class);
                if (response.getDelayTime() == null) {
                    context.setState(DdMsgSingleStatus.READY_TO_SEND.getState());
                } else {
                    context.getDdSingleExecTimeoutCenter().publish(context.getDdMsgId(), TimeLongUtil.currentTimeMillis(LocalDateTime.now().plus(response.getDelayTime())));
                    context.setState(null);
                }
            }
        },
        RENAME_SEND {
            @Override
            public void accept(SingleSendResponse response, DdSingleExecuteContext context) {
                context.getMongoTemplate().updateMulti(
                        new Query(new Criteria("id").is(response.getDdMsgId()).and("status").is(DdMsgSingleStatus.READY_TO_SEND.getStatus())),
                        new Update().set("status", DdMsgSingleStatus.READY_TO_SEND.getStatus())
                                .set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("successAmount", response.getSuccessAmount())
                                .set("sendResLog", response.getSendResLog())
                                .set("returnCode", response.getReturnCode()),
                        DdMsgSingle.class);
                if (response.getDelayTime() == null) {
                    context.setState(DdMsgSingleStatus.READY_TO_SEND.getState());
                } else {
                    context.getDdSingleExecTimeoutCenter().publish(context.getDdMsgId(), TimeLongUtil.currentTimeMillis(LocalDateTime.now().plus(response.getDelayTime())));
                    context.setState(null);
                }
            }
        },
        QUERY {
            @Override
            public void accept(SingleSendResponse response, DdSingleExecuteContext context) {
                context.getMongoTemplate().updateMulti(
                        new Query(new Criteria("id").is(response.getDdMsgId()).and("status").is(DdMsgSingleStatus.READY_TO_SEND.getStatus())),
                        new Update().set("status",
                                        response.getDelayTime() == null ? DdMsgSingleStatus.READY_TO_QUERY.getStatus() : DdMsgSingleStatus.PENDING_TO_QUERY.getStatus())
                                .set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("successAmount", response.getSuccessAmount())
                                .set("sendResLog", response.getSendResLog())
                                .set("returnCode", response.getReturnCode()),
                        DdMsgSingle.class);
                if (response.getDelayTime() == null) {
                    context.setState(DdMsgSingleStatus.READY_TO_SEND.getState());
                } else {
                    context.getDdSingleExecTimeoutCenter().publish(context.getDdMsgId(), TimeLongUtil.currentTimeMillis(LocalDateTime.now().plus(response.getDelayTime())));
                    context.setState(null);
                }
            }
        },
        CALLBACK {
            @Override
            public void accept(SingleSendResponse response, DdSingleExecuteContext context) {
                context.getMongoTemplate().updateMulti(
                        new Query(new Criteria("id").is(response.getDdMsgId()).and("status").is(DdMsgSingleStatus.READY_TO_SEND.getStatus())),
                        new Update().set("status", DdMsgSingleStatus.PENDING_TO_QUERY.getStatus())
                                .set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("sendResLog", response.getSendResLog()),
                        DdMsgSingle.class);
                context.setState(null);
            }
        };
    }

}
