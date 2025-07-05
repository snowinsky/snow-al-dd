package com.snow.al.dd.core.biztime.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.List;

import static java.time.temporal.ChronoField.*;

@Getter
@EqualsAndHashCode
public class TimeRange {

    private static final DateTimeFormatter FORMATTER = new DateTimeFormatterBuilder()
            .appendValue(HOUR_OF_DAY, 1, 2, SignStyle.NEVER)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .optionalStart()
            .appendFraction(NANO_OF_SECOND, 0, 9, true).toFormatter();
    private static final List<Character> SEPARATORS = List.of('-', '~');
    private final LocalTime start;
    private final LocalTime end;

    public TimeRange(LocalTime start, LocalTime end) {
        this.start = start;
        this.end = end;
    }

    public TimeRange(String startString, String endString) {
        this(LocalTime.parse(startString, FORMATTER), LocalTime.parse(endString, FORMATTER));
    }

    public TimeRange(String startSeparatorEnd) {
        this(startSeparatorEnd, SEPARATORS.stream().filter(a -> startSeparatorEnd.contains(a.toString())).findFirst().orElseThrow(() -> new IllegalArgumentException("separator is not - or ~")));
    }

    public TimeRange(String startSeparatorEnd, char separator) {
        String[] startAndEnd = startSeparatorEnd.split(String.valueOf(separator));
        this.start = LocalTime.parse(startAndEnd[0], FORMATTER);
        this.end = LocalTime.parse(startAndEnd[1], FORMATTER);
    }

    public boolean inRange(LocalDateTime input) {
        LocalTime inputTime = input.toLocalTime();
        if (inputTime.equals(start) || inputTime.equals(end)) {
            return true;
        }
        return inputTime.isAfter(start) && inputTime.isBefore(end);
    }

    public boolean isBeforeRange(LocalDateTime input) {
        LocalTime inputTime = input.toLocalTime();
        return inputTime.isBefore(start);
    }

    public boolean isAfterRange(LocalDateTime input) {
        LocalTime inputTime = input.toLocalTime();
        return inputTime.isAfter(end);
    }

    @Override
    public String toString() {
        return start + "~" + end;
    }

}
