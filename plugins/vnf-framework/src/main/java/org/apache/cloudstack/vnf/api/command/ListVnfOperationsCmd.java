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

import com.cloud.exception.InvalidParameterValueException;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.BaseCmd.CommandType;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.vnf.api.response.VnfOperationResponse;
import org.apache.cloudstack.vnf.entity.VnfOperationVO;
import org.apache.cloudstack.vnf.service.VnfService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@APICommand(
    name = "listVnfOperations",
    description = "Lists VNF operations with optional filters",
    responseObject = VnfOperationResponse.class,
    requestHasSensitiveInfo = false,
    responseHasSensitiveInfo = false,
    since = "4.21.7",
    authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User}
)
public class ListVnfOperationsCmd extends BaseListCmd {

    @Inject
    private VnfService vnfService;

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(
        name = "vnfinstanceid",
        type = CommandType.UUID,
        entityType = VnfOperationResponse.class,
        description = "filter by VNF instance ID"
    )
    private Long vnfInstanceId;

    @Parameter(
        name = "state",
        type = CommandType.STRING,
        description = "filter by operation state (Pending, InProgress, Completed, Failed)"
    )
    private String state;

    @Parameter(
        name = "operationtype",
        type = CommandType.STRING,
        description = "filter by operation type (CREATE_FIREWALL_RULE, DELETE_FIREWALL_RULE, CREATE_NAT_RULE, etc.)"
    )
    private String operationType;

    @Parameter(
        name = "ruleid",
        type = CommandType.STRING,
        description = "filter by rule ID"
    )
    private String ruleId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getVnfInstanceId() {
        return vnfInstanceId;
    }

    public String getState() {
        return state;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getRuleId() {
        return ruleId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() {
        List<VnfOperationVO> operations;

        // Apply filters
        if (ruleId != null) {
            VnfOperationVO operation = vnfService.findOperationByRuleId(ruleId);
            operations = operation != null ? List.of(operation) : new ArrayList<>();
        } else if (vnfInstanceId != null && state != null) {
            try {
                VnfOperationVO.State stateEnum = VnfOperationVO.State.valueOf(state);
                operations = vnfService.listOperationsByVnfInstanceAndState(vnfInstanceId, stateEnum);
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterValueException("Invalid state: " + state);
            }
        } else if (vnfInstanceId != null) {
            operations = vnfService.listOperationsByVnfInstance(vnfInstanceId);
        } else if (state != null) {
            try {
                VnfOperationVO.State stateEnum = VnfOperationVO.State.valueOf(state);
                operations = vnfService.listOperationsByState(stateEnum);
            } catch (IllegalArgumentException e) {
                throw new InvalidParameterValueException("Invalid state: " + state);
            }
        } else {
            // No filters - return all (with pagination)
            operations = vnfService.listAllOperations(this);
        }

        // Create response
        ListResponse<VnfOperationResponse> response = new ListResponse<>();
        List<VnfOperationResponse> responseList = new ArrayList<>();

        for (VnfOperationVO operation : operations) {
            VnfOperationResponse opResponse = new VnfOperationResponse();
            opResponse.setId(operation.getUuid());
            opResponse.setVnfInstanceId(operation.getVnfInstanceId());
            opResponse.setOperationType(operation.getOperationType());
            opResponse.setRuleId(operation.getRuleId());
            opResponse.setState(operation.getState().toString());
            opResponse.setErrorCode(operation.getErrorCode());
            opResponse.setErrorMessage(operation.getErrorMessage());
            opResponse.setCreatedAt(operation.getCreatedAt());
            opResponse.setStartedAt(operation.getStartedAt());
            opResponse.setCompletedAt(operation.getCompletedAt());
            opResponse.setObjectName("vnfoperation");
            responseList.add(opResponse);
        }

        response.setResponses(responseList);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getCommandName() {
        return "listvnfoperationsresponse";
    }

    @Override
    public long getEntityOwnerId() {
        // TODO: Implement account ownership for multi-tenancy
        return 1; // Admin account
    }
}
