package org.apache.cloudstack.vnf;

public class VnfDeviceRule {
    private String externalId;
    private String serviceName;  // "Firewall", "NAT", etc.
    private Map<String, Object> properties;
    public String getExternalId() { return externalId; }
    public void setExternalId(String id) { this.externalId = id; }
    public Map<String, Object> getProperties() { return properties; }
    public void setProperty(String key, Object value) { properties.put(key, value); }
}
/**
 * Reconciliation result
 */