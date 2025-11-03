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
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;
import org.apache.cloudstack.vnf.entity.VnfInstanceVO;

import java.util.Date;

@EntityReference(value = VnfInstanceVO.class)
public class VnfInstanceResponse extends BaseResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the VNF instance")
    private String id;

    @SerializedName(ApiConstants.NAME)
    @Param(description = "the name of the VNF instance")
    private String name;

    @SerializedName("vnftemplateid")
    @Param(description = "the VNF template ID")
    private String vnfTemplateId;

    @SerializedName("vminstanceid")
    @Param(description = "the VM instance ID")
    private String vmInstanceId;

    @SerializedName(ApiConstants.ACCOUNT)
    @Param(description = "the account associated with the VNF instance")
    private String accountName;

    @SerializedName(ApiConstants.DOMAIN_ID)
    @Param(description = "the domain ID of the VNF instance")
    private String domainId;

    @SerializedName(ApiConstants.DOMAIN)
    @Param(description = "the domain name of the VNF instance")
    private String domainName;

    @SerializedName(ApiConstants.ZONE_ID)
    @Param(description = "the zone ID of the VNF instance")
    private String zoneId;

    @SerializedName(ApiConstants.ZONE_NAME)
    @Param(description = "the zone name of the VNF instance")
    private String zoneName;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the VNF instance")
    private String state;

    @SerializedName("managementip")
    @Param(description = "the management IP of the VNF instance")
    private String managementIp;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date when this VNF instance was created")
    private Date created;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVnfTemplateId() { return vnfTemplateId; }
    public void setVnfTemplateId(String vnfTemplateId) { this.vnfTemplateId = vnfTemplateId; }

    public String getVmInstanceId() { return vmInstanceId; }
    public void setVmInstanceId(String vmInstanceId) { this.vmInstanceId = vmInstanceId; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getDomainId() { return domainId; }
    public void setDomainId(String domainId) { this.domainId = domainId; }

    public String getDomainName() { return domainName; }
    public void setDomainName(String domainName) { this.domainName = domainName; }

    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }

    public String getZoneName() { return zoneName; }
    public void setZoneName(String zoneName) { this.zoneName = zoneName; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getManagementIp() { return managementIp; }
    public void setManagementIp(String managementIp) { this.managementIp = managementIp; }

    public Date getCreated() { return created; }
    public void setCreated(Date created) { this.created = created; }
}
