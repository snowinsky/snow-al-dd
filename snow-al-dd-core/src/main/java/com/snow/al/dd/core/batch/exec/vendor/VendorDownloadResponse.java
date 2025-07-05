package com.snow.al.dd.core.batch.exec.vendor;

import com.snow.al.dd.core.batch.exec.state.DdBatchExecuteContext;
import com.snow.al.dd.core.mongo.model.db.DdMsgBatch;
import com.snow.al.dd.core.mongo.model.db.DdMsgBatchStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Getter
@Setter
public class VendorDownloadResponse extends VendorResponse {
    protected String pathResFile;
    protected DownloadNextStep downloadNextStep;

    public VendorDownloadResponse(String returnCode, String returnMsg) {
        super(returnCode, returnMsg);
    }

    public enum DownloadNextStep implements VendorResponseConsumer<VendorDownloadResponse> {
        QUERY {
            @Override
            public void accept(VendorDownloadResponse response, DdBatchExecuteContext context) {
                context.getMongoTemplate().findAndModify(
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_DOWNLOAD.getStatus())),
                        new Update().set("pathResFile", response.getPathResFile())
                                .set("status", DdMsgBatchStatus.READY_TO_VENDOR_QUERY.getStatus()),
                        DdMsgBatch.class);
                context.setState(DdMsgBatchStatus.READY_TO_VENDOR_QUERY.getState());
            }
        },
        SUCCESS {
            @Override
            public void accept(VendorDownloadResponse response, DdBatchExecuteContext context) {
                context.getMongoTemplate().findAndModify(
                        new Query(Criteria.where("id").is(context.getBatchId()).and("status").is(DdMsgBatchStatus.READY_TO_VENDOR_DOWNLOAD.getStatus())),
                        new Update().set("pathResFile", response.getPathResFile())
                                .set("status", DdMsgBatchStatus.READY_TO_RESFILE_PARSE.getStatus()),
                        DdMsgBatch.class);
                context.setState(DdMsgBatchStatus.READY_TO_RESFILE_PARSE.getState());
            }
        }
    }
}
