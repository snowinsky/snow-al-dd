package com.snow.al.dd.core.batch.pack;

import com.mongodb.client.result.UpdateResult;
import com.snow.al.dd.core.distributed.lock.DdLock;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class DdMsgBatchPackService {

    private final MongoTemplate mongoTemplate;
    private final DdLock distributedLock;
    private final BatchDdRequestExtractor batchDdRequestExtractor;

    public void startRestoreUnpackedDdMsg() {
        new Thread(this::restoreUnpackedDdMsg).start();
    }

    @Transactional
    public void pack(DdRequest ddRequest) {
        log.info("start to pack the ddRequest with ddMsgId:{}", ddRequest.getDdMsgId());
        //1, insert the dd message into the dd_msg collection
        DdMsg inserted = mongoTemplate.insert(ddRequest.newDdMsg());
        if (inserted.getId() == null) {
            throw new IllegalStateException("insert dd_msg fail:" + ddRequest.getDdMsgId());
        }
        // start to pack the dd message, first update the status of the dd message to packing
        var aa = mongoTemplate.findAndModify(
                new Query(Criteria.where("status").is("waitToPack").and("id").is(inserted.getId())),
                new Update().set("status", "packing"), DdMsg.class);
        Optional.ofNullable(aa).ifPresent(ddMsg -> {
            distributedLock.tryLock(ddMsg.getBatchTag(), Duration.ofSeconds(10), () -> {
                packDdMsg2BatchTag(ddMsg);
            });
        });
    }

    public void closeBatch(String batchId) {
        DdMsgBatch batch = mongoTemplate.findOne(new Query(Criteria.where("id").is(batchId).and("status").is("packing")), DdMsgBatch.class);
        if (batch == null) {
            return;
        }
        distributedLock.tryLock(batch.getBatchTag(), Duration.ofSeconds(10), () -> {
            mongoTemplate.findAndModify(new Query(Criteria.where("id").is(batchId).and("status").is("packing")),
                    new Update().set("status", "readyToReqFileGenerate"), DdMsgBatch.class);
        });
    }

    private void packDdMsg2BatchTag(DdMsg ddMsg) {
        String batchTag = ddMsg.getBatchTag();
        // find the specific batch by batchTag and status is packing
        DdMsgBatch oldBatch = mongoTemplate.findOne(new Query(Criteria.where("batchTag").is(batchTag)
                .and("status").is("packing")), DdMsgBatch.class);
        log.info("find the batch(tag={},status={}) return:{}", batchTag, "packing", oldBatch);
        // if the batch is not existed, create a new batch
        // if the batch is existed, append the dd message to the batch
        if (oldBatch == null) {
            var config = batchDdRequestExtractor.extract(ddMsg);
            DdMsgBatch batch = DdMsgBatch.builder()
                    .batchTag(batchTag)
                    .vendorCode(config.getVendorCode())
                    .bankCode(config.getBankCode())
                    .vendorFeedbackMode(config.getVendorFeedbackMode())
                    .currentBatchAmount(ddMsg.getDdMsgAmount())
                    .currentBatchSize(1)
                    .status("packing")
                    .createdAt(Instant.now())
                    .expiredAt(Instant.now().plusSeconds(config.getPackMaxBatchSize()))
                    .batchReqMsgList(List.of(new DdMsgBatch.DbMsgOfBatch(ddMsg)))
                    .maxBatchSize(config.getPackMaxBatchSize())
                    .maxBatchAmount(config.getPackMaxBatchAmount())
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
            // if the batch is full, update the status of the batch to readyToReqFileGenerate
            closeBatch(oldBatch.getId());
            packDdMsg2BatchTag(ddMsg);
            return;
        }
        // append the dd message to the batch
        // if the batch is not full, append the dd message to the batch
        // if the batch is full, update the status of the batch to readyToReqFileGenerate
        Query batchAppendQuery = new Query(Criteria.where("id").is(oldBatch.getId())
                .and("status").is("packing")
                .and("currentBatchAmount").is(oldBatch.getCurrentBatchAmount())
                .and("currentBatchSize").is(oldBatch.getCurrentBatchSize())
                .and("maxBatchSize").gte(oldBatch.getCurrentBatchSize() + 1)
                .and("maxBatchAmount").gte(oldBatch.getCurrentBatchAmount() + ddMsg.getDdMsgAmount()));

        Update batchAppendUpdate = new Update().inc("currentBatchAmount", ddMsg.getDdMsgAmount())
                .inc("currentBatchSize", 1)
                .addToSet("batchReqMsgList", new DdMsgBatch.DbMsgOfBatch(ddMsg))
                .set("status", oldBatch.getCurrentBatchSize() + 1 == oldBatch.getMaxBatchSize() || oldBatch.getCurrentBatchAmount() + ddMsg.getDdMsgAmount() == oldBatch.getMaxBatchAmount() ? "readyToReqFileGenerate" : "packing");
        UpdateResult batchUpdateResult = mongoTemplate.updateMulti(batchAppendQuery, batchAppendUpdate, DdMsgBatch.class);
        // if the batch is updated, remove the dd message from the dd_msg collection
        if (batchUpdateResult.getModifiedCount() == 1) {
            // 3. 查询批次, 如果存在则发送
            mongoTemplate.findAndRemove(new Query(Criteria.where("id").is(ddMsg.getId())
                    .and("status").is("packing")), DdMsg.class);
        } else {
            packDdMsg2BatchTag(ddMsg);
        }
    }


    public void restoreUnpackedDdMsg() {
        log.info("******====> start to restore the unpacked dd_msg collection");
        do {
            distributedLock.tryLock("restoreUnpackedDdMsg", Duration.ofSeconds(10), () -> {
                // find the dd_msg collection that status is packing
                List<DdMsg> ddMsgs = mongoTemplate.find(new Query(Criteria.where("status").is("packing")
                        .and("expiredAt").lt(Date.from(Instant.now()))).limit(1000), DdMsg.class);
                log.info("*******====>>>> find the expired dd_msg collection that status is packing, return:{}", ddMsgs.size());
                if (!ddMsgs.isEmpty()) {
                    for (DdMsg ddMsg : ddMsgs) {
                        distributedLock.tryLock(ddMsg.getBatchTag(), Duration.ofSeconds(10), () -> {
                            packDdMsg2BatchTag(ddMsg);
                        });
                    }
                }
            });
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("sleep is interrupted...", e);
            }
        } while (true);
    }


}
