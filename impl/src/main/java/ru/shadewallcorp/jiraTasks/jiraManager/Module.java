package ru.shadewallcorp.jiraTasks.jiraManager;

import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import play.api.libs.ws.WSClient;
import play.api.libs.ws.ahc.AsyncHttpClientProvider;
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient;
import ru.shadewallcorp.jiraTasks.jiraManager.application.JiraManagerServiceImpl;
import ru.shadewallcorp.jiraTasks.jiraManager.api.JiraManagerService;
import ru.shadewallcorp.jiraTasks.jiraManager.util.ReportGenerator;

/**
 * JIRA Manager module.
 * https://github.com/lagom/lagom-recipes/blob/master/mixed-persistence/mixed-persistence-java-sbt/hello-impl/src/main/java/com/lightbend/
 *   lagom/recipes/mixedpersistence/hello/impl/HelloModule.java
 *
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
public class Module extends AbstractModule implements ServiceGuiceSupport {

    @Override
    protected void configure() {
        bindService(JiraManagerService.class, JiraManagerServiceImpl.class);
        bind(ReportGenerator.class).asEagerSingleton();
        // Logging of outgoing requests
        bind(AsyncHttpClient.class).toProvider(AsyncHttpClientProvider.class);
        bind(WSClient.class).toProvider(ConfiguredAhcWSClientProvider.class);

    }
}
