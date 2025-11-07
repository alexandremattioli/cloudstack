// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.cloudstack.vnf.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * VnfReconciliationLogVO - Tracks reconciliation runs and drift detection
 * Maps to vnf_reconciliation_log table
 */
@Entity
@Table(name = "vnf_reconciliation_log")
public class VnfReconciliationLogVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", length = 40)
    private String uuid;

    @Column(name = "network_id", nullable = false)
    private Long networkId;

    @Column(name = "vnf_appliance_id")
    private Long vnfApplianceId;

    @Column(name = "started", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date started;

    @Column(name = "completed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date completed;

    @Column(name = "status", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private ReconciliationStatus status = ReconciliationStatus.Running;

    @Column(name = "rules_checked")
    private Integer rulesChecked = 0;

    @Column(name = "missing_rules_found")
    private Integer missingRulesFound = 0;

    @Column(name = "extra_rules_found")
    private Integer extraRulesFound = 0;

    @Column(name = "rules_reapplied")
    private Integer rulesReapplied = 0;

    @Column(name = "rules_removed")
    private Integer rulesRemoved = 0;

    @Column(name = "drift_detected")
    private Boolean driftDetected = false;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "details", columnDefinition = "MEDIUMTEXT")
    private String details;

    public enum ReconciliationStatus {
        Running,
        Success,
        Failed,
        PartialSuccess
    }

    public VnfReconciliationLogVO() {
        this.uuid = UUID.randomUUID().toString();
        this.started = new Date();
    }

    public VnfReconciliationLogVO(Long networkId, Long vnfApplianceId) {
        this();
        this.networkId = networkId;
        this.vnfApplianceId = vnfApplianceId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Long networkId) {
        this.networkId = networkId;
    }

    public Long getVnfApplianceId() {
        return vnfApplianceId;
    }

    public void setVnfApplianceId(Long vnfApplianceId) {
        this.vnfApplianceId = vnfApplianceId;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getCompleted() {
        return completed;
    }

    public void setCompleted(Date completed) {
        this.completed = completed;
    }

    public ReconciliationStatus getStatus() {
        return status;
    }

    public void setStatus(ReconciliationStatus status) {
        this.status = status;
    }

    public Integer getRulesChecked() {
        return rulesChecked;
    }

    public void setRulesChecked(Integer rulesChecked) {
        this.rulesChecked = rulesChecked;
    }

    public Integer getMissingRulesFound() {
        return missingRulesFound;
    }

    public void setMissingRulesFound(Integer missingRulesFound) {
        this.missingRulesFound = missingRulesFound;
    }

    public Integer getExtraRulesFound() {
        return extraRulesFound;
    }

    public void setExtraRulesFound(Integer extraRulesFound) {
        this.extraRulesFound = extraRulesFound;
    }

    public Integer getRulesReapplied() {
        return rulesReapplied;
    }

    public void setRulesReapplied(Integer rulesReapplied) {
        this.rulesReapplied = rulesReapplied;
    }

    public Integer getRulesRemoved() {
        return rulesRemoved;
    }

    public void setRulesRemoved(Integer rulesRemoved) {
        this.rulesRemoved = rulesRemoved;
    }

    public Boolean getDriftDetected() {
        return driftDetected;
    }

    public void setDriftDetected(Boolean driftDetected) {
        this.driftDetected = driftDetected;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
