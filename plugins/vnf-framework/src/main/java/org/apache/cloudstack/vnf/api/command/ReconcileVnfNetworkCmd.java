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

import com.cloud.event.EventTypes;
import com.cloud.exception.CloudException;
import org.apache.cloudstack.api.*;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.vnf.api.response.VnfReconciliationResponse;
import org.apache.cloudstack.vnf.entity.VnfReconciliationLogVO;
import org.apache.cloudstack.vnf.service.VnfService;
import org.apache.log4j.Logger;

import javax.inject.Inject;

/**
 * API Command to reconcile VNF network rules with device state
 */
@APICommand(
    name = "reconcileVnfNetwork",
    description = "Reconcile network rules between CloudStack and VNF device",
    responseObject = VnfReconciliationResponse.class,
    since = "4.21.0",
    authorized = {RoleType.Admin}
)
public class ReconcileVnfNetworkCmd extends BaseCmd {
    
    public static final Logger s_logger = Logger.getLogger(ReconcileVnfNetworkCmd.class);
    
    @Inject
    private VnfService vnfService;
    
    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    
    @Parameter(
        name = ApiConstants.NETWORK_ID,
        type = CommandType.UUID,
        entityType = NetworkResponse.class,
        required = true,
        description = "Network ID to reconcile"
    )
    private Long networkId;
    
    @Parameter(
        name = "dryrun",
        type = CommandType.BOOLEAN,
        description = "If true, only detect drift without fixing (default: false)"
    )
    private Boolean dryRun;
    
    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////
    
    public Long getNetworkId() {
        return networkId;
    }
    
    public Boolean getDryRun() {
        return dryRun != null ? dryRun : false;
    }
    
    /////////////////////////////////////////////////////
    /////////////// API Implementation //////////////////
    /////////////////////////////////////////////////////
    
    @Override
    public void execute() throws CloudException {
        s_logger.info("Reconciling VNF network: " + networkId + " (dryRun=" + getDryRun() + ")");
        
        VnfReconciliationLogVO log = vnfService.reconcileNetwork(networkId, getDryRun());
        
        VnfReconciliationResponse response = new VnfReconciliationResponse();
        response.setId(log.getUuid());
        response.setNetworkId(log.getNetworkId().toString());
        response.setStatus(log.getStatus().name());
        response.setStarted(log.getStarted());
        response.setCompleted(log.getCompleted());
        response.setRulesChecked(log.getRulesChecked());
        response.setMissingRules(log.getMissingRulesFound());
        response.setExtraRules(log.getExtraRulesFound());
        response.setRulesReapplied(log.getRulesReapplied());
        response.setRulesRemoved(log.getRulesRemoved());
        response.setDriftDetected(log.getDriftDetected());
        response.setErrorMessage(log.getErrorMessage());
        response.setObjectName("vnfreconciliation");
        response.setResponseName(getCommandName());
        
        setResponseObject(response);
    }
    
    @Override
    public String getCommandName() {
        return "reconcileVnfNetworkResponse";
    }
    
    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
    
    @Override
    public String getEventType() {
        return EventTypes.EVENT_VNF_RECONCILIATION;
    }
    
    @Override
    public String getEventDescription() {
        return "Reconciling VNF network " + networkId;
    }
}
