package org.apache.cloudstack.vnf;

public class VnfConnectivityResult {
    private boolean reachable;
    private long latencyMs;
    private String method;
    private int responseCode;
    private String message;
    public boolean isReachable() { return reachable; }
    public void setReachable(boolean reachable) { this.reachable = reachable; }
    public long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(long ms) { this.latencyMs = ms; }
}
// =====================================================
// 3. ENUMERATIONS
// =====================================================