package com.snow.al.dd.core.batch.pack;

import com.mongodb.client.result.UpdateResult;
import com.snow.al.dd.core.distributedlock.MongoTemplateDistributedLock;
import com.snow.al.dd.core.mongo.model.DdRequest;
import com.snow.al.dd.core.mongo.model.db.DdMsg;
import com.snow.al.dd.core.mongo.model.db.DdMsgBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class DdMsgBatchPackService {

    private final MongoTemplate mongoTemplate;
    private final MongoTemplateDistributedLock distributedLock;
    private final BatchDdPackConfig batchDdPackConfig;

    public void startRestoreUnpackedDdMsg() {
        new Thread(this::restoreUnpackedDdMsg).start();
    }

    @Transactional
    public void pack(DdRequest ddRequest) {
        log.info("start to pack the ddRequest with ddMsgId:{}", ddRequest.getDdMsgId());
        String batchTag = ddRequest.getDdBatchTag();
        //1, insert the dd message into the dd_msg collection
        DdMsg ddMsg = Optional.of(ddRequest).map(a -> DdMsg.builder()
                .ddMsgId(a.getDdMsgId())
                .ddMsgAmount(a.getDdMsgAmount())
                .ddMsgBody(a.getDdMsgBody())
                .batchTag(a.getDdBatchTag())
                .status("waitToPack")
                .build()).orElse(null);
        DdMsg inserted = mongoTemplate.insert(ddMsg);
        if (inserted.getId() == null) {
            throw new IllegalStateException("insert dd_msg fail:" + ddMsg);
        }
        // start to pack the dd message, first update the status of the dd message to packing
        Query query = new Query(Criteria.where("status").is("waitToPack").and("id").is(inserted.getId()));
        Update update = new Update().set("status", "packing");
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, DdMsg.class);
        if (updateResult.getModifiedCount() == 1) {
            String clientId = distributedLock.getThreadClientId();
            try {
                distributedLock.acquireLock(batchTag, clientId, Duration.ofSeconds(10), Duration.ofSeconds(10));
                packDdMsg2BatchTag(batchTag, ddMsg);
            } finally {
                distributedLock.releaseLock(batchTag, clientId);
            }
        }
    }

    private void packDdMsg2BatchTag(String batchTag, DdMsg ddMsg) {
        // find the specific batch by batchTag and status is packing
        Query batchQuery = new Query(Criteria.where("batchTag").is(batchTag)
                .and("status").is("packing"));
        DdMsgBatch oldBatch = mongoTemplate.findOne(batchQuery, DdMsgBatch.class);
        log.info("find the batch(tag={},status={}) return:{}", batchTag, "packing", oldBatch);
        // if the batch is not existed, create a new batch
        // if the batch is existed, append the dd message to the batch
        if (oldBatch == null) {
            DdMsgBatch batch = DdMsgBatch.builder()
                    .batchTag(batchTag)
                    .currentBatchAmount(ddMsg.getDdMsgAmount())
                    .currentBatchSize(1)
                    .status("packing")
                    .createdAt(Instant.now())
                    .expiredAt(Instant.now().plusSeconds(batchDdPackConfig.getExpiredSeconds(ddMsg)))
                    .batchMsgs(List.of(new DdMsgBatch.DbMsgOfBatch(ddMsg)))
                    .maxBatchSize(batchDdPackConfig.getMaxBatchSize(ddMsg))
                    .maxBatchAmount(batchDdPackConfig.getMaxBatchAmount(ddMsg))
                    .build();
            DdMsgBatch insertedDdMsgBatch = mongoTemplate.insert(batch);
            // if the batch is inserted, remove the dd message from the dd_msg collection
            if (insertedDdMsgBatch.getId() != null) {
                mongoTemplate.findAndRemove(new Query(Criteria.where("id").is(ddMsg.getId())
                        .and("status").is("packing")), DdMsg.class);
            }
            return;
        }

        if (oldBatch.getCurrentBatchAmount() + ddMsg.getDdMsgAmount() > oldBatch.getMaxBatchAmount() || Instant.now().isAfter(oldBatch.getExpiredAt())) {
            // if the batch is full, update the status of the batch to readyToSend
            Query batchFullQuery = new Query(Criteria.where("id").is(oldBatch.getId())
                    .and("status").is("packing"));
            Update batchFullUpdate = new Update().set("status", "readyToSend");
            mongoTemplate.updateMulti(batchFullQuery, batchFullUpdate, DdMsgBatch.class);
            packDdMsg2BatchTag(batchTag, ddMsg);
            return;
        }
        // append the dd message to the batch
        // if the batch is not full, append the dd message to the batch
        // if the batch is full, update the status of the batch to readyToSend
        Query batchAppendQuery = new Query(Criteria.where("id").is(oldBatch.getId())
                .and("status").is("packing")
                .and("currentBatchAmount").is(oldBatch.getCurrentBatchAmount())
                .and("currentBatchSize").is(oldBatch.getCurrentBatchSize())
                .and("maxBatchSize").gte(oldBatch.getCurrentBatchSize() + 1)
                .and("maxBatchAmount").gte(oldBatch.getCurrentBatchAmount() + ddMsg.getDdMsgAmount()));

        Update batchAppendUpdate = new Update().inc("currentBatchAmount", ddMsg.getDdMsgAmount())
                .inc("currentBatchSize", 1)
                .addToSet("batchMsgs", new DdMsgBatch.DbMsgOfBatch(ddMsg))
                .set("status", oldBatch.getCurrentBatchSize() + 1 == oldBatch.getMaxBatchSize() || oldBatch.getCurrentBatchAmount() + ddMsg.getDdMsgAmount() == oldBatch.getMaxBatchAmount() ? "readyToSend" : "packing");
        UpdateResult batchUpdateResult = mongoTemplate.updateMulti(batchAppendQuery, batchAppendUpdate, DdMsgBatch.class);
        // if the batch is updated, remove the dd message from the dd_msg collection
        if (batchUpdateResult.getModifiedCount() == 1) {
            // 3. 查询批次, 如果存在则发送
            mongoTemplate.findAndRemove(new Query(Criteria.where("id").is(ddMsg.getId())
                    .and("status").is("packing")), DdMsg.class);
        } else {
            packDdMsg2BatchTag(batchTag, ddMsg);
        }
    }


    public void restoreUnpackedDdMsg() {
        log.info("******====> start to restore the unpacked dd_msg collection");
        do {
            try {
                if (distributedLock.tryAcquireLock("restoreUnpackedDdMsg", distributedLock.getGlobalClientId(), Duration.ofSeconds(10000))) {
                    // find the dd_msg collection that status is packing
                    Query query = new Query(Criteria.where("status").is("packing")
                            .and("expiredAt").lt(Instant.now())).limit(1000);
                    List<DdMsg> ddMsgs = mongoTemplate.find(query, DdMsg.class);
                    log.info("*******====>>>> find the expired dd_msg collection that status is packing, return:{}", ddMsgs.size());
                    if (!ddMsgs.isEmpty()) {
                        for (DdMsg ddMsg : ddMsgs) {
                            String clientId = distributedLock.getThreadClientId();
                            try {
                                distributedLock.acquireLock(ddMsg.getBatchTag(), clientId, Duration.ofSeconds(10), Duration.ofSeconds(10));
                                packDdMsg2BatchTag(ddMsg.getBatchTag(), ddMsg);
                            } finally {
                                distributedLock.releaseLock(ddMsg.getBatchTag(), clientId);
                            }
                        }
                    }
                }
            } finally {
                distributedLock.releaseLock("restoreUnpackedDdMsg", distributedLock.getGlobalClientId());
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                log.error("sleep is interrupted...", e);
            }
        } while (true);
    }


}
