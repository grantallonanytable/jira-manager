package ru.shadewallcorp.jiraTasks.jiraManager.date;

/**
 * Значения для рабочего дня
 *
 */
public enum DateStatus {
    WEEKDAY_WORK_TIME(true, true, false, false),
    WEEKDAY_BEFORE_WORK_TIME(true, false, true, false),
    WEEKDAY_AFTER_WORK_TIME(true, false, false, true),
    HOLIDAY_WORK_TIME(false, true, false, false),
    HOLIDAY_BEFORE_WORK_TIME(false, false, true, false),
    HOLIDAY_AFTER_WORK_TIME(false, false, false, true);

    private final boolean isWeekday;
    private final boolean isWorkTime;
    private final boolean isBeforeWorkTime;
    private final boolean isAfterWorkTime;

    public boolean isWeekday() {
        return isWeekday;
    }

    public boolean isWorkTime() {
        return isWorkTime;
    }

    public boolean isBeforeWorkTime() {
        return isBeforeWorkTime;
    }

    public boolean isAfterWorkTime() {
        return isAfterWorkTime;
    }

    DateStatus(boolean isWeekday, boolean isWorkTime, boolean isBeforeWorkTime, boolean isAfterWorkTime) {
        this.isWeekday = isWeekday;
        this.isWorkTime = isWorkTime;
        this.isBeforeWorkTime = isBeforeWorkTime;
        this.isAfterWorkTime = isAfterWorkTime;
    }

    public static DateStatus getDateStatus(String dateStatusName) {
        for (DateStatus dateStatus : DateStatus.values()) {
            if (dateStatus.name().equals(dateStatusName))
                return dateStatus;
        }
        throw new IllegalArgumentException(
                dateStatusName
                        + " - invalid DateStatus, use one of "  + DateStatus.values().toString()
        );
    }

    public static DateStatus getDateStatus(boolean isWeekday, boolean isWorkTime, boolean isBeforeWorkTime, boolean isAfterWorkTime) {
        for (DateStatus dateStatus : DateStatus.values()) {
            if (dateStatus.isWeekday == isWeekday && dateStatus.isWorkTime == isWorkTime && dateStatus.isBeforeWorkTime == isBeforeWorkTime && dateStatus.isAfterWorkTime == isAfterWorkTime)
                return dateStatus;
        }
        throw new IllegalArgumentException("DataStatus not fount");
    }
}