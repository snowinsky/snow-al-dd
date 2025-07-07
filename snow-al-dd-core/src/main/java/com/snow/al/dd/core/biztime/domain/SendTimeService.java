package com.snow.al.dd.core.biztime.domain;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

public interface SendTimeService {

    void refreshVendor(String vendorCode);

    boolean isBizTime(String vendorCode, String bankCode, LocalDateTime inputDateTime);

    boolean hasBizTimeInFuture(String vendorCode, String bankCode, LocalDateTime inputDateTime);

    LocalDateTime getLatestBizTime(String vendorCode, String bankCode, LocalDateTime inputDateTime);

    LocalDateTime getLatestMaintenanceTime(String vendorCode, String bankCode, LocalDateTime inputDateTime);

    default SortedSet<DateTimeRange> getCanSendDateTimeRanges(String vendorCode, String bankCode, LocalDate inputDate) {
        SortedSet<DateTimeRange> defaultOne = new TreeSet<>(Comparator.comparing(DateTimeRange::getStart));
        defaultOne.add(new DateTimeRange(LocalDateTime.of(inputDate, LocalTime.of(0, 0, 0)), LocalDateTime.of(inputDate, LocalTime.of(23, 59, 59))));
        return defaultOne;
    }

    default void executeInActiveBizTime(String vendorCode, String bankCode, Runnable runInBizTime, Consumer<LocalDateTime> delayRunner, Runnable reject) {
        if (isBizTime(vendorCode, bankCode, LocalDateTime.now())) {
            runInBizTime.run();
            return;
        }
        if (hasBizTimeInFuture(vendorCode, bankCode, LocalDateTime.now())) {
            LocalDateTime latestBizTime = getLatestBizTime(vendorCode, bankCode, LocalDateTime.now());
            delayRunner.accept(latestBizTime);
            return;
        }
        reject.run();
    }

}
