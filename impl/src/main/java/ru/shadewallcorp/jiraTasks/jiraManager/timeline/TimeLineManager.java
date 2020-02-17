package ru.shadewallcorp.jiraTasks.jiraManager.timeline;

import ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil;
import ru.shadewallcorp.jiraTasks.jiraManager.date.Time;
import ru.shadewallcorp.jiraTasks.jiraManager.sla.*;
import ru.shadewallcorp.jiraTasks.jiraManager.jira.issue.IssueDo;
import ru.shadewallcorp.jiraTasks.jiraManager.jira.issue.IssueStatusDo;
import ru.shadewallcorp.jiraTasks.jiraManager.jira.issue.Severity;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import static ru.shadewallcorp.jiraTasks.jiraManager.jira.issue.IssueStatus.*;

/**
 * Менеджер для расчета времени выполнения пунктов sla по инцидентам
 *
 */
public class TimeLineManager {
    /**
     * Рассчитывает времена выполнения пунктов SLA по инцидентам
     * <p>
     * Время реакции - это разница между временем первого попадания инцидента в статус Analysis
     * и временем заведения инцидента.
     * Если инцидент еще не попадал в статус Analysis, то вместо времени попадания инцидента в этот статус
     * берется текущее время.
     * <p>
     * Время восстановления - это разница между временем попадания инцидента в статус Ready for development
     * и временем заведения инцидента, с вычетом пребывания инцидента в статусах WAITING EXTERNAL и Доп.инфо
     * <p>
     * Время решения - это разница между временем попадания инцидента в статус Релиз
     * и временем заведения инцидента, с вычетом пребывания инцидента в статусах WAITING EXTERNAL и Доп.инфо
     *
     * @param issue тикет в jira
     * @return объект с временами выполнения пунктов sla по инцидентам
     */
    public static SLA getTimeLine(IssueDo issue) throws ParseException {
        SLA timeLine = new SLA();
        SLATimeLapse reaction = getSLATimeLapse(
                issue.getSeverity(),
                issue.getCreationDate(),
                issue.getIssueStatusList(),
                SLAActionType.REACTION_ACTION
        );
        SLATimeLapse recovery = getSLATimeLapse(
                issue.getSeverity(),
                issue.getCreationDate(),
                issue.getIssueStatusList(),
                SLAActionType.RECOVERY_ACTION
        );
        SLATimeLapse solution = getSLATimeLapse(
                issue.getSeverity(),
                issue.getCreationDate(),
                issue.getIssueStatusList(),
                SLAActionType.SOLUTION_ACTION
        );
        if (reaction == null)
            timeLine.setActualReactionTimeLapse(null);
        else {
            SLATimeType slaTimeType = issue.getSeverity().getSla().getMaxReactionTimeLapse().getSlaTimeType();
            reaction.setTimeLapse(reaction.getTimeLapse());
            reaction.setSlaTimeType(slaTimeType);
            timeLine.setActualReactionTimeLapse(reaction);
        }
        if (recovery == null)
            timeLine.setActualRecoveryTimeLapse(null);
        else {
            SLATimeType slaTimeType = issue.getSeverity().getSla().getMaxRecoveryTimeLapse().getSlaTimeType();
            Time needInfoTime = getNeedInfoTimeLapse(issue.getIssueStatusList(), slaTimeType);
            recovery.setTimeLapse(diffTime(recovery.getTimeLapse(), needInfoTime, slaTimeType));
            recovery.setSlaTimeType(slaTimeType);
            timeLine.setActualRecoveryTimeLapse(recovery);
            // Если по ошибке перевели сразу в разработку, то время реакции равно времени восстановления
            if(reaction == null)
                timeLine.setActualReactionTimeLapse(timeLine.getActualRecoveryTimeLapse());
        }
        if (solution == null)
            timeLine.setActualSolutionTimeLapse(null);
        else {
            SLATimeType slaTimeType = issue.getSeverity().getSla().getMaxSolutionTimeLapse().getSlaTimeType();
            Time needInfoTime = getNeedInfoTimeLapse(issue.getIssueStatusList(), slaTimeType);
            solution.setTimeLapse(diffTime(solution.getTimeLapse(), needInfoTime, slaTimeType));
            solution.setSlaTimeType(slaTimeType);
            timeLine.setActualSolutionTimeLapse(solution);
        }
        return timeLine;
    }

    /**
     * Рассчитывает дедлайн по sla
     *
     * @param issue тикет в jira
     * @return объект в дадлайнами по sla
     * @throws ParseException
     */
    public static SLA getDeadLine(IssueDo issue) throws ParseException {
        SLA timeLine = new SLA();
        SLADeadline reaction;
        SLADeadline recovery;
        SLADeadline solution;
        reaction = getSLADeadline(
                issue.getSeverity(),
                issue.getCreationDate(),
                SLAActionType.REACTION_ACTION
        );
        recovery = getSLADeadline(
                issue.getSeverity(),
                issue.getCreationDate(),
                SLAActionType.RECOVERY_ACTION
        );
        solution = getSLADeadline(
                issue.getSeverity(),
                issue.getCreationDate(),
                SLAActionType.SOLUTION_ACTION
        );
        if (reaction == null)
            timeLine.setReactionDeadline(null);
        else {
            SLATimeType slaTimeType = issue.getSeverity().getSla().getMaxReactionTimeLapse().getSlaTimeType();
            reaction.setDeadline(reaction.getDeadline());
            reaction.setSlaTimeType(slaTimeType);
            timeLine.setReactionDeadline(reaction);
        }
        if (recovery == null)
            timeLine.setRecoveryDeadline(null);
        else {
            SLATimeType slaTimeType = issue.getSeverity().getSla().getMaxRecoveryTimeLapse().getSlaTimeType();
            Time needInfoTime = getNeedInfoTimeLapse(issue.getIssueStatusList(), slaTimeType);
            recovery.setDeadline(addTime(recovery.getDeadline(), needInfoTime, slaTimeType));
            recovery.setSlaTimeType(slaTimeType);
            timeLine.setRecoveryDeadline(recovery);
        }
        if (solution == null)
            timeLine.setSolutionDeadline(null);
        else {
            SLATimeType slaTimeType = issue.getSeverity().getSla().getMaxSolutionTimeLapse().getSlaTimeType();
            Time needInfoTime = getNeedInfoTimeLapse(issue.getIssueStatusList(), slaTimeType);
            solution.setDeadline(addTime(solution.getDeadline(), needInfoTime, slaTimeType));
            solution.setSlaTimeType(slaTimeType);
            timeLine.setSolutionDeadline(solution);
        }
        return timeLine;
    }

    /**
     * Рассчитывает время реакции/восстановления/решения
     *
     * @param severity          критичность инцидента
     * @param issueCreationDate дата создания цинцидента
     * @param issueStatusList   список статусов, в которые переходил инцидент
     * @return объект с временем реакции/восстановления/решения (если null, то действие реакции/восстановления/решения еще не произошло)
     * @throws ParseException
     */
    private static SLATimeLapse getSLATimeLapse(Severity severity, Date issueCreationDate, List<IssueStatusDo> issueStatusList,
                                                SLAActionType slaActionType) throws ParseException {
        SLATimeLapse slaTimeLapse = null;
        SLATimeType slaTimeType = null;
        if (slaActionType == SLAActionType.REACTION_ACTION) {
            SLATimeLapse slaMaxTimeLapse = severity.getSla().getMaxReactionTimeLapse();
            if (slaMaxTimeLapse == null) return null;
            slaTimeType = slaMaxTimeLapse.getSlaTimeType();
        }
        if (slaActionType == SLAActionType.RECOVERY_ACTION) {
            SLATimeLapse slaMaxTimeLapse = severity.getSla().getMaxRecoveryTimeLapse();
            if (slaMaxTimeLapse == null) return null;
            slaTimeType = slaMaxTimeLapse.getSlaTimeType();
        }
        if (slaActionType == SLAActionType.SOLUTION_ACTION) {
            SLATimeLapse slaMaxTimeLapse = severity.getSla().getMaxSolutionTimeLapse();
            if (slaMaxTimeLapse == null) return null;
            slaTimeType = slaMaxTimeLapse.getSlaTimeType();
        }
        for (IssueStatusDo issueStatus : issueStatusList) {
            // если тип - реакция, то индиактор - статус Analysis
            // если тип - восстановление, то индикатор - статус Ready for development
            // если тип - решение, то индикатор - стутус Релиз (также указан статус READY TO MERGE на случай
            // некорректного перевода по wf и статус Закрыт на случай прохождения тикета мимо wf)
            if ((slaActionType == SLAActionType.REACTION_ACTION && issueStatus.getStatusTo() == ANALYSIS)
                    || (slaActionType == SLAActionType.RECOVERY_ACTION && issueStatus.getStatusTo() == READY_FOR_DEVELOPMENT)
                    || (slaActionType == SLAActionType.SOLUTION_ACTION && (issueStatus.getStatusTo() == READY_TO_RELEASE
                        || issueStatus.getStatusTo() == READY_TO_MERGE || issueStatus.getStatusTo() == CLOSED))) {
                slaTimeLapse = new SLATimeLapse(
                        issueStatus.getCreationDate(),
                        getTimeLapse(issueCreationDate, issueStatus.getCreationDate(), slaTimeType),
                        slaTimeType
                );
                if(slaActionType != SLAActionType.RECOVERY_ACTION)
                    break;
            }
        }
        return slaTimeLapse;
    }

    /**
     * Рассчитывает дедлайн реакции/восстановления/решения
     *
     * @param severity          критичность инцидента
     * @param issueCreationDate дата создания цинцидента
     * @return объект с дедлайном реакции/восстановления/решения (если null, то действие реакции/восстановления/решения еще не произошло)
     * @throws ParseException
     */
    private static SLADeadline getSLADeadline(Severity severity, Date issueCreationDate,
                                              SLAActionType slaActionType) throws ParseException {
        SLADeadline slaDeadline = null;
        if (slaActionType == SLAActionType.REACTION_ACTION) {
            SLATimeLapse slaTimeLapse = severity.getSla().getMaxReactionTimeLapse();
            if (slaTimeLapse == null) return null;
            slaDeadline = new SLADeadline();
            slaDeadline.setSlaTimeType(slaTimeLapse.getSlaTimeType());
            slaDeadline.setDeadline(
                    addTime(
                            issueCreationDate,
                            slaTimeLapse.getTimeLapse(),
                            slaDeadline.getSlaTimeType()
                    )
            );
        }
        if (slaActionType == SLAActionType.RECOVERY_ACTION) {
            SLATimeLapse slaTimeLapse = severity.getSla().getMaxRecoveryTimeLapse();
            if (slaTimeLapse == null) return null;
            slaDeadline = new SLADeadline();
            slaDeadline.setSlaTimeType(slaTimeLapse.getSlaTimeType());
            slaDeadline.setDeadline(
                    addTime(
                            issueCreationDate,
                            slaTimeLapse.getTimeLapse(),
                            slaDeadline.getSlaTimeType()
                    )
            );
        }
        if (slaActionType == SLAActionType.SOLUTION_ACTION) {
            SLATimeLapse slaTimeLapse = severity.getSla().getMaxSolutionTimeLapse();
            if (slaTimeLapse == null) return null;
            slaDeadline = new SLADeadline();
            slaDeadline.setSlaTimeType(slaTimeLapse.getSlaTimeType());
            slaDeadline.setDeadline(
                    addTime(
                            issueCreationDate,
                            slaTimeLapse.getTimeLapse(),
                            slaDeadline.getSlaTimeType()
                    )
            );
        }
        return slaDeadline;
    }

    /**
     * Возвращает время нахождения инцидента в колонке NeedInfo
     *
     * @param statusDoList список статусов
     * @param slaTimeType  тип временного отрезка
     * @return время нахождения инцидента в колонке NeedInfo
     * @throws ParseException
     */
    private static Time getNeedInfoTimeLapse(List<IssueStatusDo> statusDoList, SLATimeType slaTimeType) throws ParseException {
        Date previousNeedInfoDate = null;
        Time time = new Time(0, 0, 0);
        boolean isNeedInfo = false;
        for (IssueStatusDo statusDo : statusDoList) {
            if (statusDo.getStatusTo() == ADD_INFO || statusDo.getStatusTo() == WAITING_EXTERNAL) {
                if (!isNeedInfo) previousNeedInfoDate = statusDo.getCreationDate();
                isNeedInfo = true;
            } else {
                if (isNeedInfo)
                    time = sumTime(time, getTimeLapse(previousNeedInfoDate, statusDo.getCreationDate(), slaTimeType), slaTimeType);
                isNeedInfo = false;
            }
        }
        if (isNeedInfo)
            time = sumTime(time, getTimeLapse(previousNeedInfoDate, new Date(), slaTimeType), slaTimeType);
        return time;
    }

    /**
     * Рассчитывает разницу между двумя датами с учетом типа временного отрезка
     *
     * @param startDate   начальная дата
     * @param endDate     конечная дата
     * @param slaTimeType тип временного отрезка
     * @return разницу между двумя датами
     * @throws ParseException
     */
    private static Time getTimeLapse(Date startDate, Date endDate, SLATimeType slaTimeType) throws ParseException {
        if (slaTimeType == SLATimeType.WORK_DAYS_WITHOUT_HOLIDAYS)
            return DateUtil.diffWorkDate(startDate, endDate, false);
        if (slaTimeType == SLATimeType.WORK_DAYS_WITH_HOLIDAYS)
            return DateUtil.diffWorkDate(startDate, endDate, true);
        return DateUtil.diffDate(startDate, endDate);
    }


    /**
     * Прибавляет время к дате
     *
     * @param date        дата
     * @param time        время
     * @param slaTimeType тип отрезка времени по sla
     * @return дату после прибавления времени к первоначальной дате
     * @throws ParseException
     */
    private static Date addTime(Date date, Time time, SLATimeType slaTimeType) throws ParseException {
        if (slaTimeType == SLATimeType.WORK_DAYS_WITHOUT_HOLIDAYS) {
            if (time.getDays() == 0) {
                return DateUtil.addWorkHours(date, time.getHours(), false);
            } else return DateUtil.addWorkDays(date, time.getDays(), false);
        }
        if (slaTimeType == SLATimeType.WORK_DAYS_WITH_HOLIDAYS) {
            if (time.getDays() == 0) {
                return DateUtil.addWorkHours(date, time.getHours(), true);
            } else return DateUtil.addWorkDays(date, time.getDays(), true);
        }
        if (slaTimeType == SLATimeType.DAY_AND_NIGHT) {
            if (time.getDays() == 0) {
                return DateUtil.addHoursAndMinutes(date, time.getHours(), time.getMinutes());
            } else return DateUtil.addDays(date, time.getDays());
        }
        return null;
    }

    /**
     * Вычисляет сумму двух отрезков времени
     *
     * @param firstTime   первое слагаемое
     * @param secondTime  второе слагаемое
     * @param slaTimeType тип временного отрезка
     * @return сумму двух отрезков времени
     * @throws ParseException
     */
    private static Time sumTime(Time firstTime, Time secondTime, SLATimeType slaTimeType) throws ParseException {
        if (slaTimeType == SLATimeType.WORK_DAYS_WITHOUT_HOLIDAYS || slaTimeType == SLATimeType.WORK_DAYS_WITH_HOLIDAYS)
            return DateUtil.sumTime(firstTime, secondTime, true);
        return DateUtil.sumTime(firstTime, secondTime, false);
    }

    /**
     * Вычисляет разницу между двумя отрезками времени
     *
     * @param firstTime   уменьшаемое
     * @param secondTime  вычетаемое
     * @param slaTimeType тип временного отрезка
     * @return разницу между двумя отрезками времени
     * @throws ParseException
     */
    private static Time diffTime(Time firstTime, Time secondTime, SLATimeType slaTimeType) throws ParseException {
        if (slaTimeType == SLATimeType.WORK_DAYS_WITHOUT_HOLIDAYS || slaTimeType == SLATimeType.WORK_DAYS_WITH_HOLIDAYS)
            return DateUtil.diffTime(firstTime, secondTime, true);
        return DateUtil.diffTime(firstTime, secondTime, false);
    }
}
