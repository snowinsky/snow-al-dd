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
import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
public class SingleQueryResponse extends VendorResponse {
    private String ddMsgId;
    private String mchTradeNo;
    private String vendorTradeNo;
    private Long successAmount;
    private String sendResLog;
    private String returnCode;
    private String returnMsg;
    private String returnCodeGroup;
    private String returnCodeGroupDesc;
    private Instant expireTime;

    private Duration delayTime;

    private QueryNextStep queryNextStep;

    public SingleQueryResponse(String returnCodeGroup, String returnCodeGroupDesc) {
        super(returnCodeGroup, returnCodeGroupDesc);
    }

    public enum QueryNextStep implements VendorResponseConsumer<SingleQueryResponse> {
        FINAL {
            @Override
            public void accept(SingleQueryResponse response, DdSingleExecuteContext context) {
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
        QUERY {
            @Override
            public void accept(SingleQueryResponse response, DdSingleExecuteContext context) {
                context.getMongoTemplate().updateMulti(
                        new Query(new Criteria("id").is(response.getDdMsgId()).and("status").is(DdMsgSingleStatus.READY_TO_QUERY.getStatus())),
                        new Update().set("status", DdMsgSingleStatus.PENDING_TO_QUERY.getStatus())
                                .set("mchTradeNo", response.getMchTradeNo())
                                .set("vendorTradeNo", response.getVendorTradeNo())
                                .set("queryResLog", response.getSendResLog()),
                        DdMsgSingle.class);
                context.getDdSingleExecTimeoutCenter().publish(context.getDdMsgId(),
                        TimeLongUtil.currentTimeMillis(LocalDateTime.now().plus(response.getDelayTime())));
                context.setState(null);
            }
        },
        SEND {
            @Override
            public void accept(SingleQueryResponse response, DdSingleExecuteContext context) {
                context.getMongoTemplate().updateMulti(
                        new Query(new Criteria("id").is(response.getDdMsgId()).and("status").is(DdMsgSingleStatus.READY_TO_QUERY.getStatus())),
                        new Update().set("status", DdMsgSingleStatus.READY_TO_SEND.getStatus())
                                .set("mchTradeNo", response.getMchTradeNo())
                                .set("queryResLog", response.getSendResLog()),
                        DdMsgSingle.class);
                context.setState(DdMsgSingleStatus.READY_TO_SEND.getState());
            }
        };
    }

}
