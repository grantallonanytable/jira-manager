package ru.shadewallcorp.jiraTask.jiraManager.application;


import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.shadewallcorp.jiraTask.jiraManager.api.ReportRequest;
import ru.shadewallcorp.jiraTask.jiraManager.api.SetScrumRequest;
import ru.shadewallcorp.jiraTask.jiraManager.api.JiraManagerResponse;
import ru.shadewallcorp.jiraTask.jiraManager.api.UpdateDeadlineRequest;
import ru.shadewallcorp.jiraTask.jiraManager.api.UpdateLabelsRequest;
import ru.shadewallcorp.jiraTask.jiraManager.util.ReportGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import java.util.concurrent.Callable;

/**
 * Implementation.
 *
 * @author Dilvish  {@literal <dilvish@newmail.ru>}
 */
@RestController
public class JiraManagerService {

    /**
     * UPDATE_LABELS.
     * @return 
     */
    @PutMapping(value = "/api/jira-manager/updateLabels")
    public Callable<JiraManagerResponse> updateLabels(@RequestBody UpdateLabelsRequest request) {
        return () -> {
            ReportGenerator.checkParamsWithDefaults(request);
            ReportGenerator.UPDATE_LABELS.accept(request.getUserName(), request.getPassword());
            return JiraManagerResponse.builder().build();
        };
    }

    /**
     * UPDATE_DEADLINE.
     * @return 
     */
    @PutMapping(value = "/api/jira-manager/updateDeadLine")
    public Callable<JiraManagerResponse> updateDeadLine(@RequestBody UpdateDeadlineRequest request) {
        return () -> {
            ReportGenerator.checkParamsWithDefaults(request);
            ReportGenerator.UPDATE_DEADLINE.accept(request.getUserName(), request.getPassword());
            return JiraManagerResponse.builder().build();
        };
    }

    /**
     * SET_SCRUM.
     * @return 
     */
    @PostMapping(value = "/api/jira-manager/setScrum")
    public Callable<JiraManagerResponse> setScrum(@RequestBody SetScrumRequest request) {
        return () -> {
            ReportGenerator.checkParamsWithDefaults(request);
            ReportGenerator.SET_SCRUM.accept(request.getUserName(), request.getPassword());
            return JiraManagerResponse.builder().build();
        };
    }

    /**
     * Отчеты.
     * @return
     * Config: spring.mvc.async.request-timeout=500
     */
    @PostMapping(value = "/api/jira-manager/report")
    public Callable<ResponseEntity<byte[]>> report(@RequestBody ReportRequest request) {
        return () -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.ms-excel")
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + genFilename() + "\"")
                .body(ReportGenerator.reportAsStream(request).toByteArray());
    }

    /**
     * Отчеты.
     * @return
     * Config: spring.mvc.async.request-timeout=500
     */
    @GetMapping(value = "/api/jira-manager/report")
    public Callable<ResponseEntity<byte[]>> report(@RequestParam(name = "userName", required = false)  String userName,
                                                   @RequestParam(name = "password", required = false)  String password,
                                                   @RequestParam(name = "issueId", required = false)  String issueId,
                                                   @RequestParam(name = "isFull", required = false)  Boolean isFull,
                                                   @RequestParam(name = "dateFrom", required = false)  Date dateFrom,
                                                   @RequestParam(name = "dateTo", required = false)  Date dateTo) {
        return () -> ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.ms-excel")
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + genFilename() + "\"")
                .body(ReportGenerator.reportAsStream(userName, password, issueId, isFull, dateFrom, dateTo).toByteArray());
    }

    public static String genFilename() {
        return DateTimeFormatter.ofPattern("'report_'YYYYMMddHHmmss'.xls'").format(LocalDateTime.now());
    }

}

