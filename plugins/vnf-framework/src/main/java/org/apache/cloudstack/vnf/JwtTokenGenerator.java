package org.apache.cloudstack.vnf;

public interface JwtTokenGenerator {
    /**
     * Generate JWT token for VNF request
     */
    String generateToken(
        VnfAppliance appliance,
        String operation,
        int expirySeconds
    );
    /**
     * Validate JWT token
     */
    boolean validateToken(String token);
}