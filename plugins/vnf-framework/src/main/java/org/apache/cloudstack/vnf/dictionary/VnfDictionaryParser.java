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

package org.apache.cloudstack.vnf.dictionary;

import com.cloud.exception.CloudException;
import org.apache.cloudstack.vnf.VnfFrameworkConfig;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Parses and validates VNF dictionary YAML content.
 * Minimal schema enforcement for early value delivery.
 */
public class VnfDictionaryParser {
    private final Yaml yaml = new Yaml();

    public ParsedDictionary parse(String yamlContent) throws CloudException {
        if (yamlContent == null || yamlContent.isEmpty()) {
            throw new CloudException("Empty dictionary content");
        }
        int maxSize = VnfFrameworkConfig.VnfDictionaryMaxSize.value();
        if (yamlContent.getBytes(StandardCharsets.UTF_8).length > maxSize) {
            throw new CloudException("Dictionary exceeds max size: " + maxSize + " bytes");
        }
        try {
            Map<String, Object> root = yaml.load(yamlContent);
            if (root == null) {
                throw new CloudException("Dictionary YAML parsed to null root");
            }
            // Required top-level keys
            require(root, "vendor");
            require(root, "product");
            // Optional sections: firewallRules, natRules
            return new ParsedDictionary(
                    stringVal(root.get("vendor")),
                    stringVal(root.get("product")),
                    stringVal(root.getOrDefault("version", "1.0")),
                    root
            );
        } catch (YAMLException ye) {
            throw new CloudException("Invalid YAML: " + ye.getMessage(), ye);
        }
    }

    private void require(Map<String, Object> root, String key) throws CloudException {
        if (!root.containsKey(key)) {
            throw new CloudException("Missing required key: " + key);
        }
    }

    private String stringVal(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public static class ParsedDictionary {
        public final String vendor;
        public final String product;
        public final String version;
        public final Map<String, Object> raw;

        public ParsedDictionary(String vendor, String product, String version, Map<String, Object> raw) {
            this.vendor = vendor;
            this.product = product;
            this.version = version;
            this.raw = raw;
        }
    }
}
