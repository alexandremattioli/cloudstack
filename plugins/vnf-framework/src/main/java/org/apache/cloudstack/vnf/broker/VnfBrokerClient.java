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

package org.apache.cloudstack.vnf.broker;

import com.cloud.exception.CloudException;
import org.apache.cloudstack.vnf.VnfFrameworkConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Thin HTTP client wrapper around the VNF Broker.
 * Handles timeouts, retries (simple exponential backoff), authentication, and correlation IDs.
 */
public class VnfBrokerClient implements Closeable {
    private static final Logger LOGGER = LogManager.getLogger(VnfBrokerClient.class);

    private final CloseableHttpClient httpClient;
    private final String baseUrl;
    private final int maxRetries;
    private final int initialDelayMs;
    private final int maxDelayMs;
    private final String authType;
    private final String authToken;
    private final String authUser;
    private final String authPass;

    public VnfBrokerClient() {
        this.baseUrl = VnfFrameworkConfig.VnfBrokerUrl.value();
        int requestTimeout = VnfFrameworkConfig.VnfBrokerTimeout.value() * 1000;
        int connectTimeout = VnfFrameworkConfig.VnfBrokerConnectTimeout.value() * 1000;
        this.maxRetries = VnfFrameworkConfig.VnfMaxRetries.value();
        this.initialDelayMs = VnfFrameworkConfig.VnfRetryDelayMs.value();
        this.maxDelayMs = VnfFrameworkConfig.VnfRetryMaxDelayMs.value();
        this.authType = VnfFrameworkConfig.VnfBrokerAuthType.value();
        this.authToken = VnfFrameworkConfig.VnfBrokerAuthToken.value();
        this.authUser = VnfFrameworkConfig.VnfBrokerUsername.value();
        this.authPass = VnfFrameworkConfig.VnfBrokerPassword.value();

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(requestTimeout)
                .setConnectionRequestTimeout(connectTimeout)
                .build();
        this.httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
    }

    public String testConnectivity(String correlationId) throws CloudException {
        return executeGet("/health", correlationId);
    }

    public String createFirewallRule(String payload, String correlationId) throws CloudException {
        return executePost("/rules/firewall", payload, correlationId);
    }

    public String updateFirewallRule(String ruleId, String payload, String correlationId) throws CloudException {
        return executePut("/rules/firewall/" + ruleId, payload, correlationId);
    }

    public String deleteFirewallRule(String ruleId, String correlationId) throws CloudException {
        return executeDelete("/rules/firewall/" + ruleId, correlationId);
    }

    public String createNatRule(String payload, String correlationId) throws CloudException {
        return executePost("/rules/nat", payload, correlationId);
    }

    public String reconcileNetwork(String networkIdentifier, String payload, String correlationId) throws CloudException {
        return executePost("/reconcile/" + networkIdentifier, payload, correlationId);
    }

    private String executeGet(String path, String correlationId) throws CloudException {
        HttpGet get = new HttpGet(composeUrl(path));
        decorateHeaders(get, correlationId);
        return perform(get, correlationId);
    }

    private String executePost(String path, String payload, String correlationId) throws CloudException {
        HttpPost post = new HttpPost(composeUrl(path));
        decorateHeaders(post, correlationId);
        if (payload != null) {
            post.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
            post.setHeader("Content-Type", "application/json");
        }
        return perform(post, correlationId);
    }

    private String executePut(String path, String payload, String correlationId) throws CloudException {
        HttpPut put = new HttpPut(composeUrl(path));
        decorateHeaders(put, correlationId);
        if (payload != null) {
            put.setEntity(new StringEntity(payload, StandardCharsets.UTF_8));
            put.setHeader("Content-Type", "application/json");
        }
        return perform(put, correlationId);
    }

    private String executeDelete(String path, String correlationId) throws CloudException {
        HttpDelete del = new HttpDelete(composeUrl(path));
        decorateHeaders(del, correlationId);
        return perform(del, correlationId);
    }

    private String composeUrl(String path) throws CloudException {
        if (baseUrl == null || baseUrl.isEmpty()) {
            throw new CloudException("VNF Broker URL not configured (vnf.broker.url)");
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return baseUrl + path;
    }

    private void decorateHeaders(org.apache.http.client.methods.HttpRequestBase request, String correlationId) {
        request.setHeader("X-Correlation-ID", correlationId);
        switch (authType == null ? "none" : authType.toLowerCase()) {
            case "bearer":
            case "jwt":
                if (authToken != null && !authToken.isEmpty()) {
                    request.setHeader("Authorization", "Bearer " + authToken);
                }
                break;
            case "basic":
                if (authUser != null && authPass != null && !authUser.isEmpty()) {
                    String basic = java.util.Base64.getEncoder().encodeToString((authUser + ":" + authPass).getBytes(StandardCharsets.UTF_8));
                    request.setHeader("Authorization", "Basic " + basic);
                }
                break;
            default:
                // no auth
        }
    }

    private String perform(org.apache.http.client.methods.HttpRequestBase request, String correlationId) throws CloudException {
        int attempt = 0;
        int delay = initialDelayMs;
        while (true) {
            attempt++;
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int status = response.getStatusLine().getStatusCode();
                String body = response.getEntity() != null ? EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8) : "";
                if (status >= 200 && status < 300) {
                    LOGGER.info("Broker call success path=" + request.getURI() + " status=" + status + " corrId=" + correlationId);
                    return body;
                }
                LOGGER.warn("Broker call failed path=" + request.getURI() + " status=" + status + " attempt=" + attempt + " corrId=" + correlationId);
                if (attempt > maxRetries || status < 500) { // don't retry client errors
                    throw new CloudException("Broker call failed (status=" + status + ") body=" + body);
                }
            } catch (IOException ioe) {
                LOGGER.warn("Broker IO error attempt=" + attempt + " corrId=" + correlationId + " msg=" + ioe.getMessage());
                if (attempt > maxRetries) {
                    throw new CloudException("Broker call IO failure: " + ioe.getMessage(), ioe);
                }
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new CloudException("Interrupted during broker retry", ie);
            }
            delay = Math.min(delay * 2, maxDelayMs);
        }
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}
