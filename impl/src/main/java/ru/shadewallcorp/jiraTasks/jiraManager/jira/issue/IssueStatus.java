package ru.shadewallcorp.jiraTasks.jiraManager.jira.issue;

/**
 * Статусы в jira из support wf
 *
 * @author Dilvish {@literal <dilvish@newmail.ru>}
 */
public enum IssueStatus {
    TODO(IssueStatusCodes.TODO, 15, true),
    ADD_INFO(IssueStatusCodes.ADD_INFO, 14, false),
    WAITING_EXTERNAL(IssueStatusCodes.WAITING_EXTERNAL, 13, false),
    READY_FOR_ANALYSES(IssueStatusCodes.READY_FOR_ANALYSES, 12, true),
    ANALYSIS(IssueStatusCodes.ANALYSIS, 11, true),
    RECOVERY(IssueStatusCodes.RECOVERY, 10, true),
    READY_FOR_DEVELOPMENT(IssueStatusCodes.READY_FOR_DEVELOPMENT, 9, true),
    DEVELOP(IssueStatusCodes.DEVELOP, 8, true),
    READY_FOR_CODE_REVIEW(IssueStatusCodes.READY_FOR_CODE_REVIEW, 7, true),
    CODE_REVIEW(IssueStatusCodes.CODE_REVIEW, 6, true),
    READY_FOR_TESTING(IssueStatusCodes.READY_FOR_TESTING, 5, true),
    ON_TEST(IssueStatusCodes.ON_TEST, 4, true),
    READY_TO_RELEASE(IssueStatusCodes.READY_TO_RELEASE, 3, false),
    READY_TO_MERGE(IssueStatusCodes.READY_TO_MERGE, 2, false),
    CLOSED(IssueStatusCodes.CLOSED, 1, false),
    CLOSED_WITHOUT_DEV(IssueStatusCodes.CLOSED_WITHOUT_DEV, 17, false),
    INVALID(IssueStatusCodes.INVALID, 16, true);

    private final int statusCode;
    private final int priority;
    private final boolean vendorStatus;

    public int getStatusCode() {
        return statusCode;
    }
    public int getPriority() {
        return priority;
    }
    public boolean isVendorStatus() {
        return vendorStatus;
    }

    IssueStatus(int statusCode, int priority, boolean vendorStatus) {
        this.statusCode = statusCode;
        this.priority = priority;
        this.vendorStatus = vendorStatus;
    }

    public static IssueStatus getIssueStatus(int statusCode) {
        for (IssueStatus issueStatus : IssueStatus.values()) {
            if (issueStatus.getStatusCode() == statusCode)
                return issueStatus;
        }
        return INVALID;
    }

}
