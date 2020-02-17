package ru.shadewallcorp.jiraTask.jiraManager.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Ответ на запрос.
 *
 * @author Dilvish  {@literal <dilvish@newmail.ru>}
 */
@AllArgsConstructor
@Builder
@Value
public class JiraManagerResponse {
    /** id. */
    private final String error;
}
