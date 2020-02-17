package ru.shadewallcorp.jiraTasks.jiraManager.api.request;

/**
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
public interface JiraManagerRequest {
    String getUserName();
    String getPassword();
    //TODO: придумать другую модель подмены логина-пароля на умолчательные
    void setUserName(String userName);
    void setPassword(String password);
}
