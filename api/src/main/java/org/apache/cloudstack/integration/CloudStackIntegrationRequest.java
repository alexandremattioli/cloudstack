/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cloudstack.integration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class CloudStackIntegrationRequest {

    private final String operation;
    private final String source;
    private final String traceId;
    private final String resourceType;
    private final String resourceId;
    private final Long accountId;
    private final Long domainId;
    private final Long zoneId;
    private final boolean failOnError;
    private final Set<String> targetProviders;
    private final Map<String, Object> parameters;

    protected CloudStackIntegrationRequest(Builder builder) {
        operation = builder.operation;
        source = builder.source;
        traceId = builder.traceId;
        resourceType = builder.resourceType;
        resourceId = builder.resourceId;
        accountId = builder.accountId;
        domainId = builder.domainId;
        zoneId = builder.zoneId;
        failOnError = builder.failOnError;
        targetProviders = Collections.unmodifiableSet(new LinkedHashSet<>(builder.targetProviders));
        parameters = Collections.unmodifiableMap(new LinkedHashMap<>(builder.parameters));
    }

    public static Builder builder(String operation) {
        return new Builder(operation);
    }

    public String getOperation() {
        return operation;
    }

    public String getSource() {
        return source;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Long getDomainId() {
        return domainId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public Set<String> getTargetProviders() {
        return targetProviders;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public boolean hasTargetProviders() {
        return !targetProviders.isEmpty();
    }

    public boolean targetsProvider(String providerName) {
        if (!hasTargetProviders()) {
            return true;
        }
        for (String targetProvider : targetProviders) {
            if (targetProvider.equalsIgnoreCase(providerName)) {
                return true;
            }
        }
        return false;
    }

    public static class Builder {
        private final String operation;
        private String source;
        private String traceId;
        private String resourceType;
        private String resourceId;
        private Long accountId;
        private Long domainId;
        private Long zoneId;
        private boolean failOnError;
        private final Set<String> targetProviders = new LinkedHashSet<>();
        private final Map<String, Object> parameters = new LinkedHashMap<>();

        protected Builder(String operation) {
            if (StringUtils.isBlank(operation)) {
                throw new IllegalArgumentException("Integration operation is required");
            }
            this.operation = operation;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder resourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder resourceId(String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        public Builder accountId(Long accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder domainId(Long domainId) {
            this.domainId = domainId;
            return this;
        }

        public Builder zoneId(Long zoneId) {
            this.zoneId = zoneId;
            return this;
        }

        public Builder failOnError(boolean failOnError) {
            this.failOnError = failOnError;
            return this;
        }

        public Builder targetProvider(String providerName) {
            if (StringUtils.isNotBlank(providerName)) {
                targetProviders.add(providerName);
            }
            return this;
        }

        public Builder targetProviders(Set<String> providerNames) {
            if (providerNames == null) {
                return this;
            }
            for (String providerName : providerNames) {
                targetProvider(providerName);
            }
            return this;
        }

        public Builder parameter(String key, Object value) {
            if (StringUtils.isBlank(key)) {
                throw new IllegalArgumentException("Integration parameter key is required");
            }
            parameters.put(key, value);
            return this;
        }

        public Builder parameters(Map<String, Object> params) {
            if (params != null) {
                parameters.putAll(params);
            }
            return this;
        }

        public CloudStackIntegrationRequest build() {
            return new CloudStackIntegrationRequest(this);
        }
    }
}
