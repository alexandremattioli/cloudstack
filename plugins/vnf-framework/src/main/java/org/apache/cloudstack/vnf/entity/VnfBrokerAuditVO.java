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

/**
 * VnfBrokerAuditVO - Audit log for VNF broker communication
 * Maps to vnf_broker_audit table
 */
@Entity
@Table(name = "vnf_broker_audit")
public class VnfBrokerAuditVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "vnf_appliance_id", nullable = false)
    private Long vnfApplianceId;

    @Column(name = "operation", nullable = false, length = 100)
    private String operation;

    @Column(name = "method", length = 10)
    private String method;

    @Column(name = "endpoint", length = 500)
    private String endpoint;

    @Column(name = "request_timestamp", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date requestTimestamp;

    @Column(name = "response_timestamp")
    @Temporal(TemporalType.TIMESTAMP)
    private Date responseTimestamp;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "success")
    private Boolean success = false;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "duration_ms")
    private Integer durationMs;

    public VnfBrokerAuditVO() {
        this.requestTimestamp = new Date();
    }

    public VnfBrokerAuditVO(Long vnfApplianceId, String operation, String method) {
        this();
        this.vnfApplianceId = vnfApplianceId;
        this.operation = operation;
        this.method = method;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVnfApplianceId() {
        return vnfApplianceId;
    }

    public void setVnfApplianceId(Long vnfApplianceId) {
        this.vnfApplianceId = vnfApplianceId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public Date getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(Date requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public Date getResponseTimestamp() {
        return responseTimestamp;
    }

    public void setResponseTimestamp(Date responseTimestamp) {
        this.responseTimestamp = responseTimestamp;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }
}
