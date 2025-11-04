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

public class VnfDictionaryResponse extends BaseResponse {

    @SerializedName("id")
    @Param(description = "Dictionary UUID")
    private String id;

    @SerializedName("name")
    @Param(description = "Dictionary name")
    private String name;

    @SerializedName("vendor")
    @Param(description = "VNF vendor")
    private String vendor;

    @SerializedName("version")
    @Param(description = "VNF version")
    private String version;

    @SerializedName("networkid")
    @Param(description = "Associated network ID")
    private Long networkId;

    @SerializedName("templateid")
    @Param(description = "Associated template ID")
    private String templateId;

    @SerializedName("description")
    @Param(description = "Dictionary description")
    private String description;

    @SerializedName("created")
    @Param(description = "Creation timestamp")
    private Date created;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Long networkId) {
        this.networkId = networkId;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
