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

import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.Configurable;

public class VnfFrameworkConfig implements Configurable {

    public static final ConfigKey<Boolean> VnfFrameworkEnabled = new ConfigKey<>(
            "Advanced",
            Boolean.class,
            "vnf.framework.enabled",
            "true",
            "Enable VNF Framework for managing Virtual Network Functions",
            true
    );

    public static final ConfigKey<Integer> VnfHealthCheckInterval = new ConfigKey<>(
            "Advanced",
            Integer.class,
            "vnf.healthcheck.interval",
            "60",
            "VNF health check interval in seconds",
            true
    );

    public static final ConfigKey<Integer> VnfMaxRetries = new ConfigKey<>(
            "Advanced",
            Integer.class,
            "vnf.max.retries",
            "3",
            "Maximum retries for VNF operations",
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
            VnfHealthCheckInterval,
            VnfMaxRetries
        };
    }
}
