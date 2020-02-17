package ru.shadewallcorp.jiraTask.jiraManager.jira.issue;

/**
 * Статусы в jira из support wf
 *

 */
public enum IssueStatus {
    TODBytes(10097, 15, true),
    ADD_INFO(10127, 14, false),
    WAITING_EXTERNAL(15293, 13, false),
    READY_FOR_ANALYSES(14494, 12, true),
    ANALYSIS(10071, 11, true),
    RECOVERY(15294, 10, true),
    READY_FOR_DEVELOPMENT(12095, 9, true),
    DEVELOP(10067, 8, true),
    READY_FOR_CODE_REVIEW(14792, 7, true),
    CODE_REVIEW(10080, 6, true),
    READY_FOR_TESTING(10140, 5, true),
    ON_TEST(10016, 4, true),
    READY_TO_RELEASE(10696, 3, false),
    READY_TO_MERGE(12898, 2, false),
    CLOSED(6, 1, false),
    CLOSED_WITHOUT_DEV(0, 17, false),
    INVALID(-1, 16, true);

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
