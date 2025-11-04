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

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.vnf.entity.VnfDictionaryVO;
import org.apache.cloudstack.vnf.service.VnfService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@APICommand(
    name = "listVnfDictionaries",
    description = "Lists VNF dictionaries",
    responseObject = VnfDictionaryResponse.class,
    since = "4.21.0",
    authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin}
)
public class ListVnfDictionariesCmd extends BaseListCmd {

    @Inject
    private VnfService vnfService;

    @Parameter(name = ApiConstants.ID,
               type = CommandType.STRING,
               description = "Dictionary UUID")
    private String id;

    @Parameter(name = "vendor",
               type = CommandType.STRING,
               description = "Filter by VNF vendor")
    private String vendor;

    @Parameter(name = ApiConstants.NETWORK_ID,
               type = CommandType.UUID,
               entityType = NetworkResponse.class,
               description = "Filter by network ID")
    private Long networkId;

    @Parameter(name = "templateid",
               type = CommandType.STRING,
               description = "Filter by template UUID")
    private String templateId;

    @Override
    public void execute() {
        List<VnfDictionaryVO> dictionaries = vnfService.listDictionaries();
        
        // Apply filters
        List<VnfDictionaryResponse> responseList = new ArrayList<>();
        for (VnfDictionaryVO dict : dictionaries) {
            if (id != null && !id.equals(dict.getUuid())) {
                continue;
            }
            if (vendor != null && !vendor.equals(dict.getVendor())) {
                continue;
            }
            if (networkId != null && !networkId.equals(dict.getNetworkId())) {
                continue;
            }
            if (templateId != null && !templateId.equals(dict.getTemplateId())) {
                continue;
            }
            
            VnfDictionaryResponse response = new VnfDictionaryResponse();
            response.setId(dict.getUuid());
            response.setName(dict.getName());
            response.setVendor(dict.getVendor());
            response.setVersion(dict.getVersion());
            response.setNetworkId(dict.getNetworkId());
            response.setTemplateId(dict.getTemplateId());
            response.setDescription(dict.getDescription());
            response.setCreated(dict.getCreated());
            response.setObjectName("vnfdictionary");
            responseList.add(response);
        }
        
        ListResponse<VnfDictionaryResponse> listResponse = new ListResponse<>();
        listResponse.setResponses(responseList);
        listResponse.setResponseName(getCommandName());
        setResponseObject(listResponse);
    }
}
