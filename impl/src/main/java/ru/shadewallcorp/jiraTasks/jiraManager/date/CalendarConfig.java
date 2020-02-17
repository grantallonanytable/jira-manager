package ru.shadewallcorp.jiraTasks.jiraManager.date;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
@Data
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
@NoArgsConstructor(onConstructor = @__({@Deprecated}))
public class CalendarConfig {
    private List<String> notWorkDaysList;
    private List<String> workHolidaysList;
}
