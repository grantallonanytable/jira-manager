package ru.shadewallcorp.jiraTasks.jiraManager.api;

import akka.NotUsed;
import akka.util.ByteString;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.api.transport.Method;
import com.lightbend.lagom.javadsl.api.transport.TransportException;
import io.netty.handler.timeout.TimeoutException;
import org.taymyr.lagom.javadsl.openapi.OpenAPIService;
import ru.shadewallcorp.jiraTasks.jiraManager.api.request.TasksRequest;
import ru.shadewallcorp.jiraTasks.jiraManager.api.request.SetScrumRequest;
import ru.shadewallcorp.jiraTasks.jiraManager.api.request.UpdateDeadlineRequest;
import ru.shadewallcorp.jiraTasks.jiraManager.api.request.UpdateLabelsRequest;
import ru.shadewallcorp.jiraTasks.jiraManager.api.response.JiraAnalyticsData;
import ru.shadewallcorp.jiraTasks.jiraManager.api.response.JiraTask;
import ru.shadewallcorp.jiraTasks.jiraManager.api.response.JiraManagerResponse;
import ru.shadewallcorp.jiraTasks.jiraManager.application.BinaryFileSerializer;

import java.util.List;
import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;

/**
 * Сервис jira-manager.
 *
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
public interface JiraManagerService extends OpenAPIService {

    /** Наименования сервиса. */
    String SERVICE_NAME = "jira-manager";

    /**
     * UPDATE_LABELS.
     * @return {@link ServiceCall}
     */
    ServiceCall<UpdateLabelsRequest, JiraManagerResponse> updateLabels() throws TimeoutException;

    /**
     * SET_SCRUM.
     * @return {@link ServiceCall}
     */
    ServiceCall<SetScrumRequest, JiraManagerResponse> setScrum() throws TimeoutException;

    /**
     * UPDATE_DEADLINE.
     * @return {@link ServiceCall}
     */
    ServiceCall<UpdateDeadlineRequest, JiraManagerResponse> updateDeadLine() throws TimeoutException;

    /**
     * Список задач, формируется по тем же параметрам, что и отчет.
     * @return {@link ServiceCall}
     */
    ServiceCall<TasksRequest, List<JiraTask>> tasks();

    /**
     * Аналитические данные для графиков.
     * @return {@link ServiceCall}
     */
    ServiceCall<NotUsed, List<JiraAnalyticsData>> graphics(Optional<String> dateFrom, Optional<String> dateTo);

    /**
     * Отчеты.
     * @return {@link ServiceCall}
     * @param issueId
     * @param isFull
     * @param dateFrom
     * @param dateTo
     */
    ServiceCall<NotUsed, ByteString> report(
            Optional<String> issueId, Optional<Boolean> isFull, Optional<String> dateFrom, Optional<String> dateTo)
            throws TransportException;

    @Override
    default Descriptor descriptor() {
        return withOpenAPI(named(SERVICE_NAME)
                .withCalls(
                        restCall(Method.POST, "/api/jira-manager/updateLabels", this::updateLabels),
                        restCall(Method.POST, "/api/jira-manager/updateDeadLine", this::updateDeadLine),
                        restCall(Method.POST, "/api/jira-manager/setScrum", this::setScrum),
                        restCall(Method.POST,"/api/jira-manager/tasks", this::tasks),
                        restCall(Method.GET,"/api/jira-manager/graphics?dateFrom&dateTo", this::graphics),
                        restCall(Method.GET,"/api/jira-manager/report?issueId&isFull&dateFrom&dateTo", this::report)
                        .withResponseSerializer(new BinaryFileSerializer()))
                .withAutoAcl(true));
        // Сериализатор нужен для того, чтобы лагом не конвертировал по умолчанию в json
        // Метод GET для тестирования
    }

}
