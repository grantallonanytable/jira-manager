package ru.shadewallcorp.jiraTasks.jiraManager.application;

import akka.NotUsed;
import akka.util.ByteString;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.BadRequest;
import com.lightbend.lagom.javadsl.api.transport.MessageProtocol;
import com.lightbend.lagom.javadsl.api.transport.ResponseHeader;
import com.lightbend.lagom.javadsl.server.HeaderServiceCall;
import com.typesafe.config.Config;
import io.netty.handler.timeout.TimeoutException;
import org.apache.http.HttpStatus;
import org.pcollections.HashTreePMap;
import org.pcollections.TreePVector;
import org.taymyr.lagom.javadsl.openapi.AbstractOpenAPIService;
import play.mvc.Http;
import ru.shadewallcorp.jiraTasks.jiraManager.api.*;
import ru.shadewallcorp.jiraTasks.jiraManager.api.request.TasksRequest;
import ru.shadewallcorp.jiraTasks.jiraManager.api.request.SetScrumRequest;
import ru.shadewallcorp.jiraTasks.jiraManager.api.request.UpdateDeadlineRequest;
import ru.shadewallcorp.jiraTasks.jiraManager.api.request.UpdateLabelsRequest;
import ru.shadewallcorp.jiraTasks.jiraManager.api.response.GraphicsResponse;
import ru.shadewallcorp.jiraTasks.jiraManager.api.response.JiraAnalyticsData;
import ru.shadewallcorp.jiraTasks.jiraManager.api.response.JiraTask;
import ru.shadewallcorp.jiraTasks.jiraManager.api.response.JiraManagerResponse;
import ru.shadewallcorp.jiraTasks.jiraManager.date.DateUtil;
import ru.shadewallcorp.jiraTasks.jiraManager.util.ReportGenerator;

import javax.inject.Inject;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static ru.shadewallcorp.jiraTasks.jiraManager.util.ReportGenerator.genFilename;

/**
 * Implementation.
 *
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
public class JiraManagerServiceImpl extends AbstractOpenAPIService implements JiraManagerService {

    private ReportGenerator reportGenerator;
    
    @Inject
    public JiraManagerServiceImpl(Config config, ReportGenerator reportGenerator) {
        super(config);
        this.reportGenerator = reportGenerator;
    }

    /** Заголовок ответа для выгрузки файла отчета */
    private static ResponseHeader httpHeaderReportFile(String filename) {
        return new ResponseHeader(HttpStatus.SC_OK,
                new MessageProtocol(of("application/vnd.ms-excel"), empty(), empty()),
                HashTreePMap.from(Collections.singletonMap(Http.HeaderNames.CONTENT_DISPOSITION,
                        TreePVector.singleton("attachment;filename=\"" + filename + "\""))));

    }

    /**
     * UPDATE_LABELS.
     * @return {@link ServiceCall}
     */
    @Override
    public ServiceCall<UpdateLabelsRequest, JiraManagerResponse> updateLabels() throws TimeoutException {
        return request -> reportGenerator.checkParamsWithDefaults(request)
                .thenRun(() -> reportGenerator.UPDATE_LABELS.accept(request.getUserName(), request.getPassword()))
                .thenApply(done -> JiraManagerResponse.OK);
    }

    /**
     * UPDATE_DEADLINE.
     * @return {@link ServiceCall}
     */
    @Override
    public ServiceCall<UpdateDeadlineRequest, JiraManagerResponse> updateDeadLine() throws TimeoutException {
        return request -> reportGenerator.checkParamsWithDefaults(request)
                .thenRun(() -> reportGenerator.UPDATE_DEADLINE.accept(request.getUserName(), request.getPassword()))
                .thenApply(done -> JiraManagerResponse.OK);
    }

    /**
     * SET_SCRUM.
     * @return {@link ServiceCall}
     */
    @Override
    public ServiceCall<SetScrumRequest, JiraManagerResponse> setScrum() throws TimeoutException {
        return request -> reportGenerator.checkParamsWithDefaults(request)
                .thenRun(() -> reportGenerator.SET_SCRUM.accept(request.getUserName(), request.getPassword()))
                .thenApply(done -> JiraManagerResponse.OK);
    }

    @Override
    public ServiceCall<TasksRequest, List<JiraTask>> tasks() {
        return request -> reportGenerator.checkParamsWithDefaults(request)
                .thenCompose(done -> reportGenerator.checkReportParams(request))
                .thenCompose(done -> completedFuture(reportGenerator.reportToList(request.getIsFull(),
                        reportGenerator.generateReportData(
                                request.getIssueId(), request.getIsFull(),
                                request.getDateFrom(), request.getDateTo()))));
    }

    @Override
    public HeaderServiceCall<NotUsed, ByteString> report(
            Optional<String> issueId, Optional<Boolean> isFull,
            Optional<String> dateFrom, Optional<String> dateTo) throws BadRequest {
        Date dateFrom1;
        Date dateTo1;
        try {
            dateFrom1 = dateFrom.isPresent() ? DateUtil.DATE_TIME_FORMAT.parse(dateFrom.get()) : null;
            dateTo1 = dateTo.isPresent() ? DateUtil.DATE_TIME_FORMAT.parse(dateTo.get()) : null;
        } catch (ParseException e) {
            throw new BadRequest(e);
        }
        return (header, notUsed) -> reportGenerator.report(
                issueId.orElse(null),
                isFull.orElse(null),
                dateFrom1, dateTo1, httpHeaderReportFile(genFilename()),
                ReportGenerator::excel2003ToBytes);
    }

    @Override
    public ServiceCall<NotUsed, List<JiraAnalyticsData>> graphics(Optional<String> dateFrom, Optional<String> dateTo) {
        LocalDate dateFrom1 = dateFrom.isPresent() ? LocalDate.parse(dateFrom.get(), DateUtil.DATE_FORMATTER) : null;
        LocalDate dateTo1 = dateTo.isPresent() ? LocalDate.parse(dateTo.get(), DateUtil.DATE_FORMATTER) : null;
        if (dateFrom1 == null || dateTo1 == null)
        {
            throw new ReportGenerator.RunParametersNotFound("dateFrom and dateTo must be specified!");
        }
        return notUsed -> reportGenerator.graphics(dateFrom1, dateTo1);
    }
}

