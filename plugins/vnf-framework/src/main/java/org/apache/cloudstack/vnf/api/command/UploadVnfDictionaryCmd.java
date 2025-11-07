package org.apache.cloudstack.vnf.api.command;

import org.apache.cloudstack.acl.RoleType;
import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.BaseCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.response.SuccessResponse;
import org.apache.cloudstack.vnf.VnfService;

import javax.inject.Inject;

@APICommand(
    name = "uploadVnfDictionary",
    description = "Upload VNF vendor dictionary (YAML format) for API translation",
    responseObject = SuccessResponse.class,
    requestHasSensitiveInfo = false,
    responseHasSensitiveInfo = false,
    since = "4.21.0",
    authorized = {RoleType.Admin}
)
public class UploadVnfDictionaryCmd extends BaseCmd {

    @Inject
    private VnfService vnfService;

    @Parameter(
        name = "vendor",
        type = CommandType.STRING,
        required = true,
        description = "Vendor name (e.g., pfsense, fortigate, paloalto, vyos)"
    )
    private String vendor;

    @Parameter(
        name = "version",
        type = CommandType.STRING,
        required = true,
        description = "Vendor API version (e.g., 2.7.0)"
    )
    private String version;

    @Parameter(
        name = "dictionary",
        type = CommandType.STRING,
        required = true,
        length = 65535,
        description = "YAML dictionary content with API mappings"
    )
    private String dictionary;

    @Parameter(
        name = "overwrite",
        type = CommandType.BOOLEAN,
        description = "Overwrite existing dictionary if present"
    )
    private Boolean overwrite;

    public String getVendor() {
        return vendor;
    }

    public String getVersion() {
        return version;
    }

    public String getDictionary() {
        return dictionary;
    }

    public Boolean getOverwrite() {
        return overwrite != null ? overwrite : false;
    }

    @Override
    public void execute() {
        String dictionaryId = vnfService.uploadVnfDictionary(this);
        SuccessResponse response = new SuccessResponse();
        response.setSuccess(dictionaryId != null);
        if (dictionaryId != null) {
            response.setDisplayText("VNF dictionary uploaded successfully: " + dictionaryId);
        } else {
            response.setDisplayText("VNF dictionary upload failed");
        }
        response.setResponseName(getCommandName());
        setResponseObject(response);
    }

    @Override
    public long getEntityOwnerId() {
        return _accountService.getSystemAccount().getAccountId();
    }
}
