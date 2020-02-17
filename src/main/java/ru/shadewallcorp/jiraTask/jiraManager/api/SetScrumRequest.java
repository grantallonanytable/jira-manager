package ru.shadewallcorp.jiraTask.jiraManager.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Wither;

/**
 *  Запрос.
 *
 * @author Dilvish  {@literal <dilvish@newmail.ru>}
 */
@Builder
@Value
@Wither
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class SetScrumRequest implements JiraManagerRequest {
    /** userName. */
    private String userName;
    /** password. */
    private String password;

}
