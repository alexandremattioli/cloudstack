package org.apache.cloudstack.vnf.api.response;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;
import org.apache.cloudstack.api.EntityReference;

@EntityReference(value = {})
public class VnfNATRuleResponse extends BaseResponse {

    @SerializedName(ApiConstants.ID)
    @Param(description = "the ID of the VNF NAT rule")
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

    @SerializedName("publicip")
    @Param(description = "public IP address")
    private String publicIp;

    @SerializedName("publicport")
    @Param(description = "public port")
    private Integer publicPort;

    @SerializedName("privateip")
    @Param(description = "private IP address")
    private String privateIp;

    @SerializedName("privateport")
    @Param(description = "private port")
    private Integer privatePort;

    @SerializedName(ApiConstants.PROTOCOL)
    @Param(description = "the protocol")
    private String protocol;

    @SerializedName("state")
    @Param(description = "state of the rule")
    private String state;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }

    public String getVnfInstanceId() { return vnfInstanceId; }
    public void setVnfInstanceId(String vnfInstanceId) { this.vnfInstanceId = vnfInstanceId; }

    public String getNetworkId() { return networkId; }
    public void setNetworkId(String networkId) { this.networkId = networkId; }

    public String getPublicIp() { return publicIp; }
    public void setPublicIp(String publicIp) { this.publicIp = publicIp; }

    public Integer getPublicPort() { return publicPort; }
    public void setPublicPort(Integer publicPort) { this.publicPort = publicPort; }

    public String getPrivateIp() { return privateIp; }
    public void setPrivateIp(String privateIp) { this.privateIp = privateIp; }

    public Integer getPrivatePort() { return privatePort; }
    public void setPrivatePort(Integer privatePort) { this.privatePort = privatePort; }

    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
