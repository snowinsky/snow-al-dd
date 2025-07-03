package com.snow.al.dd.core.batch.pack;

import com.snow.al.dd.core.mongo.model.db.DdMsg;

public interface BatchDdPackConfig {

    Integer getMaxBatchSize(DdMsg ddMsg);

    Long getMaxBatchAmount(DdMsg ddMsg);

    Long getExpiredSeconds(DdMsg ddMsg);
}
