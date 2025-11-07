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

package org.apache.cloudstack.vnf;

import org.apache.cloudstack.framework.config.Configurable;
import org.apache.cloudstack.config.Configuration;
import org.apache.cloudstack.framework.config.ConfigKey;

public class VnfFrameworkConfig implements Configurable {
    
    // Feature Flags
    public static final ConfigKey<Boolean> VnfFrameworkEnabled = new ConfigKey<>(
            "Advanced",
            Boolean.class,
            "vnf.framework.enabled",
            "true",
            "Enable VNF Framework for managing Virtual Network Functions",
            true
    );
    
    public static final ConfigKey<Boolean> VnfReconciliationEnabled = new ConfigKey<>(
            "Advanced",
            Boolean.class,
            "vnf.reconciliation.enabled",
            "true",
            "Enable automatic VNF network reconciliation",
            true
    );
    
    // Broker Connection Settings
    public static final ConfigKey<String> VnfBrokerUrl = new ConfigKey<>(
            "Advanced",
            String.class,
            "vnf.broker.url",
            "",
            "VNF Broker base URL (e.g., http://vnf-broker:8080/api/v1). Required for VNF operations.",
            true
    );
    
    public static final ConfigKey<Integer> VnfBrokerTimeout = new ConfigKey<>(
            "Advanced",
            Integer.class,
            "vnf.broker.timeout",
            "30",
            "VNF Broker HTTP request timeout in seconds",
            true
    );
    
    public static final ConfigKey<Integer> VnfBrokerConnectTimeout = new ConfigKey<>(
            "Advanced",
            Integer.class,
            "vnf.broker.connect.timeout",
            "10",
            "VNF Broker HTTP connection timeout in seconds",
            true
    );
    
    // Retry Settings
    public static final ConfigKey<Integer> VnfMaxRetries = new ConfigKey<>(
            "Advanced",
            Integer.class,
            "vnf.max.retries",
            "3",
            "Maximum retries for VNF operations on transient failures",
            true
    );
    
    public static final ConfigKey<Integer> VnfRetryDelayMs = new ConfigKey<>(
            "Advanced",
            Integer.class,
            "vnf.retry.delay.ms",
            "1000",
            "Initial retry delay in milliseconds (exponential backoff applied)",
            true
    );
    
    public static final ConfigKey<Integer> VnfRetryMaxDelayMs = new ConfigKey<>(
            "Advanced",
            Integer.class,
            "vnf.retry.max.delay.ms",
            "30000",
            "Maximum retry delay in milliseconds",
            true
    );
    
    // Health Check Settings
    public static final ConfigKey<Integer> VnfHealthCheckInterval = new ConfigKey<>(
            "Advanced",
            Integer.class,
            "vnf.healthcheck.interval",
            "60",
            "VNF health check interval in seconds",
            true
    );
    
    public static final ConfigKey<Integer> VnfHealthCheckTimeout = new ConfigKey<>(
            "Advanced",
            Integer.class,
            "vnf.healthcheck.timeout",
            "10",
            "VNF health check timeout in seconds",
            true
    );
    
    // Reconciliation Settings
    public static final ConfigKey<Integer> VnfReconciliationInterval = new ConfigKey<>(
            "Advanced",
            Integer.class,
            "vnf.reconciliation.interval",
            "300",
            "VNF network reconciliation interval in seconds",
            true
    );
    
    public static final ConfigKey<Boolean> VnfReconciliationAutoFix = new ConfigKey<>(
            "Advanced",
            Boolean.class,
            "vnf.reconciliation.autofix",
            "false",
            "Automatically fix detected drift during reconciliation (true) or dry-run only (false)",
            true
    );
    
    // Operation Settings
    public static final ConfigKey<Integer> VnfOperationExpiry = new ConfigKey<>(
            "Advanced",
            Integer.class,
            "vnf.operation.expiry",
            "3600",
            "VNF operation expiry time in seconds for pending operations",
            true
    );
    
    public static final ConfigKey<Integer> VnfOperationCleanupAge = new ConfigKey<>(
            "Advanced",
            Integer.class,
            "vnf.operation.cleanup.age",
            "2592000",
            "Age in seconds before completed operations are cleaned up (default: 30 days)",
            true
    );
    
    // Dictionary Settings
    public static final ConfigKey<Integer> VnfDictionaryMaxSize = new ConfigKey<>(
            "Advanced",
            Integer.class,
            "vnf.dictionary.max.size",
            "1048576",
            "Maximum VNF dictionary YAML size in bytes (default: 1MB)",
            true
    );
    
    // Authentication Settings
    public static final ConfigKey<String> VnfBrokerAuthType = new ConfigKey<>(
            "Advanced",
            String.class,
            "vnf.broker.auth.type",
            "none",
            "VNF Broker authentication type: none, basic, bearer, jwt",
            true
    );
    
    public static final ConfigKey<String> VnfBrokerAuthToken = new ConfigKey<>(
            "Secure",
            String.class,
            "vnf.broker.auth.token",
            "",
            "VNF Broker authentication token (for bearer/jwt auth)",
            true
    );
    
    public static final ConfigKey<String> VnfBrokerUsername = new ConfigKey<>(
            "Advanced",
            String.class,
            "vnf.broker.username",
            "",
            "VNF Broker username (for basic auth)",
            true
    );
    
    public static final ConfigKey<String> VnfBrokerPassword = new ConfigKey<>(
            "Secure",
            String.class,
            "vnf.broker.password",
            "",
            "VNF Broker password (for basic auth)",
            true
    );
    
    // Audit Settings
    public static final ConfigKey<Boolean> VnfAuditEnabled = new ConfigKey<>(
            "Advanced",
            Boolean.class,
            "vnf.audit.enabled",
            "true",
            "Enable VNF broker call audit logging",
            true
    );
    
    public static final ConfigKey<Boolean> VnfAuditPayloads = new ConfigKey<>(
            "Advanced",
            Boolean.class,
            "vnf.audit.payloads",
            "false",
            "Store full request/response payloads in audit log (may contain sensitive data)",
            true
    );

    @Override
    public String getConfigComponentName() {
        return VnfFrameworkConfig.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[] {
            VnfFrameworkEnabled,
            VnfReconciliationEnabled,
            VnfBrokerUrl,
            VnfBrokerTimeout,
            VnfBrokerConnectTimeout,
            VnfMaxRetries,
            VnfRetryDelayMs,
            VnfRetryMaxDelayMs,
            VnfHealthCheckInterval,
            VnfHealthCheckTimeout,
            VnfReconciliationInterval,
            VnfReconciliationAutoFix,
            VnfOperationExpiry,
            VnfOperationCleanupAge,
            VnfDictionaryMaxSize,
            VnfBrokerAuthType,
            VnfBrokerAuthToken,
            VnfBrokerUsername,
            VnfBrokerPassword,
            VnfAuditEnabled,
            VnfAuditPayloads
        };
    }
}
