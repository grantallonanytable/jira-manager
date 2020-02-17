package ru.shadewallcorp.jiraTasks.jiraManager.sla;

/**
 * SLA по приоритетам инцидентов
 *
 */
public class SLA {
    private SLATimeLapse maxReactionTimeLapse;
    private SLATimeLapse maxRecoveryTimeLapse;
    private SLATimeLapse maxSolutionTimeLapse;
    private SLATimeLapse actualReactionTimeLapse;
    private SLATimeLapse actualRecoveryTimeLapse;
    private SLATimeLapse actualSolutionTimeLapse;
    private SLADeadline reactionDeadline;
    private SLADeadline recoveryDeadline;
    private SLADeadline solutionDeadline;

    public SLA() {
    }

    public SLA(SLATimeLapse maxReactionTimeLapse, SLATimeLapse maxRecoveryTimeLapse, SLATimeLapse maxSolutionTimeLapse) {
        this.maxReactionTimeLapse = maxReactionTimeLapse;
        this.maxRecoveryTimeLapse = maxRecoveryTimeLapse;
        this.maxSolutionTimeLapse = maxSolutionTimeLapse;
    }

    public SLA(SLATimeLapse maxReactionTimeLapse, SLATimeLapse maxRecoveryTimeLapse, SLATimeLapse maxSolutionTimeLapse,
               SLATimeLapse actualReactionTimeLapse, SLATimeLapse actualRecoveryTimeLapse, SLATimeLapse actualSolutionTimeLapse,
               SLADeadline reactionDeadline, SLADeadline recoveryDeadline, SLADeadline solutionDeadline) {
        this.maxReactionTimeLapse = maxReactionTimeLapse;
        this.maxRecoveryTimeLapse = maxRecoveryTimeLapse;
        this.maxSolutionTimeLapse = maxSolutionTimeLapse;
        this.actualReactionTimeLapse = actualReactionTimeLapse;
        this.actualRecoveryTimeLapse = actualRecoveryTimeLapse;
        this.actualSolutionTimeLapse = actualSolutionTimeLapse;
        this.reactionDeadline = reactionDeadline;
        this.recoveryDeadline = recoveryDeadline;
        this.solutionDeadline = solutionDeadline;
    }

    public SLA (SLA timeline, SLA deadline) {
        this.maxReactionTimeLapse = timeline.getMaxReactionTimeLapse();
        this.maxRecoveryTimeLapse = timeline.getMaxRecoveryTimeLapse();
        this.maxSolutionTimeLapse = timeline.getMaxSolutionTimeLapse();
        this.actualReactionTimeLapse = timeline.getActualReactionTimeLapse();
        this.actualRecoveryTimeLapse = timeline.getActualRecoveryTimeLapse();
        this.actualSolutionTimeLapse = timeline.getActualSolutionTimeLapse();
        this.reactionDeadline = deadline.getReactionDeadline();
        this.recoveryDeadline = deadline.getRecoveryDeadline();
        this.solutionDeadline = deadline.getSolutionDeadline();
    }

    public SLATimeLapse getMaxReactionTimeLapse() {
        return maxReactionTimeLapse;
    }

    public void setMaxReactionTimeLapse(SLATimeLapse maxReactionTimeLapse) {
        this.maxReactionTimeLapse = maxReactionTimeLapse;
    }

    public SLATimeLapse getMaxRecoveryTimeLapse() {
        return maxRecoveryTimeLapse;
    }

    public void setMaxRecoveryTimeLapse(SLATimeLapse maxRecoveryTimeLapse) {
        this.maxRecoveryTimeLapse = maxRecoveryTimeLapse;
    }

    public SLATimeLapse getMaxSolutionTimeLapse() {
        return maxSolutionTimeLapse;
    }

    public void setMaxSolutionTimeLapse(SLATimeLapse maxSolutionTimeLapse) {
        this.maxSolutionTimeLapse = maxSolutionTimeLapse;
    }

    public SLATimeLapse getActualReactionTimeLapse() {
        return actualReactionTimeLapse;
    }

    public void setActualReactionTimeLapse(SLATimeLapse actualReactionTimeLapse) {
        this.actualReactionTimeLapse = actualReactionTimeLapse;
    }

    public SLATimeLapse getActualRecoveryTimeLapse() {
        return actualRecoveryTimeLapse;
    }

    public void setActualRecoveryTimeLapse(SLATimeLapse actualRecoveryTimeLapse) {
        this.actualRecoveryTimeLapse = actualRecoveryTimeLapse;
    }

    public SLATimeLapse getActualSolutionTimeLapse() {
        return actualSolutionTimeLapse;
    }

    public void setActualSolutionTimeLapse(SLATimeLapse actualSolutionTimeLapse) {
        this.actualSolutionTimeLapse = actualSolutionTimeLapse;
    }

    public SLADeadline getReactionDeadline() {
        return reactionDeadline;
    }

    public void setReactionDeadline(SLADeadline reactionDeadline) {
        this.reactionDeadline = reactionDeadline;
    }

    public SLADeadline getRecoveryDeadline() {
        return recoveryDeadline;
    }

    public void setRecoveryDeadline(SLADeadline recoveryDeadline) {
        this.recoveryDeadline = recoveryDeadline;
    }

    public SLADeadline getSolutionDeadline() {
        return solutionDeadline;
    }

    public void setSolutionDeadline(SLADeadline solutionDeadline) {
        this.solutionDeadline = solutionDeadline;
    }
}
