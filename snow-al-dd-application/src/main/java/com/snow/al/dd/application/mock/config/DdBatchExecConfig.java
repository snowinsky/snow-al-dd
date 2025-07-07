package com.snow.al.dd.application.mock.config;
import com.snow.al.dd.core.batch.exec.vendor.VendorDownloadResponse.DownloadNextStep;
import com.snow.al.dd.core.batch.exec.vendor.VendorQueryResponse.QueryNextStep;
import com.snow.al.dd.core.batch.exec.vendor.VendorSendResponse.SendNextStep;

import com.snow.al.dd.core.batch.exec.DdBatchExecutor;
import com.snow.al.dd.core.batch.exec.DdBatchExecutorBuilder;
import com.snow.al.dd.core.batch.exec.vendor.*;
import com.snow.al.dd.core.biztime.domain.SendTimeService;
import com.snow.al.dd.core.distributed.ratelimiter.DdRateLimiter;
import com.snow.al.dd.core.distributed.ratelimiter.RedissonRateLimiter;
import com.snow.al.dd.core.mongo.db.DdMsgBatch;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.time.LocalDateTime;

@Configuration
public class DdBatchExecConfig {

    @Bean
    public DdBatchExecutor ddBatchExecutor(MongoTemplate mongoTemplate){
        JedisPool jedisPool = new JedisPool("localhost", 6379);
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        RedissonClient redissonClient = org.redisson.Redisson.create(config);

        return new DdBatchExecutorBuilder()
                .jedisPool(jedisPool, redissonClient)
                .mongoTemplate(mongoTemplate)
                .vendorExecuteAdapter(new VendorExecuteAdapter() {
                    @Override
                    public void generateReqFile(DdMsgBatch batch) {
                        batch.setBatchName(System.currentTimeMillis() + ".txt");
                        batch.setPathReqFile("/user/aa/" + batch.getBatchName());
                    }

                    @Override
                    public void parseResFile(DdMsgBatch batch) {
                        batch.setPathResFile("/user/aa/res/" + batch.getBatchName());
                    }

                    @Override
                    public void notifyResFile(DdMsgBatch batch) {
                        System.out.println(batch);
                    }

                    @Override
                    public DdRateLimiter getRateLimiter(DdMsgBatch batch) {
                        return new RedissonRateLimiter(redissonClient,
                                batch.getVendorCode(), 1, Duration.ofSeconds(5));
                    }

                    @Override
                    public SendTimeService getVendorBizTimeFacade(DdMsgBatch batch) {
                        return new SendTimeService() {
                            @Override
                            public void refreshVendor(String vendorCode) {

                            }

                            @Override
                            public boolean isBizTime(String vendorCode, String bankCode, LocalDateTime inputDateTime) {
                                return true;
                            }

                            @Override
                            public boolean hasBizTimeInFuture(String vendorCode, String bankCode, LocalDateTime inputDateTime) {
                                return false;
                            }

                            @Override
                            public LocalDateTime getLatestBizTime(String vendorCode, String bankCode, LocalDateTime inputDateTime) {
                                return null;
                            }

                            @Override
                            public LocalDateTime getLatestMaintenanceTime(String vendorCode, String bankCode, LocalDateTime inputDateTime) {
                                return null;
                            }
                        };
                    }

                    @Override
                    public VendorSendResponse send(VendorRequest<VendorSendResponse> request) {
                        VendorSendResponse response = new VendorSendResponse("aa", "bb");
                        response.setMchTradeNo("mch" + System.currentTimeMillis());
                        response.setVendorTradeNo("ven" + System.currentTimeMillis());
                        response.setSendResLog("{log from bank}");
                        response.setNextStepDelayInterval(Duration.ofSeconds(20));
                        response.setSendNextStep(SendNextStep.QUERY);
                        response.setReturnCode("0000");
                        response.setReturnMsg("success");

                        return response;
                    }

                    @Override
                    public VendorQueryResponse query(VendorRequest<VendorQueryResponse> request) {
                        VendorQueryResponse response = new VendorQueryResponse("cc", "dd");
                        response.setMchTradeNo("");
                        response.setVendorTradeNo("");
                        response.setTradeStatus("");
                        response.setTradeStatusDesc("");
                        response.setQueryResLog("query log");
                        response.setQueryNextStep(QueryNextStep.READY_TO_DOWNLOAD);
                        response.setReturnCode("");
                        response.setReturnMsg("");

                        return response;
                    }

                    @Override
                    public VendorDownloadResponse download(VendorRequest<VendorDownloadResponse> request) {
                        VendorDownloadResponse response = new VendorDownloadResponse("ee", "ff");
                        response.setPathResFile("/user/aa/res/aaaaa.txt.res" );
                        response.setDownloadNextStep(DownloadNextStep.SUCCESS);
                        response.setReturnCode("");
                        response.setReturnMsg("");


                        return response;
                    }


                }).build();
    }
}
