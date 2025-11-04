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

import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.vnf.entity.VnfDictionaryVO;
import org.apache.cloudstack.vnf.service.VnfService;

import javax.inject.Inject;

@APICommand(
    name = "uploadVnfDictionary",
    description = "Upload or update a VNF dictionary for a specific vendor and template",
    responseObject = VnfDictionaryResponse.class,
    since = "4.21.0",
    authorized = {RoleType.Admin}
)
public class UploadVnfDictionaryCmd extends BaseCmd {

    @Inject
    private VnfService vnfService;

    @Parameter(name = ApiConstants.NAME,
               type = CommandType.STRING,
               required = true,
               description = "Name of the dictionary")
    private String name;

    @Parameter(name = "vendor",
               type = CommandType.STRING,
               required = true,
               description = "VNF vendor (e.g., pfsense, opnsense, fortinet)")
    private String vendor;

    @Parameter(name = "version",
               type = CommandType.STRING,
               required = true,
               description = "VNF appliance version (e.g., 2.7.0)")
    private String version;

    @Parameter(name = "content",
               type = CommandType.STRING,
               required = true,
               length = 65535,
               description = "YAML dictionary content")
    private String content;

    @Parameter(name = ApiConstants.NETWORK_ID,
               type = CommandType.UUID,
               entityType = NetworkResponse.class,
               description = "Network ID to associate this dictionary with (optional)")
    private Long networkId;

    @Parameter(name = "templateid",
               type = CommandType.STRING,
               description = "Template UUID to associate with this dictionary (optional)")
    private String templateId;

    @Parameter(name = ApiConstants.DESCRIPTION,
               type = CommandType.STRING,
               description = "Description of the dictionary")
    private String description;

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException,
            ServerApiException, ConcurrentOperationException, ResourceAllocationException,
            NetworkRuleConflictException {
        
        VnfDictionaryVO dictionary = vnfService.uploadDictionary(
            name, vendor, version, content, networkId, templateId, description);
        
        if (dictionary == null) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, 
                "Failed to upload VNF dictionary");
        }

        VnfDictionaryResponse response = new VnfDictionaryResponse();
        response.setId(dictionary.getUuid());
        response.setName(dictionary.getName());
        response.setVendor(dictionary.getVendor());
        response.setVersion(dictionary.getVersion());
        response.setNetworkId(dictionary.getNetworkId());
        response.setTemplateId(dictionary.getTemplateId());
        response.setDescription(dictionary.getDescription());
        response.setCreated(dictionary.getCreated());
        response.setObjectName("vnfdictionary");
        
        setResponseObject(response);
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_PORTABLE_IP_ASSIGN;
    }

    @Override
    public String getEventDescription() {
        return "Uploading VNF dictionary: " + name;
    }

    @Override
    public long getEntityOwnerId() {
        return getAccountId();
    }
}
