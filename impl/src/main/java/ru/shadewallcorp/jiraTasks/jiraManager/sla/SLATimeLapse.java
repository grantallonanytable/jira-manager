package ru.shadewallcorp.jiraTasks.jiraManager.sla;

import ru.shadewallcorp.jiraTasks.jiraManager.date.Time;

import java.util.Date;

/**
 * Временные отрезки по SLA
 *
 */
public class SLATimeLapse {
    // конечная дата, от которой рассчитывается (вычитается начальная) временной период
    private Date date;
    private Time timeLapse;
    private SLATimeType slaTimeType;

    public SLATimeLapse(){
    }

    public SLATimeLapse(Time timeLapse, SLATimeType slaTimeType) {
        this.date = null;
        this.timeLapse = timeLapse;
        this.slaTimeType = slaTimeType;
    }

    public SLATimeLapse(Date date, Time timeLapse, SLATimeType slaTimeType) {
        this.date = date;
        this.timeLapse = timeLapse;
        this.slaTimeType = slaTimeType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTimeLapse() {
        return timeLapse;
    }

    public void setTimeLapse(Time timeLapse) {
        this.timeLapse = timeLapse;
    }

    public SLATimeType getSlaTimeType() {
        return slaTimeType;
    }

    public void setSlaTimeType(SLATimeType slaTimeType) {
        this.slaTimeType = slaTimeType;
    }

    public String getTimeLapseStr() {
        if(timeLapse != null)
            return timeLapse.getDays() + " дней, "  + timeLapse.getHours() + " часов, "
                + timeLapse.getMinutes() + " минут/" + slaTimeType.getDesc();
        return "";
    }
}
