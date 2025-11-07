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

package org.apache.cloudstack.vnf.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "vnf_operations")
public class VnfOperationVO {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "operation_id", nullable = false, unique = true)
    private String operationId;
    
    @Column(name = "rule_id")
    private String ruleId;
    
    @Column(name = "vnf_appliance_id")
    private Long vnfApplianceId;
    
    @Column(name = "operation_type")
    private String operationType;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "created")
    private Date created;
    
    @Column(name = "completed")
    private Date completed;
    
    @Column(name = "error_message", length = 1024)
    private String errorMessage;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }
    
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    
    public Long getVnfApplianceId() { return vnfApplianceId; }
    public void setVnfApplianceId(Long vnfApplianceId) { this.vnfApplianceId = vnfApplianceId; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getCreated() { return created; }
    public void setCreated(Date created) { this.created = created; }
    
    public Date getCompleted() { return completed; }
    public void setCompleted(Date completed) { this.completed = completed; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
