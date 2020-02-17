package ru.shadewallcorp.jiraTasks.jiraManager.date;

/**
 * Класс - обертка для объекта разницы между двумя датами
 *
 */
public class Time {
    private long days;
    private long hours;
    private long minutes;

    public Time(long days, long hours, long minutes) {
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
    }

    public long getDays() {
        return days;
    }

    public void setDays(long days) {
        this.days = days;
    }

    public long getHours() {
        return hours;
    }

    public void setHours(long hours) {
        this.hours = hours;
    }

    public long getMinutes() {
        return minutes;
    }

    public void setMinutes(long minutes) {
        this.minutes = minutes;
    }
}