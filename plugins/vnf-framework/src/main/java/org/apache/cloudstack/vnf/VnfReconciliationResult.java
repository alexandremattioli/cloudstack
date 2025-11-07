package org.apache.cloudstack.vnf;

public class VnfReconciliationResult {
    private boolean success;
    private boolean driftDetected;
    private int rulesChecked;
    private int missingRules;
    private int extraRules;
    private int rulesReapplied;
    private int rulesRemoved;
    private List<ReconciliationAction> actions;
    private String errorMessage;
    // Getters and setters
    public boolean isDriftDetected() { return driftDetected; }
    public void setDriftDetected(boolean detected) { this.driftDetected = detected; }
    public int getMissingRules() { return missingRules; }
    public void setMissingRules(int count) { this.missingRules = count; }
    public List<ReconciliationAction> getActions() { return actions; }
    public void addAction(ReconciliationAction action) { actions.add(action); }
}
/**
 * Individual reconciliation action
 */