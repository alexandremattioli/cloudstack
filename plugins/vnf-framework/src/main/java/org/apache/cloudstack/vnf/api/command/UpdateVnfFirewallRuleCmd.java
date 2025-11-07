package org.apache.cloudstack.vnf.api.command;
import com.cloud.event.EventTypes;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.NetworkRuleConflictException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.vnf.api.response.VnfFirewallRuleResponse;
import org.apache.cloudstack.vnf.service.VnfService;

import javax.inject.Inject;

@APICommand(
    name = "updateVnfFirewallRule",
    description = "Updates a VNF firewall rule",
    responseObject = VnfFirewallRuleResponse.class,
    requestHasSensitiveInfo = false,
    responseHasSensitiveInfo = false,
    since = "4.21.7",
    authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User}
)
public class UpdateVnfFirewallRuleCmd extends BaseAsyncCmd {

    @Inject
    private VnfService vnfService;

    @Parameter(
        name = "id",
        type = CommandType.UUID,
        entityType = VnfFirewallRuleResponse.class,
        required = true,
        description = "the ID of the firewall rule to update"
    )
    private Long id;

    @Parameter(
        name = "vnfinstanceid",
        type = CommandType.UUID,
        required = true,
        description = "the ID of the VNF instance"
    )
    private Long vnfInstanceId;

    @Parameter(
        name = "action",
        type = CommandType.STRING,
        description = "action to perform on traffic (allow/deny)"
    )
    private String action;

    @Parameter(
        name = "enabled",
        type = CommandType.BOOLEAN,
        description = "enable or disable the rule"
    )
    private Boolean enabled;

    public Long getId() {
        return id;
    }

    public Long getVnfInstanceId() {
        return vnfInstanceId;
    }

    public String getAction() {
        return action;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException,
            ServerApiException, ConcurrentOperationException, ResourceAllocationException, NetworkRuleConflictException {
        VnfFirewallRuleResponse response = vnfService.updateVnfFirewallRule(this);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_FIREWALL_UPDATE;
    }

    @Override
    public String getEventDescription() {
        return "Updating VNF firewall rule id=" + id;
    }

    @Override
    public long getEntityOwnerId() {
        return getAccountId();
    }
}
