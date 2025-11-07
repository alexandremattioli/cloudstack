package org.apache.cloudstack.vnf;

import java.util.Date;
import java.util.Map;

public class VnfDictionary {
    private String id;
    private String uuid;
    private Long templateId;
    private Long networkId;
    private String name;
    private String yamlContent;
    private String schemaVersion;
    private String vendor;
    private String product;
    private Date created;
    private Date updated;
    // Parsed structure
    private AccessConfig accessConfig;
    private Map<String, ServiceDefinition> services;
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(String version) { this.schemaVersion = version; }
    public AccessConfig getAccessConfig() { return accessConfig; }
    public void setAccessConfig(AccessConfig config) { this.accessConfig = config; }
    public Map<String, ServiceDefinition> getServices() { return services; }
    public void setServices(Map<String, ServiceDefinition> services) { this.services = services; }
    public ServiceDefinition getService(String name) { return services.get(name); }
}
/**
 * Access configuration from dictionary
 */