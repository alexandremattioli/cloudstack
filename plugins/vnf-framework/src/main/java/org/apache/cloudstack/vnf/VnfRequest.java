package org.apache.cloudstack.vnf;

import java.util.Map;

public class VnfRequest {
    private String targetIp;
    private String protocol;
    private String method;
    private String uri;
    private Map<String, String> headers;
    private String body;
    private int timeoutSeconds;
    private String jwtToken;  // For broker authorization
    // Getters and setters
    public String getTargetIp() { return targetIp; }
    public void setTargetIp(String ip) { this.targetIp = ip; }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getJwtToken() { return jwtToken; }
    public void setJwtToken(String token) { this.jwtToken = token; }
}
/**
 * VNF response from device
 */