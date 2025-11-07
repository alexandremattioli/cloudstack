package org.apache.cloudstack.vnf.api.command;
import com.cloud.event.EventTypes;
import com.cloud.user.Account;

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
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.vnf.api.response.VnfFirewallRuleResponse;
import org.apache.cloudstack.vnf.service.VnfService;

import javax.inject.Inject;

@APICommand(
    name = "createVnfFirewallRule",
    description = "Creates a VNF firewall rule",
    responseObject = VnfFirewallRuleResponse.class,
    requestHasSensitiveInfo = false,
    responseHasSensitiveInfo = false,
    since = "4.21.7",
    authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User}
)
public class CreateVnfFirewallRuleCmd extends BaseAsyncCmd {

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
        description = "the ID of the network for which the firewall rule is created"
    )
    private Long networkId;

    @Parameter(
        name = "vnfinstanceid",
        type = CommandType.UUID,
        entityType = VnfFirewallRuleResponse.class,
        required = true,
        description = "the ID of the VNF instance"
    )
    private Long vnfInstanceId;

    @Parameter(
        name = "action",
        type = CommandType.STRING,
        required = true,
        description = "action to perform on traffic (allow/deny)"
    )
    private String action;

    @Parameter(
        name = ApiConstants.PROTOCOL,
        type = CommandType.STRING,
        required = true,
        description = "the protocol (tcp/udp/icmp/any)"
    )
    private String protocol;

    @Parameter(
        name = "sourceaddressing",
        type = CommandType.STRING,
        required = true,
        description = "source addressing in CIDR notation or alias"
    )
    private String sourceAddressing;

    @Parameter(
        name = "destinationaddressing",
        type = CommandType.STRING,
        required = true,
        description = "destination addressing in CIDR notation or alias"
    )
    private String destinationAddressing;

    @Parameter(
        name = "sourceports",
        type = CommandType.STRING,
        required = false,
        description = "source ports (comma-separated or range, e.g., '80,443' or '1024-65535')"
    )
    private String sourcePorts;

    @Parameter(
        name = "destinationports",
        type = CommandType.STRING,
        required = false,
        description = "destination ports (comma-separated or range, e.g., '80,443' or '1024-65535')"
    )
    private String destinationPorts;

    @Parameter(
        name = ApiConstants.DESCRIPTION,
        type = CommandType.STRING,
        required = false,
        description = "description of the firewall rule"
    )
    private String description;

    @Parameter(
        name = "ruleid",
        type = CommandType.STRING,
        required = false,
        description = "idempotent rule ID (for preventing duplicates)"
    )
    private String ruleId;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public Long getNetworkId() {
        return networkId;
    }

    public Long getVnfInstanceId() {
        return vnfInstanceId;
    }

    public String getAction() {
        return action;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getSourceAddressing() {
        return sourceAddressing;
    }

    public String getDestinationAddressing() {
        return destinationAddressing;
    }

    public String getSourcePorts() {
        return sourcePorts;
    }

    public String getDestinationPorts() {
        return destinationPorts;
    }

    public String getDescription() {
        return description;
    }

    public String getRuleId() {
        return ruleId;
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException,
                                  ServerApiException, ConcurrentOperationException,
                                  ResourceAllocationException, NetworkRuleConflictException {
        try {
            VnfFirewallRuleResponse response = vnfService.createFirewallRule(this);
            response.setResponseName(getCommandName());
            setResponseObject(response);
        } catch (Exception e) {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_FIREWALL_OPEN;
    }

    @Override
    public String getEventDescription() {
        return "Creating VNF firewall rule for network " + getNetworkId();
    }

    @Override
    public long getEntityOwnerId() {
        return Account.ACCOUNT_ID_SYSTEM;
    }
}
