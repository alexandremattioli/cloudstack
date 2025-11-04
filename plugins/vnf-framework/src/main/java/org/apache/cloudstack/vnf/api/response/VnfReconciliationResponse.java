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

/**
 * Response object for VNF reconciliation operations
 */
public class VnfReconciliationResponse extends BaseResponse {
    
    @SerializedName("id")
    @Param(description = "Reconciliation log UUID")
    private String id;
    
    @SerializedName("networkid")
    @Param(description = "Network ID")
    private String networkId;
    
    @SerializedName("status")
    @Param(description = "Reconciliation status (Running, Success, Failed, PartialSuccess)")
    private String status;
    
    @SerializedName("started")
    @Param(description = "Reconciliation start time")
    private Date started;
    
    @SerializedName("completed")
    @Param(description = "Reconciliation completion time")
    private Date completed;
    
    @SerializedName("ruleschecked")
    @Param(description = "Number of rules checked")
    private Integer rulesChecked;
    
    @SerializedName("missingrules")
    @Param(description = "Number of missing rules found")
    private Integer missingRules;
    
    @SerializedName("extrarules")
    @Param(description = "Number of extra rules found")
    private Integer extraRules;
    
    @SerializedName("rulesreapplied")
    @Param(description = "Number of rules re-applied")
    private Integer rulesReapplied;
    
    @SerializedName("rulesremoved")
    @Param(description = "Number of rules removed")
    private Integer rulesRemoved;
    
    @SerializedName("driftdetected")
    @Param(description = "Whether drift was detected")
    private Boolean driftDetected;
    
    @SerializedName("errormessage")
    @Param(description = "Error message if failed")
    private String errorMessage;
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNetworkId() {
        return networkId;
    }
    
    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
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
    
    public Integer getRulesChecked() {
        return rulesChecked;
    }
    
    public void setRulesChecked(Integer rulesChecked) {
        this.rulesChecked = rulesChecked;
    }
    
    public Integer getMissingRules() {
        return missingRules;
    }
    
    public void setMissingRules(Integer missingRules) {
        this.missingRules = missingRules;
    }
    
    public Integer getExtraRules() {
        return extraRules;
    }
    
    public void setExtraRules(Integer extraRules) {
        this.extraRules = extraRules;
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
}
