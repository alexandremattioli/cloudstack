package org.apache.cloudstack.vnf.api.command;

import com.cloud.event.EventTypes;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.NetworkResponse;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.vnf.VnfService;

import javax.inject.Inject;

@APICommand(
    name = "reconcileVnfNetwork",
    description = "Reconcile VNF state with network configuration",
    responseObject = SuccessResponse.class,
    requestHasSensitiveInfo = false,
    responseHasSensitiveInfo = false,
    since = "4.21.0",
    authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin}
)
public class ReconcileVnfNetworkCmd extends BaseAsyncCmd {

    @Inject
    private VnfService vnfService;

    @Parameter(
        name = ApiConstants.NETWORK_ID,
        type = CommandType.UUID,
        entityType = NetworkResponse.class,
        required = true,
        description = "The ID of the network to reconcile"
    )
    private Long networkId;

    @Parameter(
        name = "force",
        type = CommandType.BOOLEAN,
        description = "Force reconciliation even if state appears synchronized"
    )
    private Boolean force;

    public Long getNetworkId() {
        return networkId;
    }

    public Boolean getForce() {
        return force != null ? force : false;
    }

    @Override
    public void execute() {
        boolean result = vnfService.reconcileVnfNetwork(this);
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(result);
        if (result) {
            response.setDisplayText("VNF network reconciliation initiated successfully");
        } else {
            response.setDisplayText("VNF network reconciliation failed");
        }
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VNF_NETWORK_RECONCILE;
    }

    @Override
    public String getEventDescription() {
        return "Reconciling VNF network configuration for network ID: " + getNetworkId();
    }

    @Override
    public long getEntityOwnerId() {
        return _accountService.getAccount(getEntityOwnerAccountId()).getAccountId();
    }

    private Long getEntityOwnerAccountId() {
        return _networkService.getNetwork(networkId).getAccountId();
    }
}
