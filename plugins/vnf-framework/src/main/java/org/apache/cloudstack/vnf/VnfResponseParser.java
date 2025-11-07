package org.apache.cloudstack.vnf;

import java.util.List;
import java.util.Map;

public interface VnfResponseParser {
    /**
     * Parse response and extract external ID
     */
    String extractExternalId(VnfResponse response, VnfDictionary dictionary, String operation);
    /**
     * Parse list response into structured data
     */
    List<VnfDeviceRule> parseListResponse(
        VnfResponse response,
        VnfDictionary dictionary,
        String serviceName
    );
    /**
     * Check if response indicates success
     */
    boolean isSuccess(VnfResponse response, VnfDictionary dictionary, String operation);
    /**
     * Extract error message from response
     */
    String extractErrorMessage(VnfResponse response);
}
// =====================================================
// 2. DATA MODELS
// =====================================================
/**
 * Represents a VNF dictionary
 */