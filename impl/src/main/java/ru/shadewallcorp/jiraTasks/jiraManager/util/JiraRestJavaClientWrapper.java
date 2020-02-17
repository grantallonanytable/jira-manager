package ru.shadewallcorp.jiraTasks.jiraManager.util;

import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.typesafe.config.ConfigBeanFactory;
import com.typesafe.config.ConfigFactory;
import io.atlassian.util.concurrent.Promise;
import lombok.Getter;
import org.codehaus.jackson.annotate.JsonIgnore;
import ru.shadewallcorp.jiraTasks.jiraManager.config.JiraConfig;
import ru.shadewallcorp.jiraTasks.jiraManager.jira.JiraVar;
import ru.shadewallcorp.jiraTasks.jiraManager.jira.client.JiraRestJavaClientFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/*
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
public class JiraRestJavaClientWrapper {

    @JsonIgnore
    @Getter
    private JiraConfig config;
    @JsonIgnore
    JiraRestClient jiraRestClient;
    @JsonIgnore
    @Getter
    private SearchRestClient searchClient;
    @JsonIgnore
    @Getter
    private IssueRestClient issueClient;

    @Inject
    public JiraRestJavaClientWrapper() throws URISyntaxException {
        config = ConfigBeanFactory.create(ConfigFactory.load().getConfig("jira"), JiraConfig.class);
        jiraRestClient = new JiraRestJavaClientFactory().createWithBasicHttpAuthentication(
                new URI(JiraVar.JIRA_URL.getJiraVar()),
                config.getUserName(), config.getPassword(),
                getJiraRestJavaClientOptions());
        searchClient = jiraRestClient.getSearchClient();
        issueClient = jiraRestClient.getIssueClient();
    }

    private HttpClientOptions getJiraRestJavaClientOptions() {
        final HttpClientOptions httpClientOptions = new HttpClientOptions();
        if (config.getLeaseTimeout() != null) {
            httpClientOptions.setLeaseTimeout(config.getLeaseTimeout());
        }
        //TODO: возможно переполнение
        if (config.getSocketTimeout() != null) {
            httpClientOptions.setSocketTimeout((int) config.getSocketTimeout().toMillis(), TimeUnit.MILLISECONDS);
        }
        if (config.getConnectionTimeout() != null) {
            httpClientOptions.setConnectionTimeout((int) config.getConnectionTimeout().toMillis(), TimeUnit.MILLISECONDS);
        }
        if (config.getRequestTimeout() != null) {
            httpClientOptions.setRequestTimeout((int) config.getRequestTimeout().toMillis(), TimeUnit.MILLISECONDS);
        }
        return httpClientOptions;
    }

    public Promise<SearchResult> searchJql(@Nullable String jql, @Nullable Integer maxResults, @Nullable Integer startAt, @Nullable Set<String> fields) {
        return searchClient.searchJql(jql, maxResults, startAt, fields);
    }

    public Promise<Issue> getIssue(String issueKey, Iterable<IssueRestClient.Expandos> expand) {
        return issueClient.getIssue(issueKey, expand);
    }

    public Promise<Void> updateIssue(String issueKey, IssueInput issue) {
        return issueClient.updateIssue(issueKey, issue);
    }

}
