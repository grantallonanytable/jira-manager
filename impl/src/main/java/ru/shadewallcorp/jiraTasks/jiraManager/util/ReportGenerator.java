package ru.shadewallcorp.jiraTasks.jiraManager.util;

import akka.Done;
import akka.japi.Pair;
import akka.util.ByteString;
import com.google.common.collect.Maps;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.api.transport.TransportException;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import io.netty.handler.timeout.TimeoutException;
import org.apache.commons.collections4.list.TreeList;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.StringUtils;
import ru.shadewallcorp.jiraTasks.jiraManager.api.response.GraphicsResponse;
import ru.shadewallcorp.jiraTasks.jiraManager.api.response.JiraAnalyticsData;
import ru.shadewallcorp.jiraTasks.jiraManager.api.response.JiraTask;
import ru.shadewallcorp.jiraTasks.jiraManager.api.request.TasksRequest;
import ru.shadewallcorp.jiraTasks.jiraManager.api.request.JiraManagerRequest;
import ru.shadewallcorp.jiraTasks.jiraManager.config.JiraConfig;
import ru.shadewallcorp.jiraTasks.jiraManager.config.JiraTaskFieldAliases;
import ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil;
import ru.shadewallcorp.jiraTasks.jiraManager.jira.issue.*;
import ru.shadewallcorp.jiraTasks.jiraManager.sla.SLADeadline;
import ru.shadewallcorp.jiraTasks.jiraManager.sla.SLATimeLapse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lightbend.lagom.javadsl.api.transport.TransportErrorCode.ProtocolError;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil.*;
import static ru.shadewallcorp.jiraTasks.jiraManager.util.TimeoutController.execute;

/**
 * Генератор отчетов
 *
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
@Singleton
public class ReportGenerator {
    final static Logger LOGGER = Logger.getLogger(ReportGenerator.class);

    private JiraRestJavaClientWrapper jira;
    public static final List<String> EXCEL_REPORT_FULL_COLUMNS = Arrays.asList("Код", "Дата создания", "Тема", "Статус", "Приоритет",
            "Номер SR", "Срок исполнения max", "Фактический срок исполнения", "Время реакции",
            "Время восстановления", "Время решения");
    public static final List<String> EXCEL_REPORT_SIMPLE_COLUMNS = Arrays.asList("Код", "Тема", "Статус", "Приоритет",
            "Номер SR", "Срок исполнения max", "Комментарий");
    private static final boolean USE_PROXY = true;

    @Inject
    public ReportGenerator(JiraRestJavaClientWrapper jiraRestJavaClientWrapper) {
        super();
        this.jira = jiraRestJavaClientWrapper;
        if (USE_PROXY) {
            System.getProperties().put("https.proxyHost", "todo_proxy-01");
            System.getProperties().put("https.proxyPort", "80");
            System.getProperties().put("https.proxySet", "true");
            TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
        }
    }

    public final BiConsumer<String, String> UPDATE_LABELS = (userName, password) -> {
        try {
            new IssueBo(jira).updateLabels();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    public final BiConsumer<String, String> SET_SCRUM = (userName, password) -> {
        try {
            new IssueBo(jira).setScrum();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    public final BiConsumer<String, String> UPDATE_DEADLINE = (userName, password) -> {
        try {
            new IssueBo(jira).updateDeadline();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private static final long API_TIMEOUT_SECONDS = 1000;

    /**
     * Запуск задания.
     * run(SET_SCRUM, user, password)
     */
    public static void run(BiConsumer<String, String> consumer, String userName, String password) throws TimeoutException {
        execute(() -> consumer.accept(userName, password), API_TIMEOUT_SECONDS);
    }

    public CompletionStage<Done> checkParamsWithDefaults(JiraManagerRequest request) {
        if (StringUtils.isEmpty(request.getUserName())) {
            request.setUserName(jira.getConfig().getUserName());
        }
        if (StringUtils.isEmpty(request.getPassword())) {
            request.setPassword(jira.getConfig().getPassword());
        }
        if (StringUtils.isEmpty(request.getUserName()) || StringUtils.isEmpty(request.getPassword())) {
            throw new RunParametersNotFound("both userName and password must be specified!");
        }
        return completedFuture(Done.getInstance());
    }

    public static CompletionStage<Done> checkReportParams(TasksRequest request) {
        if (request.getIssueId() == null &&
                (request.getIsFull() == null || request.getDateFrom() == null || request.getDateTo() == null))
        {
            throw new RunParametersNotFound("isFull, dateFrom and dateTo must be specified if issueId not specified!");
        }
        if (request.getIssueId() != null) {
            request.setIsFull(false);
        }
        return completedFuture(Done.getInstance());
    }

    public <HEADER, DATA> CompletionStage<Pair<HEADER, DATA>> report(
            TasksRequest request, HEADER header, Function<Map<Integer, List<String>>, DATA> dataConverter)
    {
        return checkParamsWithDefaults(request)
                .thenCompose(done -> checkReportParams(request))
                .thenCompose(done -> completedFuture(dataConverter.apply(generateReportData(
                                request.getIssueId(), request.getIsFull(),
                                request.getDateFrom(), request.getDateTo()))))
                .thenApply(rep -> Pair.create(header, rep));
    }

    public <HEADER, DATA> CompletionStage<Pair<HEADER, DATA>> report(
            String issueId, Boolean isFull, Date dateFrom, Date dateTo, HEADER header,
            Function<Map<Integer, List<String>>, DATA> dataConverter)
    {
        TasksRequest request = TasksRequest.builder()
                .userName(jira.getConfig().getUserName())
                .password(jira.getConfig().getPassword())
                .issueId(issueId)
                .isFull(isFull)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .build();
        return report(request, header, dataConverter);
    }

    public void saveReportToLocalExcel2003(
            String issueId, boolean isFullReport,
            Date dateFrom, Date dateTo, String reportPath)
            throws TimeoutException {
        Runnable task = () -> {
            try {
                saveReportToLocalExcel2003(reportPath, generateReportData(issueId, isFullReport, dateFrom, dateTo));
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        execute(task, Integer.MAX_VALUE);
    }

    public Map<Integer, List<String>> generateReportData(
            String issueId, boolean isFullReport, Date dateFrom, Date dateTo)
            throws TransportException {
        Map<Integer, List<String>> res = new HashMap<>();
        List<String> columnNames;
        List<IssueDo> resolvedIncList = null;
        List<IssueDo> closedWithoutDevIncList = null;
        List<IssueDo> issueDoList;
        List<IssueDo> newIncList = null;
        IssueBo issueBo = new IssueBo(jira);
        try {
            if (issueId != null) {
                issueDoList = Collections.singletonList(issueBo.getInc(issueId));
            } else {
                resolvedIncList = issueBo.getResolvedInc(dateFrom, dateTo);
                closedWithoutDevIncList = issueBo.getClosedWithoutDevInc(dateFrom, dateTo);
                issueDoList = issueBo.getAllOpenInc();
                newIncList = issueBo.getNewInc(dateFrom, dateTo);
            }
            if (isFullReport) {
                columnNames = EXCEL_REPORT_FULL_COLUMNS;
            } else {
                columnNames = EXCEL_REPORT_SIMPLE_COLUMNS;
            }
            res.put(0, new ArrayList<>(Collections.singletonList("Список открытых продуктивных дефектов")));
            res.put(1, columnNames);
            for (int i = 0; i < issueDoList.size(); i++) {
                res.put(i + 2, fillIncTable(issueDoList, i, isFullReport));
            }
            int maxKeyValue = Collections.max(res.entrySet(), Comparator.comparingInt(Map.Entry::getKey)).getKey();
            if (newIncList != null && resolvedIncList != null) {
                maxKeyValue += 2;
                res.put(++maxKeyValue, new ArrayList<>(Collections.singletonList("Список новых продуктивных дефектов")));
                res.put(++maxKeyValue, columnNames);
                for (int i = 0; i < newIncList.size(); i++) {
                    res.put(++maxKeyValue, fillIncTable(newIncList, i, isFullReport));
                }
                maxKeyValue += 2;
                res.put(++maxKeyValue, new ArrayList<>(Collections.singletonList("Список решенных продуктивных дефектов")));
                res.put(++maxKeyValue, columnNames);
                for (int i = 0; i < resolvedIncList.size(); i++) {
                    res.put(++maxKeyValue, fillIncTable(resolvedIncList, i, isFullReport));
                }
                maxKeyValue += 2;
                res.put(++maxKeyValue, new ArrayList<>(Collections.singletonList("Список закрытых без разработки продуктивных дефектов")));
                res.put(++maxKeyValue, columnNames);
                for (int i = 0; i < closedWithoutDevIncList.size(); i++) {
                    res.put(++maxKeyValue, fillIncTable(closedWithoutDevIncList, i, isFullReport));
                }
                maxKeyValue += 2;
                res.put(++maxKeyValue, new ArrayList<>(Arrays.asList("Количество новых продуктивных дефектов", newIncList.size() + "")));
                res.put(++maxKeyValue, new ArrayList<>(Arrays.asList("Количество новых отклоненных продуктивных дефектов", (int) newIncList.stream().filter(IssueDo::isRejected).count() + "")));
                res.put(++maxKeyValue, new ArrayList<>(Arrays.asList("Количество решенных продуктивных дефектов", resolvedIncList.size() + "")));
                res.put(++maxKeyValue, new ArrayList<>(Arrays.asList("Количество закрытых без разработки продуктивных дефектов", closedWithoutDevIncList.size() + "")));
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequest(e);
        }
    }

    private List<String> fillIncTable(List<IssueDo> issueDoList, int index, boolean isFullReport) {
        IssueDo issueDo = issueDoList.get(index);
        Optional<SLADeadline> slaSolutionDeadline = Optional.ofNullable(issueDo.getSla().getSolutionDeadline());
        Optional<SLATimeLapse> slaReactionTimeLapse = Optional.ofNullable(issueDo.getSla().getActualReactionTimeLapse());
        Optional<SLATimeLapse> slaRecoveryTimeLapse = Optional.ofNullable(issueDo.getSla().getActualRecoveryTimeLapse());
        Optional<SLATimeLapse> slaSolutionTimeLapse = Optional.ofNullable(issueDo.getSla().getActualSolutionTimeLapse());
        List<String> fieldValues;
        if(!isFullReport) {
            fieldValues = new ArrayList<>(Arrays.asList(
                    issueDo.getIssueId(),
                    issueDo.getShortDescription(),
                    issueDo.getCurrentStatus().name(),
                    issueDo.getSeverity().getSeverity() + "",
                    issueDo.getSrNumber(),
                    date(slaSolutionDeadline.orElse(new SLADeadline()).getDeadline())
            ));
        } else {
            fieldValues = new ArrayList<>(Arrays.asList(
                    issueDo.getIssueId(),
                    date(issueDo.getCreationDate()),
                    issueDo.getShortDescription(),
                    issueDo.getCurrentStatus().name(),
                    issueDo.getSeverity().getSeverity() + "",
                    issueDo.getSrNumber(),
                    date(slaSolutionDeadline.orElse(new SLADeadline()).getDeadline()),
                    date(slaSolutionTimeLapse.orElse(new SLATimeLapse()).getDate()),
                    slaReactionTimeLapse.orElse(new SLATimeLapse()).getTimeLapseStr(),
                    slaRecoveryTimeLapse.orElse(new SLATimeLapse()).getTimeLapseStr(),
                    slaSolutionTimeLapse.orElse(new SLATimeLapse()).getTimeLapseStr()
            ));
        }
        return fieldValues;
    }

    /**
     * Сгенерировать отчет xls и сохранить в фалй
     *
     * @param excelFilePath путь для файла xls
     * @param rowColumnMap  значения для отчета
     * @throws Exception
     */
    public static void saveReportToLocalExcel2003(String excelFilePath, Map<Integer, List<String>> rowColumnMap) throws Exception {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        Sheet sheet = hssfWorkbook.createSheet();
        generateReportToExcel2003(rowColumnMap, sheet);
        try (FileOutputStream outputStream = new FileOutputStream(excelFilePath + genFilename())) {
            hssfWorkbook.write(outputStream);
        }
    }

    /**
     * Сгенерировать отчет xls в виде строки
     *
     * @param rowColumnMap  значения для отчета
     * @throws Exception
     */
    public static ByteString excel2003ToBytes(Map<Integer, List<String>> rowColumnMap) {
        try {
            HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
            Sheet sheet = hssfWorkbook.createSheet();
            generateReportToExcel2003(rowColumnMap, sheet);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            hssfWorkbook.write(outputStream);
            return ByteString.fromArray(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequest(e);
        }
    }

    /**
     * Сгенерировать список задач по запросу.
     *
     * @param isFull  признак полного отчета
     * @param rowColumnMap  значения для отчета
     * @throws Exception
     */
    public List<JiraTask> reportToList(Boolean isFull, Map<Integer, List<String>> rowColumnMap) {
        List<JiraTask> res = new ArrayList<>();
        // Первые 2 строчки - заголовки
        // TODO: заголовки вставлять только при печати в Excel
        if (rowColumnMap != null) {
            List<String> columnDefs = isFull ? EXCEL_REPORT_FULL_COLUMNS : EXCEL_REPORT_SIMPLE_COLUMNS;
            res = rowColumnMap.values().stream()
                    .skip(2)
                    .map(value -> rowColumnMapRecordToTask(columnDefs, value))
                    .collect(Collectors.toList());
        }
        return res;
    }

    private JiraTask rowColumnMapRecordToTask(List<String> columnDef, List<String> values) {
        JiraTaskFieldAliases aliases = jira.getConfig().getJiraTaskFieldAliases();
        return JiraTask.builder()
                .closeDate(JiraConfig.getOptionalValue(values, JiraConfig.indexOf(columnDef, aliases.getCloseDate())))
                .key(JiraConfig.getOptionalValue(values, JiraConfig.indexOf(columnDef, aliases.getKey())))
                .comment(JiraConfig.getOptionalValue(values, JiraConfig.indexOf(columnDef, aliases.getComment())))
                .name(JiraConfig.getOptionalValue(values, JiraConfig.indexOf(columnDef, aliases.getName())))
                .priority(JiraConfig.getOptionalValue(values, JiraConfig.indexOf(columnDef, aliases.getPriority())))
                .srnumber(JiraConfig.getOptionalValue(values, JiraConfig.indexOf(columnDef, aliases.getSrnumber())))
                .startDate(JiraConfig.getOptionalValue(values, JiraConfig.indexOf(columnDef, aliases.getStartDate())))
                .status(JiraConfig.getOptionalValue(values, JiraConfig.indexOf(columnDef, aliases.getStatus())))
                .build();
    }

    public static String genFilename() {
        return DateTimeFormatter.ofPattern("'report_'YYYYMMddHHmmss'.xls'").format(LocalDateTime.now());
    }

    /**
     * Сгенерировать отчет xls
     *
     * @param rowColumnMap  значения для отчета
     * @param sheet страница книги Excel
     */
    private static void generateReportToExcel2003(Map<Integer, List<String>> rowColumnMap, @NotNull Sheet sheet) {
        if(rowColumnMap == null) {
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue("По заданному запросу не смогло сформироваться ни одного объекта инцидента из jira");
        } else {
            rowColumnMap.forEach((rowNumber, rowValues) -> {
                Row row = sheet.createRow(rowNumber);
                int columnNumber = 0;
                for (String value : rowValues) {
                    Cell cell = row.createCell(columnNumber);
                    cell.setCellValue(value);
                    columnNumber++;
                }
            });
        }
    }

    public CompletionStage<List<JiraAnalyticsData>> graphics(LocalDate dateFrom, LocalDate dateTo)
            throws TransportException {
        /*
        GraphicsResponse response = GraphicsResponse.builder()
                .jiraTaskStatusData(new ArrayList<>())
                .jiraTaskNewStatusData(new ArrayList<>())
                .jiraTaskOpenedClosedData(new ArrayList<>())
                .jiraTaskOpenedRejectedData(new ArrayList<>())
                .build();
         */
        //TODO:
        List<JiraAnalyticsData> response = new TreeList<>();
        IssueBo issueBo = new IssueBo(jira);
        try {
            List<IssueDo> allIssues = issueBo.getAllInc(dateFrom, dateTo);
            LOGGER.info("Preparing all statuses list...");
            Map<String, Map<LocalDate, List<IssueStatus>>> allStatuses = allIssues.stream().collect(Collectors.toMap(
                    IssueDo::getIssueId,
                    issue -> issue.getIssueStatusList().stream()
                            .map(IssueStatusDo::getCreationDateAsLocal)
                            .map(LocalDate::atStartOfDay)
                            .map(LocalDateTime::toLocalDate)
                            .collect(Collectors.toSet())
                            .stream()
                            .collect(Collectors.toMap(
                                    dt -> dt,
                                    dt -> issue.getIssueStatusList().stream()
                                            .filter(status -> dt.equals(status.getCreationDateAsLocal().atStartOfDay().toLocalDate()))
                                            .map(IssueStatusDo::getStatusTo)
                                            .collect(Collectors.toList())
                            ))

            ));
            LOGGER.info("Preparing all statuses list...done, task count= " + allStatuses.size());
            for (LocalDate date = dateFrom; date.isBefore(dateTo.plusDays(1)); date = date.plusDays(1)) {
                Integer createdCount = 0;
                Integer analysisCount = 0;
                Integer developCount = 0;
                Integer onTestCount = 0;
                Integer waitingExternalCount = 0;
                Integer addInfoCount = 0;
                Integer closedCount = 0;
                Integer closedWithoutDevCount = 0;
                Integer invalidCount = 0;
                // Новые
                for (IssueDo issueDo: allIssues) {
                    if (date.equals(issueDo.getCreationDateAsLocal().atStartOfDay().toLocalDate())) {
                        createdCount ++;
                    }
                }
                for (Map<LocalDate, List<IssueStatus>> issueStatuses: allStatuses.values()) {
                    if (issueStatuses.get(date) != null) {
                        for(IssueStatus status: issueStatuses.get(date))
                            switch (status.getStatusCode()) {
                                case IssueStatusCodes.TODO: createdCount++; break;
                                case IssueStatusCodes.ANALYSIS: analysisCount++; break;
                                case IssueStatusCodes.DEVELOP: developCount++; break;
                                case IssueStatusCodes.ON_TEST: onTestCount++; break;
                                case IssueStatusCodes.WAITING_EXTERNAL: waitingExternalCount++; break;
                                case IssueStatusCodes.ADD_INFO: addInfoCount++; break;
                                case IssueStatusCodes.CLOSED: closedCount++; break;
                                case IssueStatusCodes.CLOSED_WITHOUT_DEV: closedWithoutDevCount++; break;
                                case IssueStatusCodes.INVALID: invalidCount++; break;
                            }
                    }
                }
                response.add(JiraAnalyticsData.builder()
                        .date(date)
                        .todo(createdCount)
                        .invalid(invalidCount)
                        .closed(closedCount)
                        .closedWithoutDev(closedWithoutDevCount)
                        .addInfo(addInfoCount)
                        .waitingExternal(waitingExternalCount)
                        .analysis(analysisCount)
                        .develop(developCount)
                        .onTest(onTestCount)
                        .build());
            }
            return completedFuture(response);
        } catch (Exception e) {
            LOGGER.error(String.format("%s%s", "Cant retrieve analytics data:", e.getLocalizedMessage()));
            e.printStackTrace();
            throw new BadRequest(e);
        }
    }

    public static class RunParametersNotFound extends TransportException {
        public RunParametersNotFound() {
            super(ProtocolError, "Run parameter not found!");
        }

        public RunParametersNotFound(String params) {
            super(ProtocolError, String.format("Run parameter not found: %s", params));
        }
    }
}
