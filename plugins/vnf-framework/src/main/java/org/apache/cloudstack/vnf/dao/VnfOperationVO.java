package org.apache.cloudstack.vnf.dao;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "vnf_operations")
public class VnfOperationVO {
    
    public enum State {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED,
        CANCELLED
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "uuid", length = 40, unique = true, nullable = false)
    private String uuid;
    
    @Column(name = "operation_id", length = 255, unique = true)
    private String operationId;
    
    @Column(name = "rule_id", length = 255)
    private String ruleId;
    
    @Column(name = "vnf_instance_id")
    private Long vnfInstanceId;
    
    @Column(name = "vnf_appliance_id")
    private Long vnfApplianceId;
    
    @Column(name = "operation_type", length = 50)
    private String operationType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private State state;
    
    @Column(name = "error_code", length = 50)
    private String errorCode;
    
    @Column(name = "error_message", length = 1024)
    private String errorMessage;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "started_at")
    private Date startedAt;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "completed_at")
    private Date completedAt;
    
    // Getters
    public Long getId() { return id; }
    public String getUuid() { return uuid; }
    public String getOperationId() { return operationId; }
    public String getRuleId() { return ruleId; }
    public Long getVnfInstanceId() { return vnfInstanceId; }
    public Long getVnfApplianceId() { return vnfApplianceId; }
    public String getOperationType() { return operationType; }
    public State getState() { return state; }
    public String getErrorCode() { return errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public Date getCreatedAt() { return createdAt; }
    public Date getStartedAt() { return startedAt; }
    public Date getCompletedAt() { return completedAt; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public void setOperationId(String operationId) { this.operationId = operationId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public void setVnfInstanceId(Long vnfInstanceId) { this.vnfInstanceId = vnfInstanceId; }
    public void setVnfApplianceId(Long vnfApplianceId) { this.vnfApplianceId = vnfApplianceId; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public void setState(State state) { this.state = state; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setStartedAt(Date startedAt) { this.startedAt = startedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }
}
