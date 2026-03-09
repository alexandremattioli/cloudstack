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
import java.util.Map;

public class CloudStackIntegrationResult {

    private final String providerName;
    private final boolean successful;
    private final boolean skipped;
    private final String message;
    private final long startedAt;
    private final long finishedAt;
    private final Map<String, Object> details;

    protected CloudStackIntegrationResult(String providerName, boolean successful, boolean skipped, String message,
            long startedAt, long finishedAt, Map<String, Object> details) {
        this.providerName = providerName;
        this.successful = successful;
        this.skipped = skipped;
        this.message = message;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.details = Collections.unmodifiableMap(new LinkedHashMap<>(details));
    }

    public static CloudStackIntegrationResult success(String providerName, String message) {
        return success(providerName, message, Collections.emptyMap());
    }

    public static CloudStackIntegrationResult success(String providerName, String message, Map<String, Object> details) {
        long now = System.currentTimeMillis();
        return new CloudStackIntegrationResult(providerName, true, false, message, now, now, details);
    }

    public static CloudStackIntegrationResult failure(String providerName, String message) {
        return failure(providerName, message, Collections.emptyMap());
    }

    public static CloudStackIntegrationResult failure(String providerName, String message, Map<String, Object> details) {
        long now = System.currentTimeMillis();
        return new CloudStackIntegrationResult(providerName, false, false, message, now, now, details);
    }

    public static CloudStackIntegrationResult skipped(String providerName, String message) {
        long now = System.currentTimeMillis();
        return new CloudStackIntegrationResult(providerName, false, true, message, now, now, Collections.emptyMap());
    }

    public static Builder builder(String providerName) {
        return new Builder(providerName);
    }

    public String getProviderName() {
        return providerName;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public boolean isSkipped() {
        return skipped;
    }

    public boolean isFailure() {
        return !successful && !skipped;
    }

    public String getMessage() {
        return message;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getFinishedAt() {
        return finishedAt;
    }

    public long getDuration() {
        return finishedAt - startedAt;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public static class Builder {
        private final String providerName;
        private boolean successful;
        private boolean skipped;
        private String message;
        private long startedAt = System.currentTimeMillis();
        private long finishedAt = startedAt;
        private final Map<String, Object> details = new LinkedHashMap<>();

        protected Builder(String providerName) {
            this.providerName = providerName;
        }

        public Builder success() {
            successful = true;
            skipped = false;
            return this;
        }

        public Builder failure() {
            successful = false;
            skipped = false;
            return this;
        }

        public Builder skipped() {
            successful = false;
            skipped = true;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder startedAt(long startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder finishedAt(long finishedAt) {
            this.finishedAt = finishedAt;
            return this;
        }

        public Builder detail(String key, Object value) {
            details.put(key, value);
            return this;
        }

        public Builder details(Map<String, Object> values) {
            if (values != null) {
                details.putAll(values);
            }
            return this;
        }

        public CloudStackIntegrationResult build() {
            return new CloudStackIntegrationResult(providerName, successful, skipped, message, startedAt, finishedAt, details);
        }
    }
}
