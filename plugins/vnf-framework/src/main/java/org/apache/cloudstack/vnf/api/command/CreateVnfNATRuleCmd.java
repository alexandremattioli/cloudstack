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
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.vnf.api.response.VnfNATRuleResponse;
import org.apache.cloudstack.vnf.service.VnfService;

import javax.inject.Inject;

@APICommand(
    name = "createVnfNATRule",
    description = "Creates a VNF NAT/Port Forwarding rule",
    responseObject = VnfNATRuleResponse.class,
    requestHasSensitiveInfo = false,
    responseHasSensitiveInfo = false,
    since = "4.21.7",
    authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User}
)
public class CreateVnfNATRuleCmd extends BaseAsyncCmd {

    @Inject
    private VnfService vnfService;

    @Parameter(
        name = ApiConstants.NETWORK_ID,
        type = CommandType.UUID,
        entityType = NetworkResponse.class,
        required = true,
        description = "the ID of the network"
    )
    private Long networkId;

    @Parameter(
        name = "vnfinstanceid",
        type = CommandType.UUID,
        required = true,
        description = "the ID of the VNF instance"
    )
    private Long vnfInstanceId;

    @Parameter(
        name = "publicip",
        type = CommandType.STRING,
        required = true,
        description = "public IP address"
    )
    private String publicIp;

    @Parameter(
        name = "publicport",
        type = CommandType.INTEGER,
        required = true,
        description = "public port"
    )
    private Integer publicPort;

    @Parameter(
        name = "privateip",
        type = CommandType.STRING,
        required = true,
        description = "private IP address"
    )
    private String privateIp;

    @Parameter(
        name = "privateport",
        type = CommandType.INTEGER,
        required = true,
        description = "private port"
    )
    private Integer privatePort;

    @Parameter(
        name = ApiConstants.PROTOCOL,
        type = CommandType.STRING,
        required = true,
        description = "the protocol (tcp/udp)"
    )
    private String protocol;

    public Long getNetworkId() {
        return networkId;
    }

    public Long getVnfInstanceId() {
        return vnfInstanceId;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public Integer getPublicPort() {
        return publicPort;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public Integer getPrivatePort() {
        return privatePort;
    }

    public String getProtocol() {
        return protocol;
    }

    @Override
    public void execute() throws ResourceUnavailableException, InsufficientCapacityException,
            ServerApiException, ConcurrentOperationException, ResourceAllocationException, NetworkRuleConflictException {
        VnfNATRuleResponse response = vnfService.createVnfNATRule(this);
        response.setResponseName(getCommandName());
        this.setResponseObject(response);
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NET_RULE_ADD;
    }

    @Override
    public String getEventDescription() {
        return "Creating VNF NAT rule for network " + networkId;
    }

    @Override
    public long getEntityOwnerId() {
        return getAccountId();
    }
}
