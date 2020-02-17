package ru.shadewallcorp.jiraTasks.jiraManager.application;

import ru.shadewallcorp.jiraTasks.jiraManager.util.JiraRestJavaClientWrapper;
import ru.shadewallcorp.jiraTasks.jiraManager.util.ReportGenerator;
import java.util.Arrays;

import static ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil.date;
import static ru.shadewallcorp.jiraTasks.jiraManager.util.Action.GENERATE_FULL_REPORT;
import static ru.shadewallcorp.jiraTasks.jiraManager.util.Action.GENERATE_REPORT_BY_ISSUE;
import static ru.shadewallcorp.jiraTasks.jiraManager.util.Action.GENERATE_SHORT_REPORT;
import static ru.shadewallcorp.jiraTasks.jiraManager.util.Action.SET_SCRUM;
import static ru.shadewallcorp.jiraTasks.jiraManager.util.Action.UPDATE_DEADLINE;
import static ru.shadewallcorp.jiraTasks.jiraManager.util.Action.UPDATE_LABELS;
import static ru.shadewallcorp.jiraTasks.jiraManager.util.Action.getAction;
import static ru.shadewallcorp.jiraTasks.jiraManager.util.Action.getAllActions;

/**
 * Класс для запуска сервисов JIRA Manager
 * TODO: убрать, когда будет работать Controller
 *
 */
public class Main {

    static final String ISSUE_REGEXP = "^TDS-[0-9]{4,5}$";
    static final String  DATE_REGEXP = "^[0-9]{2}-[0-9]{2}-[0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2}$";

    /**
     * Запускает сервисы JIRA Manager.
     * @param args аргументы
     *             args[0] - название действия (UPDATE_DEADLINE, UPDATE_LABELS, GENERATE_REPORT_BY_ISSUE, GENERATE_SHORT_REPORT, GENERATE_FULL_REPORT, SET_SCRUM)
     *             args[1] - имя пользователя в jira
     *             args[2] - пароль в jira
     *             args[3] - номер тикета в jira (TDS-*****)
     *             args[4] - dateFrom (dd-MM-yyyy HH:mm:ss)
     *             args[5] - dateTo (dd-MM-yyyy HH:mm:ss)
     *             args[6] - путь для создания отчета
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if(args.length < 7)
            throw new StartJarException("количество аргументов должно быть 7");
        if(!args[3].matches(ISSUE_REGEXP))
            throw new StartJarException("не верный формат номера issueId - " + args[3]);
        if (!args[4].matches(DATE_REGEXP))
            throw new StartJarException("не верный формат dateFrom - " + args[4]);
        if (!args[5].matches(DATE_REGEXP))
            throw new StartJarException("не верный формат dateTo - " + args[5]);

        String action = args[0];
        String userName = args[1];
        String password = args[2];
        String issueId = args[3];
        String dateFrom = args[4];
        String dateTo = args[5];
        String reportPath = args[6];

        ReportGenerator reportGenerator = new ReportGenerator(new JiraRestJavaClientWrapper());
        if(getAction(action) == UPDATE_LABELS)
            ReportGenerator.run(reportGenerator.UPDATE_LABELS, args[1], args[2]);
        if(getAction(action) == UPDATE_DEADLINE)
            ReportGenerator.run(reportGenerator.UPDATE_DEADLINE, args[1], args[2]);
        if(getAction(action) == SET_SCRUM)
            ReportGenerator.run(reportGenerator.SET_SCRUM, args[1], args[2]);
        if(getAction(action) == GENERATE_REPORT_BY_ISSUE)
            reportGenerator.saveReportToLocalExcel2003(issueId, false, null, null, reportPath);
        if(getAction(action) == GENERATE_FULL_REPORT)
            reportGenerator.saveReportToLocalExcel2003(null, true, date(dateFrom), date(dateTo), reportPath);
        if(getAction(action) == GENERATE_SHORT_REPORT)
            reportGenerator.saveReportToLocalExcel2003(null, false, date(dateFrom), date(dateTo), reportPath);
    }

    public static class StartJarException extends Exception {
        private final static String DEFAULT_ERROR = "Некорректно заданы входные значения (%s), они должны задаваться в следующем виде:\n " +
                "название действия [%s], имя пользователя в jira, пароль в jira, номер тикета в jira (TDS-****), " +
                "dateFrom (dd-MM-yyyy HH:mm:ss), dateTo (dd-MM-yyyy HH:mm:ss), путь для создания отчета";

        public StartJarException() {
            super(String.format(DEFAULT_ERROR, "<не указано>", Arrays.toString(getAllActions())));
        }
        public StartJarException(String arg) {
            super(String.format(DEFAULT_ERROR, arg, Arrays.toString(getAllActions())));
        }
    }
}
