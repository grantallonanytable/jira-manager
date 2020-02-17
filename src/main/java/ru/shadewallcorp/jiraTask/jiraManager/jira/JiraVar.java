package ru.shadewallcorp.jiraTask.jiraManager.jira;

/**
 * Переменные для JiraVar
 *

 */
public enum JiraVar {
    JIRA_URL("https://jira.TODO_JIRA_HOST.ru");

    private final String jiraVar;

    public String getJiraVar() {
        return jiraVar;
    }

    JiraVar(String jiraVar) {
        this.jiraVar = jiraVar;
    }

    public static JiraVar getJiraVar(String jiraVar) {
        for (JiraVar issueStatusEnum : JiraVar.values()) {
            if (issueStatusEnum.getJiraVar().equals(jiraVar))
                return issueStatusEnum;
        }
        throw new IllegalArgumentException(
                jiraVar
                        + " - invalid jira variable, use one of "  + JiraVar.values().toString()
        );
    }
}
