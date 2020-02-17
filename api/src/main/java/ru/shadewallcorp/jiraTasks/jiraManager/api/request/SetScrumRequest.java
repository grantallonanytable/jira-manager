package ru.shadewallcorp.jiraTasks.jiraManager.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 *  Запрос.
 *
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
@Builder
@Data
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class SetScrumRequest implements JiraManagerRequest {
    /** userName. */
    private String userName;
    /** password. */
    private String password;

}
