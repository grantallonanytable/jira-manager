package ru.shadewallcorp.jiraTasks.jiraManager.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
@Data
@AllArgsConstructor(onConstructor = @__({@JsonCreator}))
@NoArgsConstructor(onConstructor = @__({@Deprecated}))
public class JiraConfig {
    /** default userName. */
    private String userName;
    /** default password. */
    private String password;
    /** max количество результатов в выборках */
    private int maxResultCount;
    // Дальше таймауты в формате timeUnit (10s, 10m, 10d ...)
    private Duration connectionTimeout;
    private Duration requestTimeout;
    private Duration socketTimeout;
    private Long leaseTimeout;

    /** Соответствия между полями объекта-задачи и заголовками */
    //TODO: костыль, не нужно в исходном массиве данных подписывать колонки кепшенами, лучше кодами, наоборот
    private JiraTaskFieldAliases jiraTaskFieldAliases;

    public static Integer indexOf(List<String> columnCaptions, List<String> aliases) {
        return aliases.stream().map(columnCaptions::indexOf).filter(i -> i > 0).findFirst().orElse(-1);
    }

    public static String getOptionalValue(List<String> values, int index) {
        return (values == null || values.isEmpty() || index < 0 || index >= values.size()) ? null :
                values.get(index);
    }
}
