package com.snow.al.dd.core.biztime;

import com.google.common.base.Splitter;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.snow.al.dd.core.biztime.db.VendorBizTime;
import com.snow.al.dd.core.biztime.domain.DateTimeRange;
import com.snow.al.dd.core.biztime.domain.SendTimeManager;
import com.snow.al.dd.core.biztime.domain.SendTimeService;
import com.snow.al.dd.core.biztime.domain.TimeRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("all")
@RequiredArgsConstructor
public class VendorBizTimeFacade implements SendTimeService {

    private final boolean enableCache;
    private final MongoTemplate mongoTemplate;


    private LoadingCache<String, VendorBizTime> vendorInfoCache = CacheBuilder.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(20, TimeUnit.MINUTES)
            .build(new CacheLoader<String, VendorBizTime>() {
                @Override
                public VendorBizTime load(String vendorCode) {
                    return getVendorInfoDirect(vendorCode);
                }
            });


    protected VendorBizTime getVendorInfoDirect(String vendorCode) {
        return mongoTemplate.findOne(new Query(Criteria.where("vendorCode").is(vendorCode)), VendorBizTime.class);
    }

    /**
     * 刷新缓存，保证其获取最新的终端值而不是缓存值
     *
     * @param vendorCode
     */
    @Override
    public void refreshVendor(String vendorCode) {
        vendorInfoCache.refresh(vendorCode);
    }

    /**
     * 暂时宗旨是尽量发送，不论是网络原因还是数据不全，都尽量发送一次。因此，获取不到vendor信息时，也发送
     *
     * @param vendorCode
     * @param bankCode
     * @return
     */
    @Override
    public boolean isBizTime(String vendorCode, String bankCode, LocalDateTime inputDateTime) {
        try {
            List<VendorBizTime.BankBizTime> supportedBanks = Optional.ofNullable(getVendorInfo(vendorCode)).map(VendorBizTime::getBankBizTimeList).orElse(Collections.emptyList());
            log.info("business: isBizTime? get bank info by vendorCode={}, bankCode={}, dateTime={} then return {}", vendorCode, bankCode, inputDateTime, supportedBanks);
            for (VendorBizTime.BankBizTime bank : supportedBanks) {
                if (bank.getBankCode().equalsIgnoreCase(bankCode)) {
                    log.info("business: isBizTime? found vendor and bank info. vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDateTime);
                    boolean canSend = canSend(bank.getBizTime(), bank.getMaintenanceTimeIgnored(), bank.getMaintenanceTime(), inputDateTime);
                    log.info("business: isBizTime? {}", canSend);
                    return canSend;
                }
            }
            log.info("business: isBizTime? cannot found any vendor or bank info. return true. vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDateTime);
            return true;
        } catch (Exception e) {
            log.warn("business: isBizTime? vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDateTime, e);
            return true;
        }
    }

    private VendorBizTime getVendorInfo(String vendorCode) {
        return enableCache ? vendorInfoCache.getUnchecked(vendorCode) : getVendorInfoDirect(vendorCode);
    }


    @Override
    public boolean hasBizTimeInFuture(String vendorCode, String bankCode, LocalDateTime inputDateTime) {
        try {
            List<VendorBizTime.BankBizTime> supportedBanks = Optional.ofNullable(getVendorInfo(vendorCode)).map(VendorBizTime::getBankBizTimeList).orElse(Collections.emptyList());
            log.info("business: hasBizTimeInFuture? get bank info by vendorCode={}, bankCode={}, dateTime={} then return {}", vendorCode, bankCode, inputDateTime, supportedBanks);
            for (VendorBizTime.BankBizTime bank : supportedBanks) {
                if (bank.getBankCode().equalsIgnoreCase(bankCode)) {
                    log.info("business: hasBizTimeInFuture? found vendor and bank info. vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDateTime);
                    boolean hasFutureBusinessTime = hasBusinessTime(bank.getBizTime(), bank.getMaintenanceTimeIgnored(), bank.getMaintenanceTime(), inputDateTime);
                    log.info("business: hasBizTimeInFuture? {}", hasFutureBusinessTime);
                    return hasFutureBusinessTime;
                }
            }
            log.info("business: hasBizTimeInFuture? cannot found any vendor or bank info. return true. vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDateTime);
            return true;
        } catch (Exception e) {
            log.warn("business: hasBizTimeInFuture? vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDateTime, e);
            return true;
        }
    }

    @Override
    public LocalDateTime getLatestBizTime(String vendorCode, String bankCode, LocalDateTime inputDateTime) {
        try {
            List<VendorBizTime.BankBizTime> supportedBanks = Optional.ofNullable(getVendorInfo(vendorCode)).map(VendorBizTime::getBankBizTimeList).orElse(Collections.emptyList());
            log.info("business: getLatestBizTime? get bank info by vendorCode={}, bankCode={}, dateTime={} then return {}", vendorCode, bankCode, inputDateTime, supportedBanks);
            for (VendorBizTime.BankBizTime bank : supportedBanks) {
                if (bank.getBankCode().equalsIgnoreCase(bankCode)) {
                    log.info("business: getLatestBizTime? found vendor and bank info. vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDateTime);
                    SendTimeManager mgmt = buildSendTimeManager(bank.getBizTime(), bank.getMaintenanceTimeIgnored(), bank.getMaintenanceTime());
                    return mgmt.getFirstBusinessDateTime(inputDateTime);
                }
            }
            log.info("business: getLatestBizTime? cannot found any vendor or bank info. return true. vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDateTime);
            return LocalDateTime.now();
        } catch (Exception e) {
            log.warn("business: getLatestBizTime? vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDateTime, e);
            return LocalDateTime.now();
        }
    }

    @Override
    public LocalDateTime getLatestMaintenanceTime(String vendorCode, String bankCode, LocalDateTime inputDateTime) {
        try {
            List<VendorBizTime.BankBizTime> supportedBanks = Optional.ofNullable(getVendorInfo(vendorCode)).map(VendorBizTime::getBankBizTimeList).orElse(Collections.emptyList());
            log.info("business: getLatestMaintTime? get bank info by vendorCode={}, bankCode={}, dateTime={} then return {}", vendorCode, bankCode, inputDateTime, supportedBanks);
            for (VendorBizTime.BankBizTime bank : supportedBanks) {
                if (bank.getBankCode().equalsIgnoreCase(bankCode)) {
                    log.info("business: getLatestMaintTime? found vendor and bank info. vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDateTime);
                    SendTimeManager mgmt = buildSendTimeManager(bank.getBizTime(), bank.getMaintenanceTimeIgnored(), bank.getMaintenanceTime());
                    return mgmt.getFirstMaintenanceDateTime(inputDateTime);
                }
            }
            log.info("business: getLatestMaintTime? cannot found any vendor or bank info. return true. vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDateTime);
            return null;
        } catch (Exception e) {
            log.warn("business: getLatestMaintTime? vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDateTime, e);
            return null;
        }
    }

    @Override
    public SortedSet<DateTimeRange> getCanSendDateTimeRanges(String vendorCode, String bankCode, LocalDate inputDate) {
        SortedSet<DateTimeRange> defaultOne = new TreeSet<>(Comparator.comparing(DateTimeRange::getStart));
        defaultOne.add(new DateTimeRange(LocalDateTime.of(inputDate, LocalTime.of(0, 0, 0)), LocalDateTime.of(inputDate, LocalTime.of(23, 59, 59))));
        try {
            List<VendorBizTime.BankBizTime> supportedBanks = Optional.ofNullable(getVendorInfo(vendorCode)).map(VendorBizTime::getBankBizTimeList).orElse(Collections.emptyList());
            log.info("business: getCanSendDateTimeRanges? get bank info by vendorCode={}, bankCode={}, dateTime={} then return {}", vendorCode, bankCode, inputDate, supportedBanks);
            for (VendorBizTime.BankBizTime bank : supportedBanks) {
                if (bank.getBankCode().equalsIgnoreCase(bankCode)) {
                    log.info("business: getCanSendDateTimeRanges? found vendor and bank info. vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDate);
                    SendTimeManager mgmt = buildSendTimeManager(bank.getBizTime(), bank.getMaintenanceTimeIgnored(), bank.getMaintenanceTime());
                    return mgmt.getCanSendDateTimeRanges(inputDate);
                }
            }
            log.info("business: getCanSendDateTimeRanges? cannot found any vendor or bank info. return true. vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDate);
            return defaultOne;
        } catch (Exception e) {
            log.warn("business: getCanSendDateTimeRanges? vendorCode={}, bankCode={}, dateTime={}", vendorCode, bankCode, inputDate, e);
            return defaultOne;
        }
    }

    private boolean hasBusinessTime(String businessTime, int maintenanceStatus, String maintainenceTime, LocalDateTime inputDateTime) {
        SendTimeManager mgmt = buildSendTimeManager(businessTime, maintenanceStatus, maintainenceTime);
        return mgmt.getFirstBusinessDateTime(inputDateTime) != null;
    }

    private boolean canSend(String businessTime, int maintenanceStatus, String maintainenceTime, LocalDateTime inputDateTime) {
        SendTimeManager mgmt = buildSendTimeManager(businessTime, maintenanceStatus, maintainenceTime);
        return mgmt.canSend(inputDateTime);
    }

    private SendTimeManager buildSendTimeManager(String businessTime, int maintenanceStatus, String maintainenceTime) {
        boolean ignoreMaintTime = maintenanceStatus == 1;
        Set<TimeRange> timeRanges = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(businessTime == null ? "" : businessTime).stream().map(TimeRange::new).collect(Collectors.toSet());
        Set<DateTimeRange> dateTimeRanges = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(maintainenceTime == null ? "" : maintainenceTime).stream().map(DateTimeRange::new).collect(Collectors.toSet());
        return new SendTimeManager(timeRanges, dateTimeRanges, ignoreMaintTime);
    }
}
