package org.apache.cloudstack.vnf;

import java.util.Date;
import com.cloud.vm.VirtualMachine;
import com.cloud.network.Network;
public class VnfAppliance {
    private Long id;
    private String uuid;
    private Long vmInstanceId;
    private Long networkId;
    private Long templateId;
    private Long dictionaryId;
    private String managementIp;
    private String guestIp;
    private String publicIp;
    private Long brokerVmId;
    private VnfState state;
    private HealthStatus healthStatus;
    private Date lastContact;
    private Date created;
    // Lazy-loaded associations
    private VirtualMachine vmInstance;
    private Network network;
    private VnfDictionary dictionary;
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public Long getVmInstanceId() { return vmInstanceId; }
    public void setVmInstanceId(Long id) { this.vmInstanceId = id; }
    public String getManagementIp() { return managementIp; }
    public void setManagementIp(String ip) { this.managementIp = ip; }
    public VnfState getState() { return state; }
    public void setState(VnfState state) { this.state = state; }
    public HealthStatus getHealthStatus() { return healthStatus; }
    public void setHealthStatus(HealthStatus status) { this.healthStatus = status; }
}
/**
 * VNF request to be sent to device
 */