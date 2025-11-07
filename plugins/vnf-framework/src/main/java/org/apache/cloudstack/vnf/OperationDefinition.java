package org.apache.cloudstack.vnf;

import java.util.Map;

public class OperationDefinition {
    private String method;       // HTTP method or "SSH"
    private String endpoint;     // URL path or CLI command
    private String body;         // Request body template
    private Map<String, String> headers;
    private ResponseMapping responseMapping;
    private String successPattern;  // For CLI responses
    // Getters and setters
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public ResponseMapping getResponseMapping() { return responseMapping; }
    public void setResponseMapping(ResponseMapping mapping) { this.responseMapping = mapping; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
    public String getSuccessPattern() { return successPattern; }
    public void setSuccessPattern(String pattern) { this.successPattern = pattern; }
}