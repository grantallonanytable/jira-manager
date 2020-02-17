package ru.shadewallcorp.jiraTask.jiraManager.jira.issue;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.PropertyInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.shadewallcorp.jiraTask.jiraManager.jira.JiraVar;
import ru.shadewallcorp.jiraTask.jiraManager.sla.SLA;
import ru.shadewallcorp.jiraTask.jiraManager.timeline.TimeLineManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static com.atlassian.jira.rest.client.api.IssueRestClient.Expandos.CHANGELOG;
import static com.atlassian.jira.rest.client.api.IssueRestClient.Expandos.OPERATIONS;
import static ru.shadewallcorp.jiraTask.jiraManager.date.DateUtil.date;

/**
 * Бизнес - объект jira issue
 *

 */
public class IssueBo {
    final static Logger logger = LoggerFactory.getLogger(IssueBo.class);

    private static final boolean IS_JENKINS = true;
    public static final String JQL_SET_SCRUM = "project = \"TODO_JIRA_SEARCH_PROJECT_NAME and type not in (Test) and labels in (product, лояльность, shop, СИС, b2b, devOps, suz, site) and labels not in (parent, scrum) AND labels not in (qa, ru.shadewallcorp.jiraTask.jiraManager.api, API, auto) AND status != Закрыт";
    public static final String JQL_UPDATE_LABELS = "project = \"TODO_JIRA_SEARCH_PROJECT_NAME and type = Incident and status in (10696, \"READY TO MERGE\")";
    public static final String JQL_UPDATE_DEADLINES = "project = \"TODO_JIRA_SEARCH_PROJECT_NAME and type = Incident and createdDate > \"2018/02/01\"" +
            " and status in (ToDo, \"READY FOR ANALYSES\", Analysis, RECOVERY, \"Ready for development\", Develop," +
            " \"READY FOR CODEREVIEW\", CodeReview, \"Ready For Testing\", \"On Test\")";
    public static final String JQL_GET_CLOSE_WITHOUT_DEV_INC = "project = \"TODO_JIRA_SEARCH_PROJECT_NAME and type = Incident and createdDate > \"2018/02/01\"";
    public static final String JQL_GET_RESOLVED_INC = "project = \"TODO_JIRA_SEARCH_PROJECT_NAME and type = Incident and createdDate > \"2018/02/01\"";
    public static final String JQL_GET_NEW_INC_FMT = "project = \"TODO_JIRA_SEARCH_PROJECT_NAME AND type = Incident and created >= \"%s\" and created <= \"%s\"";
    public static final String JQL_GET_INC_FMT = "project = \"TODO_JIRA_SEARCH_PROJECT_NAME and type = Incident and issue = %s";
    public static final String JQL_GET_ALL_INC = "project = \"TODO_JIRA_SEARCH_PROJECT_NAME and type = Incident and createdDate > \"2018/02/01\"";
    public static final String JQL_GET_ALL_OPEN_INC = "project = \"TODO_JIRA_SEARCH_PROJECT_NAME AND type = Incident AND status != 6 AND status != 10696 AND status != 12898";
    private String userName;
    private String password;

    public IssueBo(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    /**
     * Генерирует список объектов всех открытых инцидентов в jira
     *
     * @return список объектов всех открытых инцидентов в jira
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParseException
     */
    public List<IssueDo> getAllOpenInc() throws Exception {
        return getAllInc(JQL_GET_ALL_OPEN_INC, true);
    }

    /**
     * Генерирует список объектов всех инцидентов в jira
     *
     * @return список объектов всех инцидентов в jira
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParseException
     */
    public List<IssueDo> getAllInc() throws Exception {
        return getAllInc(JQL_GET_ALL_INC, false);
    }

    /**
     * Генерирует объект инцидента в jira
     *
     * @param issueId номер тикета в jira
     * @return бъект инцидента в jira
     * @throws ParseException
     * @throws IOException
     * @throws URISyntaxException
     */
    public IssueDo getInc(String issueId) throws Exception {
        List<IssueDo> issueDoList = getAllInc(String.format(JQL_GET_INC_FMT, issueId), true);
        return (issueDoList.size() == 0) ? null : issueDoList.get(0);
    }

    /**
     * Составляет список открытых тикетов за период времени
     *
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @return список решенных тикетов за период времени
     * @throws ParseException
     * @throws IOException
     * @throws URISyntaxException
     */
    public List<IssueDo> getNewInc(Date startDate, Date endDate) throws Exception {
        return getAllInc(String.format(JQL_GET_NEW_INC_FMT, date(startDate, "yyyy/MM/dd HH:mm"), date(endDate, "yyyy/MM/dd HH:mm")), true);
    }

    /**
     * Составляет список решенных тикетов за период времени
     *
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @return список решенных тикетов за период времени
     * @throws ParseException
     * @throws IOException
     * @throws URISyntaxException
     */
    public List<IssueDo> getResolvedInc(Date startDate, Date endDate) throws Exception {
        List<IssueDo> issueDoList = getAllInc(JQL_GET_RESOLVED_INC, false);
        List<IssueDo> closedInc = new ArrayList<>();
        for(IssueDo issueDo : issueDoList) {
            Date solutionDate = issueDo.getSolutionDate();
            if(solutionDate != null && solutionDate.after(startDate) && solutionDate.before(endDate))
                closedInc.add(issueDo);
        }
        return closedInc;
    }

    /**
     * Составляет список закрытых без разработки тикетов за период времени
     *
     * @param startDate начальная дата
     * @param endDate конечная дата
     * @return список закрытых без разработки тикетов за период времени
     * @throws ParseException
     * @throws IOException
     * @throws URISyntaxException
     */
    public List<IssueDo> getClosedWithoutDevInc(Date startDate, Date endDate) throws Exception {
        List<IssueDo> issueDoList = getAllInc(JQL_GET_CLOSE_WITHOUT_DEV_INC, true);
        List<IssueDo> closedInc = new ArrayList<>();
        for(IssueDo issueDo : issueDoList) {
            Date solutionDate = issueDo.getSolutionDate();
            if(solutionDate != null && solutionDate.after(startDate) && solutionDate.before(endDate) && issueDo.getCurrentStatus() == IssueStatus.CLOSED_WITHOUT_DEV)
                closedInc.add(issueDo);
        }
        return closedInc;
    }


    /**
     * Обновляет дедлайны у активных инцидентов
     *
     * @throws ParseException
     * @throws IOException
     * @throws URISyntaxException
     */
    public void updateDeadline() throws Exception {
        List<IssueDo> issueDoList;
        // обновляем дедлайны в описании
        issueDoList = getAllInc(JQL_UPDATE_DEADLINES, false);
        for (IssueDo issueDo : issueDoList) {
            String deadlineDescription = "{color:red}"
                    + "REACTION DEADLINE: "
                    + date(issueDo.getSla().getReactionDeadline().getDeadline())
                    + "\n"
                    + "RECOVERY DEADLINE: "
                    + ((issueDo.getSla().getRecoveryDeadline() != null) ? date(issueDo.getSla().getRecoveryDeadline().getDeadline()) : "-")
                    + "\n"
                    + "SOLUTION DEADLINE: "
                    + date(issueDo.getSla().getSolutionDeadline().getDeadline())
                    + "\n\n\n{color}";
            String description = (issueDo.getDescription() != null) ? issueDo.getDescription() : "";
            if (!description.contains(deadlineDescription))
                updateField(
                        issueDo.getIssueId(),
                        "description",
                        deadlineDescription
                                + description
                                .replaceAll("\\{color\\:red\\}REACTION DEADLINE[\\s\\S]*\\{color\\}", "")
                );
        }
    }

    /**
     * Обновляет метки у тикетов в статусах рфм, релиз
     *
     * @throws ParseException
     * @throws IOException
     * @throws URISyntaxException
     */
    public void updateLabels() throws Exception {
        List<IssueDo> issueDoList = getAllInc(JQL_UPDATE_LABELS, true);
        for (IssueDo issueDo : issueDoList) {
            List<String> labels = issueDo.getLabels();
            if (!labels.contains("scrum")) {
                labels.add("scrum");
                updateField(issueDo.getIssueId(), "labels", labels);
            }
        }
    }

    /**
     * Добавляет метку scrum у открытых задач и дефектов
     *
     * @throws ParseException
     * @throws IOException
     * @throws URISyntaxException
     */
    public void setScrum() throws Exception {
        for (IssueDo issueDo : getAllInc(JQL_SET_SCRUM, true)) {
            List<String> labels = issueDo.getLabels();
            if (!labels.contains("scrum")) {
                labels.add("scrum");
                updateField(issueDo.getIssueId(), "labels", labels);
            }
        }
    }

    /**
     * Генерирует список всех инцидентов в jira по заданному jql
     *
     * @param jql jql запрос
     * @param includeNotDev - флаг учета тикета, который не находился в разработке, но закрыт
     * @return список всех инцидентов в jira
     * @throws URISyntaxException
     * @throws IOException
     * @throws ParseException
     */
    private List<IssueDo> getAllInc(String jql, boolean includeNotDev) throws Exception {
        if(IS_JENKINS) {
            System.getProperties().put("https.proxyHost", "TODO_PROXY");
            System.getProperties().put("https.proxyPort", "80");
            System.getProperties().put("https.proxySet", "true");
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
        }
        try (JiraRestClient jiraRestClient = new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(new URI(JiraVar.JIRA_URL.getJiraVar()), userName, password)) {
            try {
                java.lang.reflect.Field f1 = Class.forName("com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient").getDeclaredField("httpClient");
                java.lang.reflect.Field f2 = Class.forName("com.atlassian.jira.rest.client.internal.async.AtlassianHttpClientDecorator").getDeclaredField("httpClient");
                java.lang.reflect.Field f3 = Class.forName("com.atlassian.httpclient.apache.httpcomponents.ApacheAsyncHttpClient").getDeclaredField("httpClient");
                java.lang.reflect.Field f4 = Class.forName("org.apache.http.impl.client.cache.CachingHttpAsyncClient").getDeclaredField("backend");
                java.lang.reflect.Field f5 = Class.forName("org.apache.http.impl.nio.client.InternalHttpAsyncClient").getDeclaredField("defaultConfig");
                java.lang.reflect.Field f6 = Class.forName("org.apache.http.client.config.RequestConfig").getDeclaredField("socketTimeout");
                f1.setAccessible(true);
                f2.setAccessible(true);
                f3.setAccessible(true);
                f4.setAccessible(true);
                f5.setAccessible(true);
                f6.setAccessible(true);
                Object requestConfig = f5.get(f4.get(f3.get(f2.get(f1.get(jiraRestClient)))));
                f6.setInt(requestConfig, 1000000);
                f1.setAccessible(false);
                f2.setAccessible(false);
                f3.setAccessible(false);
                f4.setAccessible(false);
                f5.setAccessible(false);
                f6.setAccessible(false);
            } catch (Exception ignore) {
            }
            List<IssueDo> issueDoList = new ArrayList<>();
            SearchResult searchResult = jiraRestClient.getSearchClient().searchJql(jql, 1000, 0, null).claim();
            for (BasicIssue basicIssue : searchResult.getIssues()) {
                Issue issue = null;
                IssueDo issueDo = null;
                // формируем объект issue
                try {
                    // формируем issue с changelog
                    issue = jiraRestClient.getIssueClient().getIssue(basicIssue.getKey(), EnumSet.of(CHANGELOG, OPERATIONS)).claim();
                    Date lastChangeStateDate = getLastChangeIncStateDate((ArrayList<ChangelogGroup>) issue.getChangelog());
                    Optional<Object> sr = Optional.ofNullable(issue.getField("customfield_18391").getValue());

                    issueDo = new IssueDo(issue.getKey(), Severity.getSeverity(issue.getPriority().getId() - 1),
                            Integer.parseInt(issue.getStatus().getId() + ""), issue.getSummary(), issue.getDescription(),
                            lastChangeStateDate == null ? issue.getCreationDate().toDate() : lastChangeStateDate,
                            new ArrayList<>(issue.getLabels()), sr.orElse("").toString());
                } catch (Exception e) {
                    throw new Exception("Error for issue " + basicIssue.getKey());
                }
                if(issueDo.getCurrentStatus() == IssueStatus.ADD_INFO || issueDo.getCurrentStatus() == IssueStatus.WAITING_EXTERNAL)
                    issueDo.setRejected(true);
                boolean isDevelop = true;
                // проходим по всем транзакциям
                for (ChangelogGroup changelogGroup : issue.getChangelog()) {
                    // проходим по всем событиям внутри транзакции
                    for (ChangelogItem changelogItem : changelogGroup.getItems()) {
                        Date creationDate = changelogGroup.getCreated().toDate();
                        /*
                         * Формируем объект события смены статуса и отсеиваем все события кроме смены статуса, а также
                         * отсеиваем изменения, которые были выполнены раньше последнего изменения критичности и
                         * раньше последнего изменения типа на Инцидент
                         */
                        if (changelogItem.getField().equals("status") && (creationDate.after(issueDo.getCreationDate())
                                || creationDate.equals(issueDo.getCreationDate()))) {
                            IssueStatusDo issueStatusDo = new IssueStatusDo(Integer.parseInt(changelogItem.getTo()),
                                    Integer.parseInt(changelogItem.getFrom()), creationDate, changelogGroup.getAuthor().getDisplayName());
                            // фиксируем наличие некорректности (наличие некорректного статуса или наличие метки incorrect)
                            issueDo.setIncorrectStatus(!issueDo.isIncorrectStatus()
                                    && (!issueStatusDo.isCorrect() || issue.getLabels().contains("incorrect")));
                            issueDo.addIssueStatusList(issueStatusDo);
                            // Помечаем, что тикет находился в разработке
                            if((issueStatusDo.getStatusFrom() == IssueStatus.ADD_INFO || issueStatusDo.getStatusFrom() == IssueStatus.WAITING_EXTERNAL)
                                    && issueStatusDo.getStatusTo() == IssueStatus.CLOSED) isDevelop = false;
                        }
                    }
                }
                // Если тикет не находился в разработке, но закрыт, то не нужно его учитывать
                if(issueDo.getCurrentStatus() == IssueStatus.CLOSED && !isDevelop) {
                    if(!includeNotDev) continue;
                    issueDo.setCurrentStatus(IssueStatus.CLOSED_WITHOUT_DEV.getStatusCode());
                    issueDo.setRejected(true);
                }
                String fixVersions = "";
                for (Version version : issue.getFixVersions()) fixVersions += version.getName() + ", ";
                issueDo.setReleaseVersion(!fixVersions.equals("") ? fixVersions.substring(0, fixVersions.length() - 2) : "");
                // заполняем времена по sla
                SLA sla = new SLA(TimeLineManager.getTimeLine(issueDo), TimeLineManager.getDeadLine(issueDo));
                issueDo.setSla(sla);
                issueDoList.add(issueDo);
            }
            // Сортируем по статусам
            Collections.sort(issueDoList, Comparator.comparingInt(item -> item.getCurrentStatus().getPriority()));
            return issueDoList;
        }
    }

    /**
     * Вычисляет дату последней смены состояния инцидента (смены критичности, смена типа)
     *
     * @param issueChangelog change log
     * @return дату последней смены состояния инцидента (если null, то изменения не было)
     */
    private Date getLastChangeIncStateDate(ArrayList<ChangelogGroup> issueChangelog) {
        Date lastChangeSeverityDate = null;
        // проходим по всем транзакциям
        for (ChangelogGroup changelogGroup : issueChangelog) {
            ArrayList<ChangelogItem> changelogItems = (ArrayList<ChangelogItem>) changelogGroup.getItems();
            // проходим по всем событиям внутри транзакции
            for (ChangelogItem changelogItem : changelogItems) {
                // отсеиваем все события кроме смены критичности и типа
                if (changelogItem.getField().equals("priority") ||
                        (changelogItem.getField().equals("issuetype") && changelogItem.getToString().equals("Incident")))
                    lastChangeSeverityDate = changelogGroup.getCreated().toDate();
            }
        }
        return lastChangeSeverityDate;
    }

    /**
     * Обновляет значение поле
     *
     * @param issueId   идентификатор тикета
     * @param fieldName название поля
     * @param value     значение
     * @throws URISyntaxException
     * @throws IOException
     */
    private void updateField(String issueId, String fieldName, Object value) throws URISyntaxException, IOException {
        try (JiraRestClient jiraRestClient = new AsynchronousJiraRestClientFactory().createWithBasicHttpAuthentication(
                new URI(JiraVar.JIRA_URL.getJiraVar()),
                userName,
                password
        )) {
            Map<String, FieldInput> valMap = new HashMap<>();
            valMap.put(fieldName, new FieldInput(fieldName, value));
            List<PropertyInput> properties = Collections.emptyList();
            jiraRestClient.getIssueClient().updateIssue(issueId, new IssueInput(valMap, properties)).claim();
        }
    }
}
