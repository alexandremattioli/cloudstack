package org.apache.cloudstack.vnf;

public class VnfResponse {
    private int statusCode;
    private String body;
    private Map<String, String> headers;
    private long durationMs;
    private boolean success;
    private String errorMessage;
    // Getters and setters
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int code) { this.statusCode = code; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long ms) { this.durationMs = ms; }
}
/**
 * Device rule structure from list operation
 */