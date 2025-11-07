package org.apache.cloudstack.vnf.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Client for communicating with the VNF Broker service running on Virtual Router
 */
public class VnfBrokerClient {
    private static final Logger LOGGER = Logger.getLogger(VnfBrokerClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final String brokerUrl;
    private final String jwtToken;
    private final int timeoutMs;
    private final int maxRetries;

    public VnfBrokerClient(String brokerUrl, String jwtToken) {
        this(brokerUrl, jwtToken, 30000, 3);
    }

    public VnfBrokerClient(String brokerUrl, String jwtToken, int timeoutMs, int maxRetries) {
        this.brokerUrl = brokerUrl;
        this.jwtToken = jwtToken;
        this.timeoutMs = timeoutMs;
        this.maxRetries = maxRetries;
    }

    /**
     * Create a firewall rule on the VNF device
     */
    public VnfOperationResponse createFirewallRule(CreateFirewallRuleRequest request) throws VnfBrokerException {
        return executeOperation("/firewall/rules", request);
    }

    /**
     * Delete a firewall rule from the VNF device
     */
    public VnfOperationResponse deleteFirewallRule(String ruleId) throws VnfBrokerException {
        Map<String, Object> request = new HashMap<>();
        request.put("ruleId", ruleId);
        return executeOperation("/firewall/rules/delete", request);
    }

    /**
     * Create a NAT rule on the VNF device
     */
    public VnfOperationResponse createNatRule(CreateNatRuleRequest request) throws VnfBrokerException {
        return executeOperation("/nat/rules", request);
    }

    /**
     * Delete a NAT rule from the VNF device
     */
    public VnfOperationResponse deleteNatRule(String ruleId) throws VnfBrokerException {
        Map<String, Object> request = new HashMap<>();
        request.put("ruleId", ruleId);
        return executeOperation("/nat/rules/delete", request);
    }

    /**
     * Execute an operation with retry logic
     */
    private VnfOperationResponse executeOperation(String endpoint, Object request) throws VnfBrokerException {
        int attempt = 0;
        VnfBrokerException lastException = null;

        while (attempt < maxRetries) {
            try {
                return performHttpRequest(endpoint, request);
            } catch (VnfBrokerException e) {
                lastException = e;
                attempt++;

                // Retry on timeout or rate limit errors
                if (e.getErrorCode() != null && 
                    (e.getErrorCode().equals("VNF_TIMEOUT") || e.getErrorCode().equals("VNF_RATE_LIMIT"))) {
                    LOGGER.warn("VNF operation failed with " + e.getErrorCode() + ", attempt " + attempt + "/" + maxRetries);
                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(1000 * attempt); // Exponential backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw e;
                        }
                        continue;
                    }
                }
                throw e;
            }
        }

        throw lastException;
    }

    /**
     * Perform HTTP POST request to broker
     */
    private VnfOperationResponse performHttpRequest(String endpoint, Object request) throws VnfBrokerException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(brokerUrl + endpoint);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + jwtToken);

            String jsonPayload = objectMapper.writeValueAsString(request);
            httpPost.setEntity(new StringEntity(jsonPayload));

            HttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                return parseSuccessResponse(responseBody);
            } else {
                return parseErrorResponse(responseBody, statusCode);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to communicate with VNF broker: " + e.getMessage(), e);
            throw new VnfBrokerException("VNF_UNREACHABLE", "Failed to reach VNF broker: " + e.getMessage(), e);
        }
    }

    private VnfOperationResponse parseSuccessResponse(String responseBody) throws VnfBrokerException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            VnfOperationResponse response = new VnfOperationResponse();
            response.setSuccess(true);
            response.setVendorRef(root.path("vendorRef").asText());
            response.setMessage(root.path("message").asText());
            return response;
        } catch (IOException e) {
            throw new VnfBrokerException("BROKER_INTERNAL", "Failed to parse broker response", e);
        }
    }

    private VnfOperationResponse parseErrorResponse(String responseBody, int statusCode) throws VnfBrokerException {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String errorCode = root.path("errorCode").asText();
            String errorMessage = root.path("message").asText();
            
            VnfOperationResponse response = new VnfOperationResponse();
            response.setSuccess(false);
            response.setErrorCode(errorCode);
            response.setMessage(errorMessage);
            
            throw new VnfBrokerException(errorCode, errorMessage);
        } catch (IOException e) {
            throw new VnfBrokerException("BROKER_INTERNAL", "HTTP " + statusCode + ": " + responseBody);
        }
    }

    /**
     * Request class for creating firewall rules
     */
    public static class CreateFirewallRuleRequest {
        private String ruleId;
        private String action;
        private String protocol;
        private String sourceAddressing;
        private String destinationAddressing;
        private String sourcePorts;
        private String destinationPorts;
        private String description;

        // Getters and setters
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getProtocol() { return protocol; }
        public void setProtocol(String protocol) { this.protocol = protocol; }
        public String getSourceAddressing() { return sourceAddressing; }
        public void setSourceAddressing(String sourceAddressing) { this.sourceAddressing = sourceAddressing; }
        public String getDestinationAddressing() { return destinationAddressing; }
        public void setDestinationAddressing(String destinationAddressing) { this.destinationAddressing = destinationAddressing; }
        public String getSourcePorts() { return sourcePorts; }
        public void setSourcePorts(String sourcePorts) { this.sourcePorts = sourcePorts; }
        public String getDestinationPorts() { return destinationPorts; }
        public void setDestinationPorts(String destinationPorts) { this.destinationPorts = destinationPorts; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * Request class for creating NAT rules
     */
    public static class CreateNatRuleRequest {
        private String ruleId;
        private String type; // SNAT, DNAT
        private String sourceAddress;
        private String destinationAddress;
        private String translatedAddress;
        private String protocol;
        private String port;
        private String translatedPort;
        private String description;

        // Getters and setters
        public String getRuleId() { return ruleId; }
        public void setRuleId(String ruleId) { this.ruleId = ruleId; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getSourceAddress() { return sourceAddress; }
        public void setSourceAddress(String sourceAddress) { this.sourceAddress = sourceAddress; }
        public String getDestinationAddress() { return destinationAddress; }
        public void setDestinationAddress(String destinationAddress) { this.destinationAddress = destinationAddress; }
        public String getTranslatedAddress() { return translatedAddress; }
        public void setTranslatedAddress(String translatedAddress) { this.translatedAddress = translatedAddress; }
        public String getProtocol() { return protocol; }
        public void setProtocol(String protocol) { this.protocol = protocol; }
        public String getPort() { return port; }
        public void setPort(String port) { this.port = port; }
        public String getTranslatedPort() { return translatedPort; }
        public void setTranslatedPort(String translatedPort) { this.translatedPort = translatedPort; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * Response from VNF broker operations
     */
    public static class VnfOperationResponse {
        private boolean success;
        private String vendorRef;
        private String errorCode;
        private String message;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getVendorRef() { return vendorRef; }
        public void setVendorRef(String vendorRef) { this.vendorRef = vendorRef; }
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * Exception for VNF broker communication errors
     */
    public static class VnfBrokerException extends Exception {
        private final String errorCode;

        public VnfBrokerException(String errorCode, String message) {
            super(message);
            this.errorCode = errorCode;
        }

        public VnfBrokerException(String errorCode, String message, Throwable cause) {
            super(message, cause);
            this.errorCode = errorCode;
        }

        public String getErrorCode() {
            return errorCode;
        }
    }
}
