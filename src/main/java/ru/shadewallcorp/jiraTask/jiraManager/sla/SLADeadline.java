package ru.shadewallcorp.jiraTask.jiraManager.sla;

import java.util.Date;

/**
 * Дедлайны по SLA
 *

 */
public class SLADeadline {
    private Date deadline;
    private SLATimeType slaTimeType;

    public SLADeadline() {
    }

    public SLADeadline(Date deadline, SLATimeType slaTimeType) {
        this.deadline = deadline;
        this.slaTimeType = slaTimeType;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public SLATimeType getSlaTimeType() {
        return slaTimeType;
    }

    public void setSlaTimeType(SLATimeType slaTimeType) {
        this.slaTimeType = slaTimeType;
    }
}