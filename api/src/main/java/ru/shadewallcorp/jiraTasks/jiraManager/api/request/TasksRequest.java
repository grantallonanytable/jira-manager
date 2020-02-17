package ru.shadewallcorp.jiraTasks.jiraManager.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 *  Запрос.
 *
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
@Builder
@Data
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class TasksRequest implements JiraManagerRequest {
    /** userName. */
    private String userName;
    /** password. */
    private String password;
    /** Номер задачи. */
    private String issueId;
    /** Полный или краткий отчет. */
    private Boolean isFull;
    /** Дата от. */
    @JsonFormat(pattern = "dd-MM-yyyy' 'hh:mm:ss")
    private Date dateFrom;
    /** Дата по. */
    @JsonFormat(pattern = "dd-MM-yyyy' 'hh:mm:ss")
    private Date dateTo;

}
