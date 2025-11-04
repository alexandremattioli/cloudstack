package org.apache.cloudstack.vnf.entity;

import org.apache.cloudstack.api.InternalIdentity;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "vnf_devices")
public class VnfDeviceVO implements InternalIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", nullable = false, unique = true)
    private String uuid;

    @Column(name = "vnf_instance_id", nullable = false)
    private Long vnfInstanceId;

    @Column(name = "network_id", nullable = false)
    private Long networkId;

    @Column(name = "vendor", nullable = false, length = 64)
    private String vendor;

    @Column(name = "broker_url", nullable = false, length = 512)
    private String brokerUrl;

    @Column(name = "management_ip", length = 40)
    private String managementIp;

    @Column(name = "api_credentials", columnDefinition = "TEXT")
    private String apiCredentials;

    @Column(name = "state", nullable = false, length = 32)
    private String state = "Enabled";

    @Column(name = "created", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "removed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date removed;

    public VnfDeviceVO() {
        this.uuid = UUID.randomUUID().toString();
        this.created = new Date();
    }

    public VnfDeviceVO(Long vnfInstanceId, Long networkId, String vendor, String brokerUrl) {
        this();
        this.vnfInstanceId = vnfInstanceId;
        this.networkId = networkId;
        this.vendor = vendor;
        this.brokerUrl = brokerUrl;
    }

    @Override
    public long getId() {
        return id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getVnfInstanceId() {
        return vnfInstanceId;
    }

    public void setVnfInstanceId(Long vnfInstanceId) {
        this.vnfInstanceId = vnfInstanceId;
    }

    public Long getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Long networkId) {
        this.networkId = networkId;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getManagementIp() {
        return managementIp;
    }

    public void setManagementIp(String managementIp) {
        this.managementIp = managementIp;
    }

    public String getApiCredentials() {
        return apiCredentials;
    }

    public void setApiCredentials(String apiCredentials) {
        this.apiCredentials = apiCredentials;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(Date removed) {
        this.removed = removed;
    }

    public enum State {
        Enabled,
        Disabled,
        Maintenance
    }

    public enum Vendor {
        PFSENSE("pfSense"),
        FORTIGATE("FortiGate"),
        PALO_ALTO("PaloAlto"),
        VYOS("VyOS");

        private final String displayName;

        Vendor(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}
