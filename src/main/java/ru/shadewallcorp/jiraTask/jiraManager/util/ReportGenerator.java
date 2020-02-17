package ru.shadewallcorp.jiraTask.jiraManager.util;

import lombok.NonNull;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import ru.shadewallcorp.jiraTask.jiraManager.api.ReportRequest;
import ru.shadewallcorp.jiraTask.jiraManager.api.JiraManagerRequest;
import ru.shadewallcorp.jiraTask.jiraManager.jira.issue.IssueBo;
import ru.shadewallcorp.jiraTask.jiraManager.jira.issue.IssueDo;
import ru.shadewallcorp.jiraTask.jiraManager.sla.SLADeadline;
import ru.shadewallcorp.jiraTask.jiraManager.sla.SLATimeLapse;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.BiConsumer;

import static ru.shadewallcorp.jiraTask.jiraManager.date.DateUtil.date;
import static ru.shadewallcorp.jiraTask.jiraManager.util.TimeoutController.execute;

/**
 * Генератор отчетов
 *

 */
@Service
public class ReportGenerator {
    @Value("${jira.userName}")
    private static String userName;
    @Value("${jira.password}")
    private static String password;

    public static final BiConsumer<String, String> UPDATE_LABELS = (userName, password) -> {
        try {
            new IssueBo(userName, password).updateLabels();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    public static final BiConsumer<String, String> SET_SCRUM = (userName, password) -> {
        try {
            new IssueBo(userName, password).setScrum();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    public static final BiConsumer<String, String> UPDATE_DEADLINE = (userName, password) -> {
        try {
            new IssueBo(userName, password).updateDeadline();
        } catch (Exception e) {
            e.printStackTrace();
        }
    };

    private static final long API_TIMEOUT_SECONDS = 1000;
    // TODO:
    private static final String REPORT_PATH = "/e/tmp";

    /**
     * Запуск задания.
     * run(SET_SCRUM, user, password)
     */
    public static void run(BiConsumer<String, String> consumer, String userName, String password) throws TaskTimeoutException {
        execute(() -> consumer.accept(userName, password), API_TIMEOUT_SECONDS);
    }

    public static void checkParamsWithDefaults(JiraManagerRequest request) {
        if (request.getUserName() == null) {
            request.withUserName(userName);
        }
        if (request.getPassword() == null) {
            request.withPassword(password);
        }
        if (request.getUserName() == null || request.getPassword() == null) {
            throw new RunParametersNotFound("both userName and password must be specified!");
        }
    }

    public static void checkReportParams(ReportRequest request) {
        if (request.getIssueId() == null &&
                (request.getIsFull() == null || request.getDateFrom() == null || request.getDateTo() == null))
        {
            throw new RunParametersNotFound("isFull, dateFrom and dateTo must be specified if issueId not specified!");
        }
    }

    public static ByteArrayOutputStream reportAsStream(
            String userName, String password, String issueId, Boolean isFull, Date dateFrom, Date dateTo) {
        return reportAsStream(ReportRequest.builder()
                .userName(userName)
                .password(password)
                .issueId(issueId)
                .isFull(isFull)
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .build());
    }

    public static ByteArrayOutputStream reportAsStream(ReportRequest request) {
        checkParamsWithDefaults(request);
        checkReportParams(request);
        return generateReportAsStream(request);
    }

    public static ByteArrayOutputStream generateReportAsStream(ReportRequest request) {
        // TODO: нельзя использовать интерфейсный тип в параметрах
        if (request.getIssueId() != null)
            return generateReportAsStream(request.getUserName(), request.getPassword(),
                    request.getIssueId(), null, null, false);
        else if (request.getIsFull())
            return generateReportAsStream(request.getUserName(), request.getPassword(),
                    null, request.getDateFrom(),  request.getDateTo(), true);
        else
            return generateReportAsStream(request.getUserName(), request.getPassword(),
                    null,  request.getDateFrom(),  request.getDateTo(), false);
    }

    private static ByteArrayOutputStream generateReportAsStream(String userName, String password, String issueId, Date dateFrom,
                                              Date dateTo, boolean isFullReport) {

        try {
            IssueBo issueBo = new IssueBo(userName, password);
            List<String> columnNames = new ArrayList<>(Arrays.asList("Код", "Тема", "Статус", "Приоритет",
                    "Номер SR", "Срок исполнения max",	"Комментарий"));
            if(issueId != null) {
                IssueDo issueDo = issueBo.getInc(issueId);
                return reportToStream(issueDo == null ?
                        null : getReportData(columnNames, new ArrayList<>(Collections.singletonList(issueDo)),
                        null, null, null, false));
            } else {
                List<IssueDo> resolvedIncList = issueBo.getResolvedInc(dateFrom, dateTo);
                List<IssueDo> closedWithoutDevIncList = issueBo.getClosedWithoutDevInc(dateFrom, dateTo);
                List<IssueDo> issueDoList = issueBo.getAllOpenInc();
                List<IssueDo> newIncList = issueBo.getNewInc(dateFrom, dateTo);
                return reportToStream(issueDoList.size() == 0 ?
                        null : getReportData(columnNames, issueDoList, newIncList, resolvedIncList,
                        closedWithoutDevIncList, isFullReport));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BadRequest("Cant generate report", e);
        }
    }

    public static void saveReport(String userName, String password, String issueId, Date dateFrom,
                                  Date dateTo, boolean isFullReport, String reportPath) throws TaskTimeoutException {
        Runnable task = () -> {
            try {
                IssueBo issueBo = new IssueBo(userName, password);
                List<String> columnNames = new ArrayList<>(Arrays.asList("Код", "Тема", "Статус", "Приоритет",
                        "Номер SR", "Срок исполнения max",	"Комментарий"));
                if(issueId != null) {
                    IssueDo issueDo = issueBo.getInc(issueId);
                    saveReport(reportPath, issueDo == null ?
                            null : getReportData(columnNames, new ArrayList<>(Collections.singletonList(issueDo)),
                            null, null, null, false));
                } else {
                    List<IssueDo> resolvedIncList = issueBo.getResolvedInc(dateFrom, dateTo);
                    List<IssueDo> closedWithoutDevIncList = issueBo.getClosedWithoutDevInc(dateFrom, dateTo);
                    List<IssueDo> issueDoList = issueBo.getAllOpenInc();
                    List<IssueDo> newIncList = issueBo.getNewInc(dateFrom, dateTo);
                    saveReport(reportPath, issueDoList.size() == 0 ?
                            null : getReportData(columnNames, issueDoList, newIncList, resolvedIncList,
                            closedWithoutDevIncList, isFullReport));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        execute(task, Integer.MAX_VALUE);
    }

    private static Map<Integer, List<String>> getReportData(List<String> columnNames, List<IssueDo> issueDoList,
                                                            List<IssueDo> newIncList, List<IssueDo> resolvedIncList,
                                                            List<IssueDo> closedWithoutDevIncList,
                                                            boolean isFullReport) {
        if(isFullReport)
            columnNames = new ArrayList<>(Arrays.asList("Код", "Дата создания", "Тема", "Статус", "Приоритет",
                    "Номер SR", "Срок исполнения max",	"Фактический срок исполнения", "Время реакции",
                    "Время восстановления", "Время решения"));
        Map<Integer, List<String>> res = new HashMap<>();
        res.put(0, new ArrayList<>(Collections.singletonList("Список открытых продуктивных дефектов")));
        res.put(1, columnNames);
        for(int i = 0; i < issueDoList.size(); i++) {
            res.put(i + 2, fillIncTable(issueDoList, i, isFullReport));
        }
        int maxKeyValue = Collections.max(res.entrySet(), Comparator.comparingInt(Map.Entry::getKey)).getKey();
        if(newIncList != null && resolvedIncList != null) {
            maxKeyValue += 2;
            res.put(++maxKeyValue, new ArrayList<>(Collections.singletonList("Список новых продуктивных дефектов")));
            res.put(++maxKeyValue, columnNames);
            for(int i = 0; i < newIncList.size(); i++) {
                res.put(++maxKeyValue, fillIncTable(newIncList, i, isFullReport));
            }
            maxKeyValue += 2;
            res.put(++maxKeyValue, new ArrayList<>(Collections.singletonList("Список решенных продуктивных дефектов")));
            res.put(++maxKeyValue, columnNames);
            for(int i = 0; i < resolvedIncList.size(); i++) {
                res.put(++maxKeyValue, fillIncTable(resolvedIncList, i, isFullReport));
            }
            maxKeyValue += 2;
            res.put(++maxKeyValue, new ArrayList<>(Collections.singletonList("Список закрытых без разработки продуктивных дефектов")));
            res.put(++maxKeyValue, columnNames);
            for(int i = 0; i < closedWithoutDevIncList.size(); i++) {
                res.put(++maxKeyValue, fillIncTable(closedWithoutDevIncList, i, isFullReport));
            }
            maxKeyValue += 2;
            res.put(++maxKeyValue, new ArrayList<>(Arrays.asList("Количество новых продуктивных дефектов", newIncList.size() + "")));
            res.put(++maxKeyValue, new ArrayList<>(Arrays.asList("Количество новых отклоненных продуктивных дефектов", (int) newIncList.stream().filter(IssueDo::isRejected).count() + "")));
            res.put(++maxKeyValue, new ArrayList<>(Arrays.asList("Количество решенных продуктивных дефектов", resolvedIncList.size() + "")));
            res.put(++maxKeyValue, new ArrayList<>(Arrays.asList("Количество закрытых без разработки продуктивных дефектов", closedWithoutDevIncList.size() + "")));
        }
        return res;
    }

    private static List<String> fillIncTable(List<IssueDo> issueDoList, int index, boolean isFullReport) {
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
    public static void saveReport(String excelFilePath, Map<Integer, List<String>> rowColumnMap) throws Exception {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        Sheet sheet = hssfWorkbook.createSheet();

        generateReport(rowColumnMap, sheet);

        try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
            hssfWorkbook.write(outputStream);
        }
    }

    /**
     * Сгенерировать отчет xls в виде строки
     *
     * @param rowColumnMap  значения для отчета
     * @throws Exception
     */
    public static ByteArrayOutputStream reportToStream(Map<Integer, List<String>> rowColumnMap) throws Exception {
        HSSFWorkbook hssfWorkbook = new HSSFWorkbook();
        Sheet sheet = hssfWorkbook.createSheet();

        generateReport(rowColumnMap, sheet);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        hssfWorkbook.write(outputStream);
        /* testing
        PrintWriter p = new PrintWriter(outputStream);
        p.println("aaabbbbcccc\ndddeeefff\n\ngggjjj");
        p.flush();
         */
        return outputStream;
    }

    /**
     * Сгенерировать отчет xls
     *
     * @param rowColumnMap  значения для отчета
     * @param sheet страница книги Excel
     */
    private static void generateReport(Map<Integer, List<String>> rowColumnMap, @NonNull Sheet sheet) {
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

    public static class RunParametersNotFound extends RejectedExecutionException {
        public RunParametersNotFound() {
            super("Run parameter not found!");
        }

        public RunParametersNotFound(String params) {
            super(String.format("Run parameter not found: %s", params));
        }
    }

    public static class BadRequest extends HttpClientErrorException {
        public BadRequest(String params, Exception e) {
            super(HttpStatus.BAD_REQUEST, "Cant generate report", null, e.getLocalizedMessage().getBytes(), null);
        }
    }

}
