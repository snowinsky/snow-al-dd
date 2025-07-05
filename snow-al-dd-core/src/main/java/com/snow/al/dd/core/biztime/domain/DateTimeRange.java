package com.snow.al.dd.core.biztime.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoField.*;

@Getter
@EqualsAndHashCode
public class DateTimeRange {

    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 1, 2, SignStyle.NEVER)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .optionalStart()
            .appendFraction(NANO_OF_SECOND, 0, 9, true).toFormatter();

    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral('/')
            .appendValue(MONTH_OF_YEAR, 2)
            .appendLiteral('/')
            .appendValue(DAY_OF_MONTH, 2).appendLiteral(' ').append(TIME_FORMATTER).toFormatter();
    private static final List<Character> DATE_SEP = List.of('/', '-');
    private static final List<Character> SEPARATORS = List.of('-', '~');
    private final LocalDateTime start;
    private final LocalDateTime end;

    public DateTimeRange(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    public DateTimeRange(String startString, String endString) {
        this(LocalDateTime.parse(startString, DATE_TIME_FORMATTER), LocalDateTime.parse(endString, DATE_TIME_FORMATTER));
    }

    public DateTimeRange(String startStringSeperateEndString, char separator) {
        String[] a = startStringSeperateEndString.split(String.valueOf(separator));
        if (a[0].contains(":")) {
            DateTimeFormatter dateTimeFormatter = getDateTimeFormatter(DATE_SEP.stream().filter(e -> a[0].contains(e.toString())).findFirst().orElseThrow(() -> new IllegalArgumentException("separator is not / or -")));
            this.start = LocalDateTime.parse(a[0], dateTimeFormatter);
            this.end = LocalDateTime.parse(a[1], dateTimeFormatter);
        } else {
            this.start = toDateTime(Long.parseLong(a[0]));
            this.end = toDateTime(Long.parseLong(a[1]));
        }
    }

    public DateTimeRange(String startStringSeperateEndString) {
        this(startStringSeperateEndString, SEPARATORS.stream().filter(a -> startStringSeperateEndString.contains(a.toString())).findFirst().orElseThrow(() -> new IllegalArgumentException("separator is not - or ~")));
    }

    private DateTimeFormatter getDateTimeFormatter(char dateSep) {
        return new DateTimeFormatterBuilder()
                .appendValue(YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
                .appendLiteral(dateSep)
                .appendValue(MONTH_OF_YEAR, 2)
                .appendLiteral(dateSep)
                .appendValue(DAY_OF_MONTH, 2).appendLiteral(' ').append(TIME_FORMATTER).toFormatter();
    }

    private LocalDateTime toDateTime(long timeMillis) {
        return LocalDateTime.ofEpochSecond(timeMillis / 1000, 0, ZoneOffset.ofHours(8));
    }

    public boolean inRange(LocalDateTime input) {
        if (input.equals(start) || input.equals(end)) {
            return true;
        }
        return input.isAfter(start) && input.isBefore(end);
    }

    public boolean isBeforeRange(LocalDateTime input) {
        return input.isBefore(start);
    }

    public boolean isAfterRange(LocalDateTime input) {
        return input.isAfter(end);
    }

    public SortedSet<DateTimeRange> minus(DateTimeRange input) {
        TreeSet<DateTimeRange> s = new TreeSet<>(Comparator.comparing(DateTimeRange::getStart));
        if (input.getEnd().isBefore(start) || input.getStart().isAfter(end) || input.getEnd().equals(start) || input.getStart().equals(end)) {
            s.add(this);
            return s;
        }
        if ((input.getStart().isBefore(start) || input.getStart().equals(start)) && input.getEnd().isAfter(start) && input.getEnd().isBefore(end)) {
            s.add(new DateTimeRange(input.getEnd(), end));
            return s;
        }
        if (input.getStart().isAfter(start) && (input.getEnd().isAfter(end) || input.getEnd().equals(end))) {
            s.add(new DateTimeRange(start, input.getStart()));
            return s;
        }
        if (input.getStart().isBefore(start) && input.getEnd().isAfter(end) ||
                input.getStart().equals(start) && input.getEnd().equals(end)) {
            return new TreeSet<>();
        }
        if (input.getStart().isAfter(start) && input.getEnd().isBefore(end)) {
            s.add(new DateTimeRange(start, input.getStart()));
            s.add(new DateTimeRange(input.getEnd(), end));
            return s;
        }
        throw new IllegalArgumentException("minus fail. input=" + input + ", current=" + this);
    }

    public Set<DateTimeRange> difference(DateTimeRange s) {
        if (null == s || s.getStart().isAfter(this.getEnd()) || s.getEnd().isBefore(this.getStart())) {
            return Set.of(this);
        }
        Set<DateTimeRange> ss = new HashSet<>();
        ss.add(new DateTimeRange(this.getStart(), s.getStart()));
        ss.add(new DateTimeRange(s.getEnd(), this.getEnd()));
        return ss.stream().filter(a -> a.getStart().isBefore(a.getEnd())).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return start + "~" + end;
    }

}
