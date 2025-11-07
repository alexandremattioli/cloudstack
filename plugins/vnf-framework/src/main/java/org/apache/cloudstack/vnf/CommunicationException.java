package org.apache.cloudstack.vnf;

public class CommunicationException extends CloudException {
    private boolean retriable;
    public CommunicationException(String message, boolean retriable) {
        super(message);
        this.retriable = retriable;
    }
    public boolean isRetriable() { return retriable; }
}
// =====================================================
// 5. VALIDATION RESULT
// =====================================================