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

package org.apache.cloudstack.vnf.api.command;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.vnf.api.response.VnfInstanceResponse;

@APICommand(
        name = "listVnfInstances",
        description = "Lists VNF instances",
        responseObject = VnfInstanceResponse.class,
        since = "4.21.7",
        authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User}
)
public class ListVnfInstancesCmd extends BaseListCmd {

    @Parameter(name = ApiConstants.ID, type = CommandType.UUID, entityType = VnfInstanceResponse.class, description = "the ID of the VNF instance")
    private Long id;

    @Parameter(name = ApiConstants.NAME, type = CommandType.STRING, description = "name of the VNF instance")
    private String name;

    @Parameter(name = ApiConstants.STATE, type = CommandType.STRING, description = "state of the VNF instance")
    private String state;

    @Parameter(name = ApiConstants.ZONE_ID, type = CommandType.UUID, description = "the zone ID")
    private Long zoneId;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public Long getZoneId() {
        return zoneId;
    }

    @Override
    public void execute() {
        ListResponse<VnfInstanceResponse> response = new ListResponse<>();
        response.setResponses(null);  // Service will populate this
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }
}
