package org.apache.cloudstack.vnf;

public class AccessConfig {
    private String protocol;  // "https", "ssh", "http"
    private int port;
    private String basePath;
    private AuthType authType;
    private String usernameRef;
    private String passwordRef;
    private String tokenRef;
    private String tokenHeader;
    // Getters and setters
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public AuthType getAuthType() { return authType; }
    public void setAuthType(AuthType authType) { this.authType = authType; }
    public String getBasePath() { return basePath; }
    public void setBasePath(String basePath) { this.basePath = basePath; }
    public String getUsernameRef() { return usernameRef; }
    public void setUsernameRef(String usernameRef) { this.usernameRef = usernameRef; }
    public String getPasswordRef() { return passwordRef; }
    public void setPasswordRef(String passwordRef) { this.passwordRef = passwordRef; }
    public String getTokenRef() { return tokenRef; }
    public void setTokenRef(String tokenRef) { this.tokenRef = tokenRef; }
    public String getTokenHeader() { return tokenHeader; }
    public void setTokenHeader(String tokenHeader) { this.tokenHeader = tokenHeader; }
}
/**
 * Service definition (e.g., Firewall, NAT)
 */