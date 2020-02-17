package ru.shadewallcorp.jiraTask.jiraManager.jira.issue;

import java.util.Date;

/**
 * Объект статуса issue в jira
 *

 */
public class IssueStatusDo {
    private IssueStatus statusTo;
    private IssueStatus statusFrom;
    private Date creationDate;
    private String author;
    private boolean isCorrect;

    public IssueStatusDo (int statusTo, int statusFrom, Date creationDate, String author) {
        setStatusTo(statusTo);
        setStatusFrom(statusFrom);
        this.creationDate = creationDate;
        this.author = author;
    }

    public IssueStatus getStatusTo() {
        return statusTo;
    }

    public void setStatusTo(int statusTo) {
        this.statusTo = IssueStatus.getIssueStatus(statusTo);
        this.isCorrect = (this.statusTo == IssueStatus.INVALID);
    }

    public IssueStatus getStatusFrom() {
        return statusFrom;
    }

    public void setStatusFrom(int statusFrom) {
        this.statusFrom = IssueStatus.getIssueStatus(statusFrom);
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isCorrect() {
        return isCorrect;
    }
}
