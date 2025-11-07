package org.apache.cloudstack.vnf;

public interface VnfBrokerClient {
    /**
     * Send a request to VNF device and get response
     */
    VnfResponse sendRequest(VnfAppliance appliance, VnfRequest request)
        throws CommunicationException;
    /**
     * Send request with retry logic
     */
    VnfResponse sendRequestWithRetry(
        VnfAppliance appliance,
        VnfRequest request,
        int maxRetries
    ) throws CommunicationException;
    /**
     * Test basic connectivity
     */
    boolean isReachable(VnfAppliance appliance);
    /**
     * Get broker type (VR, Direct, External)
     */
    BrokerType getBrokerType();
}
/**
 * Response parser interface
 */