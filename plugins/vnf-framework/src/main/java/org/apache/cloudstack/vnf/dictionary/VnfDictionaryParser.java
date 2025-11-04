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
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class VnfDictionaryParser {
    
    private static final Logger s_logger = Logger.getLogger(VnfDictionaryParser.class);
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseDictionary(String yamlContent) throws CloudException {
        try {
            Yaml yaml = new Yaml();
            Object parsed = yaml.load(yamlContent);
            
            if (!(parsed instanceof Map)) {
                throw new CloudException("Invalid YAML: root must be a map");
            }
            
            return (Map<String, Object>) parsed;
            
        } catch (Exception e) {
            s_logger.error("Failed to parse VNF dictionary", e);
            throw new CloudException("Failed to parse dictionary: " + e.getMessage(), e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> getOperation(Map<String, Object> dictionary, String serviceName, String operationName) throws CloudException {
        try {
            Map<String, Object> services = (Map<String, Object>) dictionary.get("services");
            if (services == null) {
                throw new CloudException("No services defined in dictionary");
            }
            
            Map<String, Object> service = (Map<String, Object>) services.get(serviceName);
            if (service == null) {
                throw new CloudException("Service not found: " + serviceName);
            }
            
            Map<String, Object> operation = (Map<String, Object>) service.get(operationName);
            if (operation == null) {
                throw new CloudException("Operation not found: " + serviceName + "." + operationName);
            }
            
            return operation;
            
        } catch (ClassCastException e) {
            throw new CloudException("Invalid dictionary structure", e);
        }
    }
    
    public String renderTemplate(String template, Map<String, Object> variables) {
        if (template == null) {
            return null;
        }
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = variables.get(varName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    @SuppressWarnings("unchecked")
    public HttpRequestSpec buildRequest(Map<String, Object> operation, Map<String, Object> variables) throws CloudException {
        HttpRequestSpec spec = new HttpRequestSpec();
        
        String method = (String) operation.get("method");
        spec.setMethod(method != null ? method : "GET");
        
        String endpoint = (String) operation.get("endpoint");
        if (endpoint == null) {
            throw new CloudException("Operation missing endpoint");
        }
        spec.setEndpoint(renderTemplate(endpoint, variables));
        
        String body = (String) operation.get("body");
        if (body != null) {
            spec.setBody(renderTemplate(body, variables));
        }
        
        Map<String, Object> headers = (Map<String, Object>) operation.get("headers");
        if (headers != null) {
            Map<String, String> renderedHeaders = new HashMap<>();
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                String value = renderTemplate(entry.getValue().toString(), variables);
                renderedHeaders.put(entry.getKey(), value);
            }
            spec.setHeaders(renderedHeaders);
        }
        
        return spec;
    }
    
    @SuppressWarnings("unchecked")
    public Object extractFromResponse(Map<String, Object> response, String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        
        String[] parts = path.split("\\.");
        Object current = response;
        
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }
        
        return current;
    }
    
    public static class HttpRequestSpec {
        private String method;
        private String endpoint;
        private String body;
        private Map<String, String> headers = new HashMap<>();
        
        public String getMethod() {
            return method;
        }
        
        public void setMethod(String method) {
            this.method = method;
        }
        
        public String getEndpoint() {
            return endpoint;
        }
        
        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
        
        public String getBody() {
            return body;
        }
        
        public void setBody(String body) {
            this.body = body;
        }
        
        public Map<String, String> getHeaders() {
            return headers;
        }
        
        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }
    }
}
