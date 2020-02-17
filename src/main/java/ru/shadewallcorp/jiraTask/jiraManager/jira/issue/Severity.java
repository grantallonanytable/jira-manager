package ru.shadewallcorp.jiraTask.jiraManager.jira.issue;

import ru.shadewallcorp.jiraTask.jiraManager.date.Time;
import ru.shadewallcorp.jiraTask.jiraManager.sla.SLA;
import ru.shadewallcorp.jiraTask.jiraManager.sla.SLATimeLapse;
import ru.shadewallcorp.jiraTask.jiraManager.sla.SLATimeType;

/**
 * Severity инцидента
 *

 */
public enum Severity {
    // не используется, но нужен для функционала setScrum
    SEV_1_PLUS(
            0,
            new SLA(
                    new SLATimeLapse(new Time(0, 0, 30), SLATimeType.DAY_AND_NIGHT),
                    new SLATimeLapse(new Time(0, 2, 0), SLATimeType.DAY_AND_NIGHT),
                    new SLATimeLapse(new Time(0, 8, 0), SLATimeType.DAY_AND_NIGHT))
    ),
    SEV_1(
            1,
            new SLA(
                    new SLATimeLapse(new Time(0, 0, 30), SLATimeType.DAY_AND_NIGHT),
                    new SLATimeLapse(new Time(0, 2, 0), SLATimeType.DAY_AND_NIGHT),
                    new SLATimeLapse(new Time(0, 8, 0), SLATimeType.DAY_AND_NIGHT))
    ),
    SEV_2(
            2,
            new SLA(
                    new SLATimeLapse(new Time(0, 1, 0), SLATimeType.WORK_DAYS_WITHOUT_HOLIDAYS),
                    new SLATimeLapse(new Time(0, 8, 0), SLATimeType.WORK_DAYS_WITHOUT_HOLIDAYS),
                    new SLATimeLapse(new Time(4, 0, 0), SLATimeType.WORK_DAYS_WITHOUT_HOLIDAYS))
    ),
    SEV_3(
            3,
            new SLA(
                    new SLATimeLapse(new Time(0, 8, 0), SLATimeType.WORK_DAYS_WITH_HOLIDAYS),
                    null,
                    new SLATimeLapse(new Time(28, 0, 0), SLATimeType.WORK_DAYS_WITH_HOLIDAYS))
    ),
    SEV_4(
            4,
            new SLA(
                    new SLATimeLapse(new Time(3, 0, 0), SLATimeType.WORK_DAYS_WITH_HOLIDAYS),
                    null,
                    new SLATimeLapse(new Time(180, 0, 0), SLATimeType.DAY_AND_NIGHT))
    );

    private final long severity;
    private final SLA sla;

    public long getSeverity() {
        return severity;
    }
    public SLA getSla() {
        return sla;
    }

    Severity(long severity, SLA sla) {
        this.severity = severity;
        this.sla = sla;
    }

    public static Severity getSeverity(long severityValue) {
        for (Severity severity : Severity.values()) {
            if (severity.getSeverity() == severityValue)
                return severity;
        }
        throw new IllegalArgumentException(
                severityValue
                        + " - invalid severity, use one of "  + Severity.values().toString()
        );
    }
}
