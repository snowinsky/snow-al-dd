package com.snow.al.dd.core.single.exec.vendor;

import com.snow.al.dd.core.batch.exec.state.DdBatchExecuteContext;
import com.snow.al.dd.core.single.exec.state.DdSingleExecuteContext;
import lombok.Data;

import java.time.Instant;

@Data
public class SingleQueryResponse {
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

    private QueryNextStep queryNextStep;

    public enum QueryNextStep implements VendorResponseConsumer<SingleQueryResponse> {
        QUERY_NEXT_STEP {
            @Override
            public void accept(SingleQueryResponse response, DdBatchExecuteContext context) {

            }
        },
        QUERY_COMPLETE {
            @Override
            public void accept(SingleQueryResponse response, DdBatchExecuteContext context) {

            }
        },
        QUERY_FAIL {
            @Override
            public void accept(SingleQueryResponse response, DdBatchExecuteContext context) {

            }
        },
    }

}
