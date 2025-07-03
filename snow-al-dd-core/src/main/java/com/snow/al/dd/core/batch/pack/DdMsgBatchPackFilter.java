package com.snow.al.dd.core.batch.pack;

import com.snow.al.dd.core.mongo.model.DdRequest;
import org.springframework.data.util.Pair;

public interface DdMsgBatchPackFilter {

    Pair<Boolean, String> eligibleCheck(DdRequest ddRequest);

    String batchTag(DdRequest ddRequest);

    String ddMsgId(DdRequest ddRequest);

    Long ddMsgAmount(DdRequest ddRequest);

}
