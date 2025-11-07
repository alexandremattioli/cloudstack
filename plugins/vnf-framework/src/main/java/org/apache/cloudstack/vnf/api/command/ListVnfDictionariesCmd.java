package org.apache.cloudstack.vnf.api.command;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.vnf.VnfService;
import org.apache.cloudstack.vnf.api.response.VnfDictionaryResponse;

import javax.inject.Inject;
import java.util.List;

@APICommand(
    name = "listVnfDictionaries",
    description = "List available VNF vendor dictionaries",
    responseObject = VnfDictionaryResponse.class,
    requestHasSensitiveInfo = false,
    responseHasSensitiveInfo = false,
    since = "4.21.0",
    authorized = {RoleType.Admin, RoleType.ResourceAdmin, RoleType.DomainAdmin}
)
public class ListVnfDictionariesCmd extends BaseListCmd {

    @Inject
    private VnfService vnfService;

    @Parameter(
        name = "vendor",
        type = CommandType.STRING,
        description = "Filter by vendor name"
    )
    private String vendor;

    @Parameter(
        name = "version",
        type = CommandType.STRING,
        description = "Filter by version"
    )
    private String version;

    public String getVendor() {
        return vendor;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public void execute() {
        List<VnfDictionaryResponse> dictionaries = vnfService.listVnfDictionaries(this);
        ListResponse<VnfDictionaryResponse> response = new ListResponse<>();
        response.setResponses(dictionaries);
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public long getEntityOwnerId() {
        return _accountService.getSystemAccount().getAccountId();
    }
}
