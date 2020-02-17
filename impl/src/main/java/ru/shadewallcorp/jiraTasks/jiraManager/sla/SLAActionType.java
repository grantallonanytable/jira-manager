package ru.shadewallcorp.jiraTasks.jiraManager.sla;

/**
 * Типы действий по SLA
 *
 */
public enum SLAActionType {
    REACTION_ACTION(1),
    RECOVERY_ACTION(2),
    SOLUTION_ACTION(3);

    private final int type;

    public int getType() {
        return type;
    }

    SLAActionType(int type) {
        this.type = type;
    }

    public static SLAActionType getType(String type) {
        for (SLAActionType slaTimeLapseType : SLAActionType.values()) {
            if (slaTimeLapseType.name().equals(type))
                return slaTimeLapseType;
        }
        throw new IllegalArgumentException(
                type
                        + " - invalid SLAActionType, use one of " + SLAActionType.values().toString()
        );
    }
}