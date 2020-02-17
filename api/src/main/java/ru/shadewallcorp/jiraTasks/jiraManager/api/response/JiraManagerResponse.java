package ru.shadewallcorp.jiraTasks.jiraManager.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Ответ на запрос.
 *
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
@AllArgsConstructor
@Builder
@Value
public class JiraManagerResponse {
    public static final JiraManagerResponse OK = JiraManagerResponse.builder().build();
    /** id. */
    private final String error;
}
