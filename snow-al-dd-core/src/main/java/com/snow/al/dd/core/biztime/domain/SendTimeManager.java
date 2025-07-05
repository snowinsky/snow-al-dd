package com.snow.al.dd.core.biztime.domain;

import com.google.common.collect.Sets;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author
 */
public class SendTimeManager {

    private final SortedSet<TimeRange> businessTimes;
    private final SortedSet<DateTimeRange> maintenanceDateTimes;
    private final boolean ignoreMaintenanceDateTimes;

    /**
     * 构造函数，如果传入的集合是null，设定默认值
     * 例如：传入的营业时间为空时，则设定为从0点到23点59分都是营业时间
     * 传入的维护时间为null或者为空时，则设定为没有维护时间
     *
     * @param businessTimes
     * @param maintenanceDateTimes
     * @param ignoreMaintenanceDateTimes
     */
    public SendTimeManager(Set<TimeRange> businessTimes, Set<DateTimeRange> maintenanceDateTimes, boolean ignoreMaintenanceDateTimes) {
        if (null == businessTimes || businessTimes.isEmpty()) {
            this.businessTimes = new TreeSet<>(Comparator.comparing(TimeRange::getStart));
            this.businessTimes.add(new TimeRange(LocalTime.of(0, 0, 0), LocalTime.of(23, 59, 59)));
        } else {
            this.businessTimes = new TreeSet<>(Comparator.comparing(TimeRange::getStart));
            this.businessTimes.addAll(businessTimes);
        }
        if (null == maintenanceDateTimes || maintenanceDateTimes.isEmpty()) {
            this.maintenanceDateTimes = new TreeSet<>();
        } else {
            this.maintenanceDateTimes = new TreeSet<>(Comparator.comparing(DateTimeRange::getStart));
            this.maintenanceDateTimes.addAll(maintenanceDateTimes);
        }
        this.ignoreMaintenanceDateTimes = ignoreMaintenanceDateTimes;
    }

    /**
     * 获取两个时间间隔集合的差集，例如营业时间段-维护时间段=可用的营业时间段
     *
     * @param source
     * @param target
     * @return
     */
    protected static SortedSet<DateTimeRange> difference(Set<DateTimeRange> source, Set<DateTimeRange> target) {
        Set<DateTimeRange> minus = splitByRangeSet(source, target);
        Set<DateTimeRange> minute = splitByRangeSet(target, source);
        Set<DateTimeRange> diff = Sets.difference(minus, minute).immutableCopy();
        return reduceDateTimeRange(diff);
    }

    /**
     * 将相交的时间段合并成一个
     *
     * @param diff
     * @return
     */
    protected static SortedSet<DateTimeRange> reduceDateTimeRange(Set<DateTimeRange> diff) {
        SortedSet<DateTimeRange> result = new TreeSet<>(Comparator.comparing(DateTimeRange::getStart));
        result.addAll(diff);
        SortedSet<DateTimeRange> reduceResult = new TreeSet<>(Comparator.comparing(DateTimeRange::getStart));
        for (DateTimeRange a : result) {
            if (reduceResult.isEmpty()) {
                reduceResult.add(a);
                continue;
            }
            DateTimeRange last = reduceResult.last();
            if (last.getEnd().equals(a.getStart()) || last.getEnd().isAfter(a.getStart())) {
                reduceResult.remove(last);
                reduceResult.add(new DateTimeRange(last.getStart(), a.getEnd()));
            } else {
                reduceResult.add(a);
            }
        }
        return reduceResult;
    }

    /**
     * 将source集合中的时间段，根据target集合中的时间段的时间点进行切割，切割成多个细小的时间段集合
     * 以便于后边做集合的差集
     *
     * @param master
     * @param branch
     * @return
     */
    protected static Set<DateTimeRange> splitByRangeSet(Set<DateTimeRange> master, Set<DateTimeRange> branch) {
        Set<DateTimeRange> minus = new HashSet<>();
        master.forEach(a -> minus.addAll(splitByRangeSet(a, branch)));
        return minus;
    }

    /**
     * 将一个时间段，分割成多个细小的时间段集合。
     *
     * @param source 需要分割的时间段
     * @param target 用于分割时间段的时间段集合，当两个有交集时才会被分割
     * @return
     */
    protected static Set<DateTimeRange> splitByRangeSet(DateTimeRange source, Set<DateTimeRange> target) {
        Set<LocalDateTime> s = new TreeSet<>();
        s.add(source.getStart());
        s.add(source.getEnd());
        target.forEach(a -> {
            if (a.getStart().isAfter(source.getStart()) && a.getStart().isBefore(source.getEnd())) {
                s.add(a.getStart());
            }
            if (a.getEnd().isAfter(source.getStart()) && a.getEnd().isBefore(source.getEnd())) {
                s.add(a.getEnd());
            }
        });
        List<LocalDateTime> l = new ArrayList<>(s);
        LinkedList<LocalDateTime> m = new LinkedList<>(s);
        m.pollFirst();
        Set<DateTimeRange> result = new HashSet<>();
        for (int i = 0; i < m.size(); i++) {
            result.add(new DateTimeRange(l.get(i), m.get(i)));
        }
        return result;
    }

    /**
     * 获取输入的年月日下可以用于发送的营业时间段
     *
     * @param anyDate
     * @return
     */
    public SortedSet<DateTimeRange> getCanSendDateTimeRanges(LocalDate anyDate) {
        //给营业时间段填上年月日，获得该天的名义上的营业时间段
        SortedSet<DateTimeRange> sortedOriginalBusinessRanges = getBusinessDateTimeRanges(anyDate);
        //拿到营业时间的最后截至时间，或者叫下班时间
        LocalDateTime lastBusinessTime = sortedOriginalBusinessRanges.last().getEnd();
        //获取有效的维护时间：如果ignoreMaintenanceDateTimes=false，所有的维护时间都有效，ignoreMaintenanceDateTimes=true时，只有覆盖了最后营业时间截至时间的那段维护时间才有效。
        SortedSet<DateTimeRange> sortedOriginalMaintainRanges = getValidMaintainDateTimeRanges(lastBusinessTime);
        return difference(sortedOriginalBusinessRanges, sortedOriginalMaintainRanges);

    }

    /**
     * 给营业时间设定上日期，使其成为某一天的具体的营业日期时间段
     *
     * @param anyDate
     * @return
     */
    protected SortedSet<DateTimeRange> getBusinessDateTimeRanges(LocalDate anyDate) {
        Set<DateTimeRange> originalBusinessRanges = this.businessTimes.stream().map(a -> new DateTimeRange(LocalDateTime.of(anyDate, a.getStart()), LocalDateTime.of(anyDate, a.getEnd()))).collect(Collectors.toSet());
        SortedSet<DateTimeRange> sortedOriginalBusinessRanges = new TreeSet<>(Comparator.comparing(DateTimeRange::getStart));
        sortedOriginalBusinessRanges.addAll(originalBusinessRanges);
        return sortedOriginalBusinessRanges;
    }

    /**
     * 获取有效的维护时间：如果ignoreMaintenanceDateTimes=false，所有的维护时间都有效，ignoreMaintenanceDateTimes=true时，只有覆盖了最后营业时间截至时间的那段维护时间才有效。
     *
     * @param lastBusinessTime 名义上的营业时间的最后一个时间点
     * @return
     */
    protected SortedSet<DateTimeRange> getValidMaintainDateTimeRanges(LocalDateTime lastBusinessTime) {
        SortedSet<DateTimeRange> validMaintenanceDateTimeRanges = new TreeSet<>(Comparator.comparing(DateTimeRange::getStart));
        if (this.ignoreMaintenanceDateTimes) {
            for (DateTimeRange a : this.maintenanceDateTimes) {
                if (a.inRange(lastBusinessTime)) {
                    validMaintenanceDateTimeRanges.add(a);
                    return validMaintenanceDateTimeRanges;
                }
            }
        } else {
            validMaintenanceDateTimeRanges.addAll(this.maintenanceDateTimes);
        }
        return validMaintenanceDateTimeRanges;
    }


    /**
     * 判断当前时间是否可以发送：
     * 忽略维护时间时，如果营业时间截止时间被维护时间盖住，则不能忽略这一段维护时间
     * 不忽略维护时间时，需要当前时间不在维护时间段内，且在营业时间段内
     *
     * @param input
     * @return
     */
    public boolean canSend(LocalDateTime input) {
        return getCanSendDateTimeRanges(input.toLocalDate()).stream().anyMatch(a -> a.inRange(input));
    }

    /**
     * 即使设置了可以忽略维护时间，也需要考虑当天的最后一段营业时间是否在维护期内。如果在就需要将该段维护期考虑进去。
     * Business time: 7:00-12:00 14:00-15:00 16:00-20:00
     * Maintenance time: 6:00-8:00 14:50-21:00
     * Business type =Yes, business available time is: 7:00-12:00 14:00-14:50
     * Business type =No, business available time is: 8:00-12:00 14:00-14:50
     *
     * @param input
     * @return
     */
    protected boolean canSendIgnoreMaintenanceDateTimeWithLastMaintenTime(LocalDateTime input) {
        if (null == maintenanceDateTimes || maintenanceDateTimes.isEmpty()) {
            return inBusinessTime(businessTimes, input);
        }
        LocalDateTime lastBusinessDateTime;
        if (null == businessTimes || businessTimes.isEmpty()) {
            lastBusinessDateTime = LocalDateTime.of(input.toLocalDate(), LocalTime.of(23, 59, 59));
        } else {
            lastBusinessDateTime = LocalDateTime.of(input.toLocalDate(), businessTimes.last().getEnd());
        }
        for (DateTimeRange dtr : maintenanceDateTimes) {
            if (dtr.inRange(lastBusinessDateTime)) {
                return !dtr.inRange(input) && inBusinessTime(businessTimes, input);
            }
        }
        return inBusinessTime(businessTimes, input);
    }

    /**
     * 没有设置维护时间，则任务无维护时间，设置了，则按维护时间判断是否在维护时间范围内
     *
     * @param maintenanceDateTimes
     * @param input
     * @return
     */
    protected boolean inMaintenanceDateTime(Set<DateTimeRange> maintenanceDateTimes, LocalDateTime input) {
        if (null == maintenanceDateTimes || maintenanceDateTimes.isEmpty()) {
            return false;
        }
        for (DateTimeRange dt : maintenanceDateTimes) {
            if (dt.inRange(input)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 没设置营业时间，则默认全天都营业，设置了，则按营业时间来计算
     *
     * @param businessTimes
     * @param input
     * @return
     */
    protected boolean inBusinessTime(Set<TimeRange> businessTimes, LocalDateTime input) {
        if (null == businessTimes || businessTimes.isEmpty()) {
            return true;
        }
        for (TimeRange t : businessTimes) {
            if (t.inRange(input)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取离输入时间最近的营业时间，如果输入时间就是营业时间则直接返回。
     * 如果正在维护期，则考虑跨过该维护期然后寻找下一个营业时间。
     * 如果跨天，则返回null。
     *
     * @param input
     * @return
     */
    public LocalDateTime getFirstBusinessDateTime(LocalDateTime input) {
        if (canSend(input)) {
            return input;
        }
        SortedSet<DateTimeRange> canSendDateTimeRanges = getCanSendDateTimeRanges(input.toLocalDate());
        for (DateTimeRange aa : canSendDateTimeRanges) {
            if (aa.isBeforeRange(input)) {
                return aa.getStart();
            }
        }
        return null;
    }


    /**
     * 获取离输入时间最近的维护时间，如果输入时间就是维护时间则直接返回。
     * 如果正在营业，则考虑跨过营业时间考虑下一个无法营业的时间，并不一定是维护时间。
     * 如果没有则返回null。
     *
     * @param input
     * @return
     */
    public LocalDateTime getFirstMaintenanceDateTime(LocalDateTime input) {
        if (!canSend(input)) {
            return input;
        }
        LocalDateTime ldt = getCanSendDateTimeRanges(input.toLocalDate()).stream().filter(a -> a.inRange(input)).findFirst().map(DateTimeRange::getEnd).orElse(null);
        if (null == ldt || ldt.toLocalTime().equals(LocalTime.of(23, 59, 59))) {
            return null;
        }
        return ldt;
    }
}
