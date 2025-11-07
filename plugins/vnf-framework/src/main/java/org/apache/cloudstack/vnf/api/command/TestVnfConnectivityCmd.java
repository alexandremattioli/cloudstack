package org.apache.cloudstack.vnf.api.command;

import com.cloud.event.EventTypes;
import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.vnf.VnfService;
import org.apache.cloudstack.vnf.api.response.VnfInstanceResponse;

import javax.inject.Inject;

@APICommand(
    name = "testVnfConnectivity",
    description = "Test connectivity to VNF broker and verify VNF instance is reachable",
    responseObject = SuccessResponse.class,
    requestHasSensitiveInfo = false,
    responseHasSensitiveInfo = false,
    since = "4.21.0",
    authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin}
)
public class TestVnfConnectivityCmd extends BaseAsyncCmd {

    @Inject
    private VnfService vnfService;

    @Parameter(
        name = ApiConstants.ID,
        type = CommandType.UUID,
        entityType = VnfInstanceResponse.class,
        description = "The ID of the VNF instance to test (optional, tests broker only if omitted)"
    )
    private Long id;

    @Parameter(
        name = "timeout",
        type = CommandType.INTEGER,
        description = "Connection timeout in seconds (default: 10)"
    )
    private Integer timeout;

    public Long getId() {
        return id;
    }

    public Integer getTimeout() {
        return timeout != null ? timeout : 10;
    }

    @Override
    public void execute() {
        String result = vnfService.testVnfConnectivity(this);
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(result != null);
        response.setDisplayText(result != null ? result : "Connectivity test failed");
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_VNF_CONNECTIVITY_TEST;
    }

    @Override
    public String getEventDescription() {
        if (id != null) {
            return "Testing connectivity to VNF instance ID: " + getId();
        }
        return "Testing connectivity to VNF broker";
    }

    @Override
    public long getEntityOwnerId() {
        if (id != null) {
            return _accountService.getAccount(getEntityOwnerAccountId()).getAccountId();
        }
        return _accountService.getSystemAccount().getAccountId();
    }

    private Long getEntityOwnerAccountId() {
        return vnfService.getVnfInstance(id).getAccountId();
    }
}
