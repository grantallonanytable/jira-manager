package ru.shadewallcorp.jiraTask.jiraManager.util;

/**
 * Типы операций
 *

 */
public enum Action {
    UPDATE_LABELS("UPDATE_LABELS"),
    UPDATE_DEADLINE("UPDATE_DEADLINE"),
    GENERATE_REPORT_BY_ISSUE("GENERATE_REPORT_BY_ISSUE"),
    GENERATE_SHORT_REPORT("GENERATE_SHORT_REPORT"),
    GENERATE_FULL_REPORT("GENERATE_FULL_REPORT"),
    SET_SCRUM("SET_SCRUM");

    private final String action;

    public String getAction() {
        return action;
    }

    Action(String action) {
        this.action = action;
    }

    public static Action getAction(String actionValue) {
        for (Action action : Action.values()) {
            if (action.name().equals(actionValue))
                return action;
        }
        throw new IllegalArgumentException(
                actionValue
                        + " - invalid Action, use one of " + Action.values().toString()
        );
    }

    public static boolean contains(String actionValue) {
        for (Action action : Action.values()) {
            if (action.name().equals(actionValue))
                return true;
        }
        return false;
    }

    public static Action[] getAllActions() {
        return Action.values();
    }
}
