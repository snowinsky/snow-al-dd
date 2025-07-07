package com.snow.al.dd.application.mock.config;

import com.snow.al.dd.core.biztime.domain.SendTimeService;
import com.snow.al.dd.core.distributed.ratelimiter.DdRateLimiter;
import com.snow.al.dd.core.mongo.model.db.DdMsgSingle;
import com.snow.al.dd.core.single.exec.DdSingleExecutor;
import com.snow.al.dd.core.single.exec.DdSingleExecutorBuilder;
import com.snow.al.dd.core.single.exec.vendor.SingleQueryResponse;
import com.snow.al.dd.core.single.exec.vendor.SingleQueryResponse.QueryNextStep;
import com.snow.al.dd.core.single.exec.vendor.SingleSendResponse;
import com.snow.al.dd.core.single.exec.vendor.SingleSendResponse.SendNextStep;
import com.snow.al.dd.core.single.exec.vendor.VendorSingleExecuteAdapter;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import redis.clients.jedis.JedisPool;

import java.time.Duration;
import java.time.LocalDateTime;

@Configuration
public class DdSingleExecConfig {

    @Bean
    public DdSingleExecutor ddSingleExecutor(MongoTemplate mongoTemplate) {
        JedisPool jedisPool = new JedisPool("localhost", 6379);
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        org.redisson.api.RedissonClient redissonClient = org.redisson.Redisson.create(config);
        return new DdSingleExecutorBuilder().mongo(mongoTemplate)
                .redis(jedisPool, redissonClient)
                .vendor(new VendorSingleExecuteAdapter(){

                    @Override
                    public SingleSendResponse send(DdMsgSingle request) {
                        SingleSendResponse send = new SingleSendResponse("a", "adesc");
                        send.setMchTradeNo("sendmchtradeno");
                        send.setVendorTradeNo("sendvendortradeno");
                        send.setSendResLog("qwqwrwqrqwrrwq");
                        send.setDelayTime(Duration.ofSeconds(10));
                        send.setSendNextStep(SendNextStep.QUERY);
                        return send;
                    }

                    @Override
                    public SingleQueryResponse query(DdMsgSingle request) {
                        SingleQueryResponse query = new SingleQueryResponse("b", "bdesc");
                        query.setSuccessAmount(1231230L);
                        query.setQueryNextStep(QueryNextStep.FINAL);

                        return query;
                    }

                    @Override
                    public void notify(DdMsgSingle request) {

                    }

                    @Override
                    public DdRateLimiter getRateLimiter(DdMsgSingle batch) {
                        return null;
                    }

                    @Override
                    public SendTimeService getVendorBizTimeFacade(DdMsgSingle batch) {
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
                })
                .build();
    }
}
