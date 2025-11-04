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

package org.apache.cloudstack.vnf.api.response;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

import java.util.Date;

public class VnfOperationResponse extends BaseResponse {

    @SerializedName("id")
    @Param(description = "the ID of the VNF operation")
    private String id;

    @SerializedName("vnfinstanceid")
    @Param(description = "the VNF instance ID")
    private Long vnfInstanceId;

    @SerializedName("operationtype")
    @Param(description = "the operation type")
    private String operationType;

    @SerializedName("ruleid")
    @Param(description = "the rule ID (if applicable)")
    private String ruleId;

    @SerializedName("state")
    @Param(description = "the operation state (Pending, InProgress, Completed, Failed)")
    private String state;

    @SerializedName("errorcode")
    @Param(description = "error code if operation failed")
    private String errorCode;

    @SerializedName("errormessage")
    @Param(description = "error message if operation failed")
    private String errorMessage;

    @SerializedName("createdat")
    @Param(description = "the date when the operation was created")
    private Date createdAt;

    @SerializedName("startedat")
    @Param(description = "the date when the operation started")
    private Date startedAt;

    @SerializedName("completedat")
    @Param(description = "the date when the operation completed")
    private Date completedAt;

    @SerializedName("vendorref")
    @Param(description = "vendor-specific reference (from response payload)")
    private String vendorRef;

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Date completedAt) {
        this.completedAt = completedAt;
    }

    public String getVendorRef() {
        return vendorRef;
    }

    public void setVendorRef(String vendorRef) {
        this.vendorRef = vendorRef;
    }
}
