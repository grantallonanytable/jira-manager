package ru.shadewallcorp.jiraTasks.jiraManager.api.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class JiraTask {
        private final String key;
        private final String name;
        private final String status;
        private final String priority;
        private final String srnumber;
        private final String startDate;
        private final String closeDate;
        private final String comment;
}
