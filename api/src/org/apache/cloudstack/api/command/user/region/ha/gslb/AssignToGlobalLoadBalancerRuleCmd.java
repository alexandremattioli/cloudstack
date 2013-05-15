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

package org.apache.cloudstack.api.command.user.region.ha.gslb;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.FirewallRuleResponse;
import org.apache.cloudstack.api.response.GlobalLoadBalancerResponse;
import org.apache.cloudstack.api.response.SuccessResponse;

import com.cloud.event.EventTypes;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.region.ha.GlobalLoadBalancerRule;
import com.cloud.region.ha.GlobalLoadBalancingRulesService;
import com.cloud.user.Account;
import com.cloud.user.UserContext;
import com.cloud.utils.StringUtils;

@APICommand(name = "assignToGlobalLoadBalancerRule", description="Assign load balancer rule or list of load " +
        "balancer rules to a global load balancer rules.", responseObject=SuccessResponse.class)
public class AssignToGlobalLoadBalancerRuleCmd extends BaseAsyncCmd {

    public static final Logger s_logger = Logger.getLogger(AssignToGlobalLoadBalancerRuleCmd.class.getName());

    private static final String s_name = "assigntogloballoadbalancerruleresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.ID, type=CommandType.UUID, entityType = GlobalLoadBalancerResponse.class,
            required=true, description="the ID of the global load balancer rule")
    private Long id;

    @Parameter(name=ApiConstants.LOAD_BALANCER_RULE_LIST, type=CommandType.LIST, collectionType=CommandType.UUID,
            entityType = FirewallRuleResponse.class, required=true, description="the list load balancer rules that " +
            "will be assigned to gloabal load balacner rule")
    private List<Long> loadBalancerRulesIds;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getGlobalLoadBalancerRuleId() {
        return id;
    }

    public List<Long> getLoadBalancerRulesIds() {
        return loadBalancerRulesIds;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Inject
    public GlobalLoadBalancingRulesService _gslbService;

    @Override
    public String getCommandName() {
        return s_name;
    }

    @Override
    public long getEntityOwnerId() {
        GlobalLoadBalancerRule globalLoadBalancerRule = _entityMgr.findById(GlobalLoadBalancerRule.class,
                getGlobalLoadBalancerRuleId());
        if (globalLoadBalancerRule == null) {
            return Account.ACCOUNT_ID_SYSTEM; // bad id given, parent this command to SYSTEM so ERROR events are tracked
        }
        return globalLoadBalancerRule.getAccountId();
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_ASSIGN_TO_GLOBAL_LOAD_BALANCER_RULE;
    }

    @Override
    public String getEventDescription() {
        return "applying load balancer rules " + StringUtils.join(getLoadBalancerRulesIds(), ",") +
                " to global load balancer rule " + getGlobalLoadBalancerRuleId();
    }

    @Override
    public void execute(){
        UserContext.current().setEventDetails("Global Load balancer rule Id: "+ getGlobalLoadBalancerRuleId()+ " VmIds: "
                + StringUtils.join(getLoadBalancerRulesIds(), ","));
        boolean result = _gslbService.assignToGlobalLoadBalancerRule(this);
        if (result) {
            SuccessResponse response = new SuccessResponse(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to assign global load balancer rule");
        }
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.gslbSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        GlobalLoadBalancerRule gslb = _gslbService.findById(id);
        if(gslb == null){
            throw new InvalidParameterValueException("Unable to find load balancer rule: " + id);
        }
        return gslb.getId();
    }
}
