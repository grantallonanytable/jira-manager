package ru.shadewallcorp.jiraTask.jiraManager.jira.issue;

import ru.shadewallcorp.jiraTask.jiraManager.sla.SLA;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Объект данных jira issue
 *

 */
public class IssueDo {
    private String issueId;
    private Severity severity;
    private IssueStatus currentStatus;
    private String shortDescription;
    private String description;
    private Date creationDate;
    private List<String> labels;
    private List<IssueStatusDo> issueStatusDoList;
    private boolean incorrectStatus;
    private boolean codeBug;
    private String srNumber;
    private SLA sla;
    private Date solutionDate;
    private String releaseVersion;

    private boolean isRejected;

    public IssueDo(String issueId, Severity severity, int statusCode, String shortDescription, String description,
                   Date creationDate, List<String> labels, List<IssueStatusDo> issueStatusDoList, boolean incorrectStatus,
                   String releaseVersion) {
        this.issueId = issueId;
        this.severity = severity;
        this.currentStatus = IssueStatus.getIssueStatus(statusCode);
        this.shortDescription = shortDescription;
        this.description = description;
        this.creationDate = creationDate;
        this.labels = labels;
        this.issueStatusDoList = issueStatusDoList;
        this.incorrectStatus = incorrectStatus;
        this.releaseVersion = releaseVersion;
        this.isRejected = false;
    }

    public IssueDo(String issueId, Severity severity, int statusCode, String shortDescription, String description,
                   Date creationDate, List<String> labels, String srNumber) {
        this.issueId = issueId;
        this.severity = severity;
        this.currentStatus = IssueStatus.getIssueStatus(statusCode);
        this.shortDescription = shortDescription;
        this.description = description;
        this.creationDate = creationDate;
        this.labels = labels;
        this.incorrectStatus = false;
        this.codeBug = false;
        this.issueStatusDoList = new ArrayList<>();
        this.srNumber = srNumber;
        this.isRejected = false;
    }

    public String getIssueId() {
        return issueId;
    }

    public void setIssueId(String issueId) {
        this.issueId = issueId;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Long priority) {
        this.severity = severity;
    }

    public IssueStatus getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(int currentStatusCode) {
        this.currentStatus = IssueStatus.getIssueStatus(currentStatusCode);
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<IssueStatusDo> getIssueStatusList() {
        return issueStatusDoList;
    }

    public void setIssueStatusList(List<IssueStatusDo> issueStatusList) {
        this.issueStatusDoList = issueStatusList;
    }

    public void addIssueStatusList(IssueStatusDo issueStatusList) {
        this.codeBug = !this.codeBug && issueStatusList.getStatusTo() == IssueStatus.DEVELOP;
        this.issueStatusDoList.add(issueStatusList);
    }

    public boolean isIncorrectStatus() {
        return incorrectStatus;
    }

    public void setIncorrectStatus(boolean incorrectStatus) {
        this.incorrectStatus = incorrectStatus;
    }

    public boolean isCodeBug() {
        return codeBug;
    }

    public void setCodeBug(boolean codeBug) {
        this.codeBug = codeBug;
    }

    public String getSrNumber() {
        return srNumber;
    }

    public void setSrNumber(String srNumber) {
        this.srNumber = srNumber;
    }

    public SLA getSla() {
        return sla;
    }

    public void setSla(SLA sla) {
        this.sla = sla;
        if(sla.getActualSolutionTimeLapse() != null)
            setSolutionDate(sla.getActualSolutionTimeLapse().getDate());
    }

    public Date getSolutionDate() {
        return solutionDate;
    }

    public void setSolutionDate(Date solutionDate) {
        this.solutionDate = solutionDate;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public void setReleaseVersion(String releaseVersion) {
        this.releaseVersion = releaseVersion;
    }

    public boolean isRejected() {
        return isRejected;
    }

    public void setRejected(boolean rejected) {
        isRejected = rejected;
    }
}
