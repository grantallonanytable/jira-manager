package ru.shadewallcorp.jiraTasks.jiraManager.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Агрегированные данные для графиков на фроните.
 *
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
@Builder
@Value
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
@Deprecated
public class GraphicsResponse {
    private final List<JiraAnalyticsData> jiraTaskStatusData;
    private final List<JiraAnalyticsData> jiraTaskNewStatusData;
    private final List<JiraAnalyticsData> jiraTaskOpenedClosedData;
    private final List<JiraAnalyticsData> jiraTaskOpenedRejectedData;
}
