package ru.shadewallcorp.jiraTasks.jiraManager.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

/**
 * Запись агрегированных данных для графика на фронте.
 *
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
@Builder
@Value
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class JiraAnalyticsData {
    @JsonFormat(pattern = "dd-MM-yyyy")
    private final LocalDate date;
    @JsonProperty("TODO")
    private final Integer todo;
    @JsonProperty("ANALYSIS")
    private final Integer analysis;
    @JsonProperty("DEVELOP")
    private final Integer develop;
    @JsonProperty("ON_TEST")
    private final Integer onTest;
    @JsonProperty("WAITING_EXTERNAL")
    private final Integer waitingExternal;
    @JsonProperty("ADD_INFO")
    private final Integer addInfo;
    @JsonProperty("CLOSED")
    private final Integer closed;
    @JsonProperty("CLOSED_WITHOUT_DEV")
    private final Integer closedWithoutDev;
    @JsonProperty("INVALID")
    private final Integer invalid;
}
