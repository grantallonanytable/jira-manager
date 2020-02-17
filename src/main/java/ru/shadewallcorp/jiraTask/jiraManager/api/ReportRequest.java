package ru.shadewallcorp.jiraTask.jiraManager.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import java.util.Date;

/**
 *  Запрос.
 *
 * @author Dilvish  {@literal <dilvish@newmail.ru>}
 */
@Builder
@Data
@Wither
@NoArgsConstructor
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
public class ReportRequest implements JiraManagerRequest {
    /** userName. */
    private String userName;
    /** password. */
    private String password;
    /** Номер задачи. */
    private String issueId;
    /** Полный или краткий отчет. */
    private Boolean isFull;
    /** Дата от. */
    private Date dateFrom;
    /** Дата по. */
    private Date dateTo;

}
