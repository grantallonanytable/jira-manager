package ru.shadewallcorp.jiraTasks.jiraManager.sla;

/**
 * Типы временного отрезка по SLA
 *
 */
public enum SLATimeType {
    DAY_AND_NIGHT(1, "круглосуточно"),
    WORK_DAYS_WITHOUT_HOLIDAYS(2, "5 дней в неделю, исключая праздники, с 9:00 до 18:00"),
    WORK_DAYS_WITH_HOLIDAYS(3, "7 дней в неделю, не исключая праздники, с 9:00 до 18:00");

    private final int type;
    private final String desc;

    public int getType() {
        return type;
    }
    public String getDesc() {
        return desc;
    }

    SLATimeType(int type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public static SLATimeType getType(String type) {
        for (SLATimeType slaTimeType : SLATimeType.values()) {
            if (slaTimeType.name().equals(type))
                return slaTimeType;
        }
        throw new IllegalArgumentException(
                type
                        + " - invalid SLATimeType, use one of " + SLATimeType.values().toString()
        );
    }
}
