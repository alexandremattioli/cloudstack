package org.apache.cloudstack.vnf.api.command;

import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.ResourceUnavailableException;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.vnf.api.response.VnfFirewallRuleResponse;
import org.apache.cloudstack.vnf.service.VnfService;

import javax.inject.Inject;

@APICommand(
    name = "deleteVnfFirewallRule",
    description = "Deletes a VNF firewall rule",
    responseObject = SuccessResponse.class,
    requestHasSensitiveInfo = false,
    responseHasSensitiveInfo = false,
    since = "4.21.7",
    authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin, RoleType.User}
)
public class DeleteVnfFirewallRuleCmd extends BaseAsyncCmd {

    @Inject
    private VnfService vnfService;

    @Parameter(
        name = "id",
        type = CommandType.UUID,
        entityType = VnfFirewallRuleResponse.class,
        required = true,
        description = "the ID of the firewall rule to delete"
    )
    private Long id;

    @Parameter(
        name = "vnfinstanceid",
        type = CommandType.UUID,
        entityType = VnfFirewallRuleResponse.class,
        required = true,
        description = "the ID of the VNF instance"
    )
    private Long vnfInstanceId;

    public Long getId() {
        return id;
    }

    public Long getVnfInstanceId() {
        return vnfInstanceId;
    }

    @Override
    public void execute() throws ResourceUnavailableException, ConcurrentOperationException {
        boolean result = vnfService.deleteVnfFirewallRule(this);
        SuccessResponse response = new SuccessResponse(getCommandName());
        response.setSuccess(result);
        this.setResponseObject(response);
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_FIREWALL_CLOSE;
    }

    @Override
    public String getEventDescription() {
        return "Deleting VNF firewall rule id=" + id;
    }

    @Override
    public long getEntityOwnerId() {
        return getAccountId();
    }
}
