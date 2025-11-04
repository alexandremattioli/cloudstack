// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.cloudstack.vnf.client;

import com.cloud.exception.CloudException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * VnfBrokerClient - HTTP client for communicating with VNF broker
 * Supports JWT authentication, retry logic, and mTLS
 */
@Component
public class VnfBrokerClient {
    
    private static final Logger s_logger = Logger.getLogger(VnfBrokerClient.class);
    
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY_MS = 1000;
    
    private final CloseableHttpClient httpClient;
    private final SecretKey jwtKey;
    
    public VnfBrokerClient() {
        this.httpClient = createHttpClient();
        this.jwtKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }
    
    /**
     * Create HTTP client with SSL and timeout configuration
     */
    private CloseableHttpClient createHttpClient() {
        try {
            // Accept self-signed certificates (for dev/test)
            SSLContextBuilder sslBuilder = new SSLContextBuilder();
            sslBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                sslBuilder.build(),
                NoopHostnameVerifier.INSTANCE
            );
            
            RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(DEFAULT_TIMEOUT_SECONDS * 1000)
                .setSocketTimeout(DEFAULT_TIMEOUT_SECONDS * 1000)
                .setConnectionRequestTimeout(DEFAULT_TIMEOUT_SECONDS * 1000)
                .build();
            
            return HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .setDefaultRequestConfig(requestConfig)
                .build();
                
        } catch (Exception e) {
            s_logger.error("Failed to create HTTP client", e);
            return HttpClients.createDefault();
        }
    }
    
    /**
     * Generate JWT token for broker authentication
     */
    private String generateJwtToken(String brokerUrl, String operation) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiration = new Date(nowMillis + (5 * 60 * 1000)); // 5 minutes
        
        return Jwts.builder()
            .setIssuer("cloudstack")
            .setSubject("vnf-framework")
            .setAudience(brokerUrl)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .claim("operation", operation)
            .signWith(jwtKey)
            .compact();
    }
    
    /**
     * Send HTTP POST request to broker
     */
    public BrokerResponse post(String url, String operation, Map<String, Object> payload) throws CloudException {
        return sendRequestWithRetry("POST", url, operation, payload, MAX_RETRIES);
    }
    
    /**
     * Send HTTP GET request to broker
     */
    public BrokerResponse get(String url, String operation) throws CloudException {
        return sendRequestWithRetry("GET", url, operation, null, MAX_RETRIES);
    }
    
    /**
     * Send HTTP DELETE request to broker
     */
    public BrokerResponse delete(String url, String operation, Map<String, Object> payload) throws CloudException {
        return sendRequestWithRetry("DELETE", url, operation, payload, MAX_RETRIES);
    }
    
    /**
     * Send HTTP PUT request to broker
     */
    public BrokerResponse put(String url, String operation, Map<String, Object> payload) throws CloudException {
        return sendRequestWithRetry("PUT", url, operation, payload, MAX_RETRIES);
    }
    
    /**
     * Send request with retry logic
     */
    private BrokerResponse sendRequestWithRetry(String method, String url, String operation, 
                                                 Map<String, Object> payload, int retriesLeft) throws CloudException {
        long startTime = System.currentTimeMillis();
        
        try {
            HttpUriRequest request = buildRequest(method, url, operation, payload);
            
            s_logger.debug("Sending " + method + " request to: " + url);
            
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            
            long duration = System.currentTimeMillis() - startTime;
            
            BrokerResponse brokerResponse = new BrokerResponse();
            brokerResponse.setStatusCode(statusCode);
            brokerResponse.setBody(responseBody);
            brokerResponse.setDurationMs((int) duration);
            brokerResponse.setSuccess(statusCode >= 200 && statusCode < 300);
            
            if (!brokerResponse.isSuccess() && retriesLeft > 0) {
                s_logger.warn("Request failed with status " + statusCode + ", retrying... (" + retriesLeft + " retries left)");
                Thread.sleep(RETRY_DELAY_MS * (MAX_RETRIES - retriesLeft + 1)); // Exponential backoff
                return sendRequestWithRetry(method, url, operation, payload, retriesLeft - 1);
            }
            
            return brokerResponse;
            
        } catch (IOException e) {
            if (retriesLeft > 0) {
                s_logger.warn("Request failed with IOException, retrying... (" + retriesLeft + " retries left)", e);
                try {
                    Thread.sleep(RETRY_DELAY_MS * (MAX_RETRIES - retriesLeft + 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return sendRequestWithRetry(method, url, operation, payload, retriesLeft - 1);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            throw new CloudException("Failed to communicate with VNF broker: " + e.getMessage(), e);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CloudException("Request interrupted: " + e.getMessage(), e);
        }
    }
    
    /**
     * Build HTTP request with headers and body
     */
    private HttpUriRequest buildRequest(String method, String url, String operation, Map<String, Object> payload) throws IOException {
        HttpUriRequest request;
        
        switch (method.toUpperCase()) {
            case "POST":
                HttpPost post = new HttpPost(url);
                if (payload != null) {
                    String jsonPayload = convertToJson(payload);
                    post.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));
                }
                request = post;
                break;
                
            case "PUT":
                HttpPut put = new HttpPut(url);
                if (payload != null) {
                    String jsonPayload = convertToJson(payload);
                    put.setEntity(new StringEntity(jsonPayload, StandardCharsets.UTF_8));
                }
                request = put;
                break;
                
            case "DELETE":
                HttpDelete delete = new HttpDelete(url);
                request = delete;
                break;
                
            case "GET":
            default:
                request = new HttpGet(url);
                break;
        }
        
        // Add headers
        String jwtToken = generateJwtToken(url, operation);
        request.addHeader("Authorization", "Bearer " + jwtToken);
        request.addHeader("Content-Type", "application/json");
        request.addHeader("Accept", "application/json");
        request.addHeader("X-VNF-Operation", operation);
        
        return request;
    }
    
    /**
     * Convert payload map to JSON string
     */
    private String convertToJson(Map<String, Object> payload) {
        // Simple JSON conversion - in production, use Jackson ObjectMapper
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");
            
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else {
                json.append("\"").append(value.toString()).append("\"");
            }
            first = false;
        }
        json.append("}");
        return json.toString();
    }
    
    /**
     * Test connectivity to broker
     */
    public boolean testConnectivity(String brokerUrl) {
        try {
            BrokerResponse response = get(brokerUrl + "/health", "health_check");
            return response.isSuccess();
        } catch (CloudException e) {
            s_logger.warn("Connectivity test failed for " + brokerUrl, e);
            return false;
        }
    }
    
    /**
     * Close HTTP client
     */
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            s_logger.warn("Failed to close HTTP client", e);
        }
    }
    
    /**
     * BrokerResponse - Response wrapper
     */
    public static class BrokerResponse {
        private int statusCode;
        private String body;
        private int durationMs;
        private boolean success;
        
        public int getStatusCode() {
            return statusCode;
        }
        
        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }
        
        public String getBody() {
            return body;
        }
        
        public void setBody(String body) {
            this.body = body;
        }
        
        public int getDurationMs() {
            return durationMs;
        }
        
        public void setDurationMs(int durationMs) {
            this.durationMs = durationMs;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
    }
}
