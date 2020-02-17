package ru.shadewallcorp.jiraTask.jiraManager.api;

/**
 * @author Dilvish  {@literal <dilvish@newmail.ru>}
 */
public interface JiraManagerRequest {
    String getUserName();
    String getPassword();
    JiraManagerRequest withUserName(String userName);
    JiraManagerRequest withPassword(String password);
}
