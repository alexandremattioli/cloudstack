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
import org.apache.cloudstack.vnf.entity.VnfApplianceVO;
import org.apache.cloudstack.vnf.service.VnfService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@APICommand(
    name = "listVnfFrameworkAppliances",
    description = "Lists VNF appliances tracked by the VNF Framework",
    responseObject = VnfApplianceResponse.class,
    since = "4.21.0",
    authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin}
)
public class ListVnfAppliancesCmd extends BaseListCmd {

    @Inject
    private VnfService vnfService;

    @Parameter(name = ApiConstants.ID,
               type = CommandType.STRING,
               description = "VNF appliance UUID")
    private String id;

    @Parameter(name = ApiConstants.NETWORK_ID,
               type = CommandType.UUID,
               entityType = NetworkResponse.class,
               description = "Filter by network ID")
    private Long networkId;

    @Parameter(name = "state",
               type = CommandType.STRING,
               description = "Filter by state (Deployed, Active, Inactive, Error, Removed)")
    private String state;

    @Parameter(name = "healthstatus",
               type = CommandType.STRING,
               description = "Filter by health status (Healthy, Degraded, Unhealthy, Unknown)")
    private String healthStatus;

    @Override
    public void execute() {
        List<VnfApplianceVO> appliances;
        
        if (networkId != null) {
            appliances = vnfService.listAppliancesByNetwork(networkId);
        } else {
            appliances = vnfService.listAllAppliances();
        }
        
        // Apply filters
        List<VnfApplianceResponse> responseList = new ArrayList<>();
        for (VnfApplianceVO appliance : appliances) {
            if (appliance.getRemoved() != null) {
                continue; // Skip removed appliances
            }
            
            if (id != null && !id.equals(appliance.getUuid())) {
                continue;
            }
            if (state != null && !state.equalsIgnoreCase(appliance.getState().toString())) {
                continue;
            }
            if (healthStatus != null && !healthStatus.equalsIgnoreCase(appliance.getHealthStatus().toString())) {
                continue;
            }
            
            VnfApplianceResponse response = new VnfApplianceResponse();
            response.setId(appliance.getUuid());
            response.setNetworkId(appliance.getNetworkId());
            response.setVmInstanceId(appliance.getVmInstanceId());
            response.setDictionaryId(appliance.getDictionaryId());
            response.setBrokerUrl(appliance.getBrokerUrl());
            response.setState(appliance.getState().toString());
            response.setHealthStatus(appliance.getHealthStatus().toString());
            response.setLastHealthCheck(appliance.getLastHealthCheck());
            response.setLastContactTime(appliance.getLastContactTime());
            response.setCreated(appliance.getCreated());
            response.setObjectName("vnfappliance");
            responseList.add(response);
        }
        
        ListResponse<VnfApplianceResponse> listResponse = new ListResponse<>();
        listResponse.setResponses(responseList);
        listResponse.setResponseName(getCommandName());
        setResponseObject(listResponse);
    }
}
