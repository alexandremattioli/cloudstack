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

package org.apache.cloudstack.vnf.api;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

import java.util.Date;

public class VnfApplianceResponse extends BaseResponse {

    @SerializedName("id")
    @Param(description = "VNF appliance UUID")
    private String id;

    @SerializedName("networkid")
    @Param(description = "Network ID")
    private Long networkId;

    @SerializedName("vminstanceid")
    @Param(description = "VM instance ID")
    private Long vmInstanceId;

    @SerializedName("dictionaryid")
    @Param(description = "Dictionary ID")
    private Long dictionaryId;

    @SerializedName("brokerurl")
    @Param(description = "Broker URL")
    private String brokerUrl;

    @SerializedName("state")
    @Param(description = "Appliance state")
    private String state;

    @SerializedName("healthstatus")
    @Param(description = "Health status")
    private String healthStatus;

    @SerializedName("lasthealthcheck")
    @Param(description = "Last health check timestamp")
    private Date lastHealthCheck;

    @SerializedName("lastcontacttime")
    @Param(description = "Last contact time")
    private Date lastContactTime;

    @SerializedName("created")
    @Param(description = "Creation timestamp")
    private Date created;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Long networkId) {
        this.networkId = networkId;
    }

    public Long getVmInstanceId() {
        return vmInstanceId;
    }

    public void setVmInstanceId(Long vmInstanceId) {
        this.vmInstanceId = vmInstanceId;
    }

    public Long getDictionaryId() {
        return dictionaryId;
    }

    public void setDictionaryId(Long dictionaryId) {
        this.dictionaryId = dictionaryId;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public Date getLastHealthCheck() {
        return lastHealthCheck;
    }

    public void setLastHealthCheck(Date lastHealthCheck) {
        this.lastHealthCheck = lastHealthCheck;
    }

    public Date getLastContactTime() {
        return lastContactTime;
    }

    public void setLastContactTime(Date lastContactTime) {
        this.lastContactTime = lastContactTime;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
