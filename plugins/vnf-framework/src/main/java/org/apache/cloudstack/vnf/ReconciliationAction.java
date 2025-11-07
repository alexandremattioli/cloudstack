package org.apache.cloudstack.vnf;

public class ReconciliationAction {
    private String service;
    private ActionType actionType;
    private String ruleId;
    private String description;
    public enum ActionType {
        REAPPLIED, REMOVED, FLAGGED, NO_ACTION
    }
    public ReconciliationAction(String service, ActionType type, String ruleId, String desc) {
        this.service = service;
        this.actionType = type;
        this.ruleId = ruleId;
        this.description = desc;
    }
}
/**
 * Connectivity test result
 */