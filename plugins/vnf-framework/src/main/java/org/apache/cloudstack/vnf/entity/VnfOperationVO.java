package org.apache.cloudstack.vnf.entity;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "vnf_operations")
public class VnfOperationVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true)
    private String uuid;

    @Column(name = "vnf_instance_id", nullable = false)
    private Long vnfInstanceId;

    @Column(name = "operation_type", nullable = false, length = 64)
    private String operationType;

    @Column(name = "rule_id", length = 128)
    private String ruleId;

    @Column(name = "op_hash", nullable = false, length = 64)
    private String opHash;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "vendor_ref", length = 256)
    private String vendorRef;

    @Column(name = "state", nullable = false, length = 32)
    private String state = "Pending";

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "completed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completedAt;

    @Column(name = "removed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date removed;

    public VnfOperationVO() {
        this.uuid = UUID.randomUUID().toString();
        this.createdAt = new Date();
    }

    public VnfOperationVO(Long vnfInstanceId, String operationType, String ruleId, String opHash) {
        this();
        this.vnfInstanceId = vnfInstanceId;
        this.operationType = operationType;
        this.ruleId = ruleId;
        this.opHash = opHash;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getVnfInstanceId() {
        return vnfInstanceId;
    }

    public void setVnfInstanceId(Long vnfInstanceId) {
        this.vnfInstanceId = vnfInstanceId;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getOpHash() {
        return opHash;
    }

    public void setOpHash(String opHash) {
        this.opHash = opHash;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public String getVendorRef() {
        return vendorRef;
    }

    public void setVendorRef(String vendorRef) {
        this.vendorRef = vendorRef;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(Date removed) {
        this.removed = removed;
    }

    public enum State {
        Pending,
        InProgress,
        Completed,
        Failed,
        TimedOut
    }

    public enum OperationType {
        CREATE_FIREWALL_RULE,
        DELETE_FIREWALL_RULE,
        UPDATE_FIREWALL_RULE,
        CREATE_NAT_RULE,
        DELETE_NAT_RULE,
        CREATE_VPN_CONNECTION,
        DELETE_VPN_CONNECTION
    }
}
