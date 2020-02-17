package ru.shadewallcorp.jiraTasks.jiraManager.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;
import java.util.Optional;

/**
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
@Data
@NoArgsConstructor
public class JiraTaskFieldAliases {
    private List<String> key;
    private List<String> name;
    private List<String> status;
    private List<String> priority;
    private List<String> srnumber;
    private List<String> startDate;
    private List<String> closeDate;
    private List<String> comment;

}
