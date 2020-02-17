package ru.shadewallcorp.jiraTasks.jiraManager.jira.client;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClientFactory;
import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.*;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.executor.ThreadLocalContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Замена стандартной фабрики, т.к. она не позволяет устанавливать таймауты.
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
public class JiraRestJavaClientFactory extends AsynchronousJiraRestClientFactory {

    public JiraRestClient createWithBasicHttpAuthentication(final URI serverUri,
                                                            final String username, final String password,
                                                            final HttpClientOptions httpClientOptions) {
        return new AsynchronousJiraRestClient(serverUri,
                createClient(serverUri, new BasicHttpAuthenticationHandler(username, password), httpClientOptions));
    }

    @SuppressWarnings("unchecked")
    private DisposableHttpClient createClient(final URI serverUri, 
                                              final AuthenticationHandler authenticationHandler,
                                              final HttpClientOptions httpClientOptions) {
        final HttpClientOptions options = (new HttpClientOptions());
        setAdditionalHttpClientParams(options, httpClientOptions);
        final DefaultHttpClientFactory defaultHttpClientFactory = new DefaultHttpClientFactory(new NoOpEventPublisher(),
                new RestClientApplicationProperties(serverUri),
                new ThreadLocalContextManager() {
                    @Override
                    public Object getThreadLocalContext() {
                        return null;
                    }

                    @Override
                    public void setThreadLocalContext(Object context) {
                    }

                    @Override
                    public void clearThreadLocalContext() {
                    }
                });

        final HttpClient httpClient = defaultHttpClientFactory.create(options);

        return new AtlassianHttpClientDecorator(httpClient, authenticationHandler) {
            @Override
            public void destroy() throws Exception {
                defaultHttpClientFactory.dispose(httpClient);
            }
        };
    }

    /**
     * Переопределить некоторые параметры для Jira Rest Java Client.
     * @param JRJCOptions
     * @param overridenOptions
     */
    private void setAdditionalHttpClientParams(HttpClientOptions JRJCOptions, HttpClientOptions overridenOptions) {
        // Надеюсь, никто не укажет миллиард миллиард миллиардов
        if (overridenOptions.getConnectionPoolTimeToLive() != 0) {
            JRJCOptions.setConnectionPoolTimeToLive((int) overridenOptions.getConnectionPoolTimeToLive(), TimeUnit.MILLISECONDS);
        }
        if (overridenOptions.getConnectionTimeout() != 0) {
            JRJCOptions.setConnectionTimeout((int) overridenOptions.getConnectionTimeout(), TimeUnit.MILLISECONDS);
        }
        if (overridenOptions.getSocketTimeout() != 0) {
            JRJCOptions.setSocketTimeout((int) overridenOptions.getSocketTimeout(), TimeUnit.MILLISECONDS);
        }
        if (overridenOptions.getRequestTimeout() != 0) {
            JRJCOptions.setRequestTimeout((int) overridenOptions.getRequestTimeout(), TimeUnit.MILLISECONDS);
        }
        if (overridenOptions.getLeaseTimeout() != 0) {
            JRJCOptions.setLeaseTimeout(overridenOptions.getLeaseTimeout());
        }
        if (overridenOptions.getMaxCacheEntries() != 0) {
            JRJCOptions.setMaxCacheEntries(overridenOptions.getMaxCacheEntries());
        }
        if (overridenOptions.getMaxTotalConnections() != 0) {
            JRJCOptions.setMaxTotalConnections(overridenOptions.getMaxTotalConnections());
        }
        if (overridenOptions.getMaxConnectionsPerHost() != 0) {
            JRJCOptions.setMaxConnectionsPerHost(overridenOptions.getMaxConnectionsPerHost());
        }
        if (overridenOptions.getMaxCacheObjectSize() != 0) {
            JRJCOptions.setMaxCacheObjectSize(overridenOptions.getMaxCacheObjectSize());
        }
        if (overridenOptions.getMaxEntitySize() != 0) {
            JRJCOptions.setMaxEntitySize(overridenOptions.getMaxEntitySize());
        }
        if (overridenOptions.getMaxCallbackThreadPoolSize() != 0) {
            JRJCOptions.setMaxCallbackThreadPoolSize(overridenOptions.getMaxCallbackThreadPoolSize());
        }
    }

    private static class NoOpEventPublisher implements EventPublisher {
        @Override
        public void publish(Object o) {
        }

        @Override
        public void register(Object o) {
        }

        @Override
        public void unregister(Object o) {
        }

        @Override
        public void unregisterAll() {
        }
    }

    /**
     * These properties are used to present JRJC as a User-Agent during http requests.
     */
    @SuppressWarnings("deprecation")
    private static class RestClientApplicationProperties implements ApplicationProperties {

        private final String baseUrl;

        private RestClientApplicationProperties(URI jiraURI) {
            this.baseUrl = jiraURI.getPath();
        }

        @Override
        public String getBaseUrl() {
            return baseUrl;
        }

        /**
         * We'll always have an absolute URL as a client.
         */
        @Nonnull
        @Override
        public String getBaseUrl(UrlMode urlMode) {
            return baseUrl;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return "Atlassian JIRA Rest Java Client";
        }

        @Nonnull
        @Override
        public String getPlatformId() {
            return ApplicationProperties.PLATFORM_JIRA;
        }

        @Nonnull
        @Override
        public String getVersion() {
            return MavenUtils.getVersion("com.atlassian.jira", "jira-rest-java-client-core");
        }

        @Nonnull
        @Override
        public Date getBuildDate() {
            // TODO implement using MavenUtils, JRJC-123
            throw new UnsupportedOperationException();
        }

        @Nonnull
        @Override
        public String getBuildNumber() {
            // TODO implement using MavenUtils, JRJC-123
            return String.valueOf(0);
        }

        @Override
        public File getHomeDirectory() {
            return new File(".");
        }

        @Override
        public String getPropertyValue(final String s) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
    private static final class MavenUtils {
        private static final Logger logger = LoggerFactory.getLogger(MavenUtils.class);

        private static final String UNKNOWN_VERSION = "unknown";

        static String getVersion(String groupId, String artifactId) {
            final Properties props = new Properties();
            InputStream resourceAsStream = null;
            try {
                resourceAsStream = MavenUtils.class.getResourceAsStream(String
                        .format("/META-INF/maven/%s/%s/pom.properties", groupId, artifactId));
                props.load(resourceAsStream);
                return props.getProperty("version", UNKNOWN_VERSION);
            } catch (Exception e) {
                logger.debug("Could not find version for maven artifact {}:{}", groupId, artifactId);
                logger.debug("Got the following exception", e);
                return UNKNOWN_VERSION;
            } finally {
                if (resourceAsStream != null) {
                    try {
                        resourceAsStream.close();
                    } catch (IOException ioe) {
                        // ignore
                    }
                }
            }
        }
    }
}
