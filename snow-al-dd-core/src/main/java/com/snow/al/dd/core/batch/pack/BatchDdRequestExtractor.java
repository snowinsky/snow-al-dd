package com.snow.al.dd.core.batch.pack;

import com.snow.al.dd.core.mongo.model.DdRequest;
import com.snow.al.dd.core.mongo.db.DdMsg;
import org.springframework.data.util.Pair;

public interface BatchDdRequestExtractor {

    DdRequestExtractResult extract(DdRequest ddRequest);

    DdRequestExtractResult extract(DdMsg ddMsg);

    Pair<Boolean, String> eligibleCheck(DdRequest ddRequest);
}
