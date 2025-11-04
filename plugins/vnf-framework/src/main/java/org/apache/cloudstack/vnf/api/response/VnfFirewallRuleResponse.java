package org.apache.cloudstack.vnf.api.response;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

@EntityReference(value = {})
public class VnfFirewallRuleResponse extends BaseResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the VNF firewall rule")
    private String id;

    @SerializedName("ruleid")
    @Param(description = "the idempotent rule ID")
    private String ruleId;

    @SerializedName("vnfinstanceid")
    @Param(description = "the VNF instance ID")
    private String vnfInstanceId;

    @SerializedName(ApiConstants.NETWORK_ID)
    @Param(description = "the network ID")
    private String networkId;

    @SerializedName("action")
    @Param(description = "action (allow/deny)")
    private String action;

    @SerializedName(ApiConstants.PROTOCOL)
    @Param(description = "the protocol")
    private String protocol;

    @SerializedName("sourceaddressing")
    @Param(description = "source addressing")
    private String sourceAddressing;

    @SerializedName("destinationaddressing")
    @Param(description = "destination addressing")
    private String destinationAddressing;

    @SerializedName("sourceports")
    @Param(description = "source ports")
    private String sourcePorts;

    @SerializedName("destinationports")
    @Param(description = "destination ports")
    private String destinationPorts;

    @SerializedName(ApiConstants.DESCRIPTION)
    @Param(description = "description")
    private String description;

    @SerializedName("vendorref")
    @Param(description = "vendor reference ID from the VNF appliance")
    private String vendorRef;

    @SerializedName(ApiConstants.STATE)
    @Param(description = "the state of the operation")
    private String state;

    @SerializedName("errorcode")
    @Param(description = "error code if operation failed")
    private String errorCode;

    @SerializedName("errormessage")
    @Param(description = "error message if operation failed")
    private String errorMessage;

    @SerializedName(ApiConstants.CREATED)
    @Param(description = "the date this rule was created")
    private String created;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getVnfInstanceId() {
        return vnfInstanceId;
    }

    public void setVnfInstanceId(String vnfInstanceId) {
        this.vnfInstanceId = vnfInstanceId;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSourceAddressing() {
        return sourceAddressing;
    }

    public void setSourceAddressing(String sourceAddressing) {
        this.sourceAddressing = sourceAddressing;
    }

    public String getDestinationAddressing() {
        return destinationAddressing;
    }

    public void setDestinationAddressing(String destinationAddressing) {
        this.destinationAddressing = destinationAddressing;
    }

    public String getSourcePorts() {
        return sourcePorts;
    }

    public void setSourcePorts(String sourcePorts) {
        this.sourcePorts = sourcePorts;
    }

    public String getDestinationPorts() {
        return destinationPorts;
    }

    public void setDestinationPorts(String destinationPorts) {
        this.destinationPorts = destinationPorts;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVendorRef() {
        return vendorRef;
    }

    public void setVendorRef(String vendorRef) {
        this.vendorRef = vendorRef;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}
