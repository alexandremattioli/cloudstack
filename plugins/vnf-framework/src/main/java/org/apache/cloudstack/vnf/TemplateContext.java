package org.apache.cloudstack.vnf;

import java.util.Map;
import java.util.HashMap;

public class TemplateContext {
    private Map<String, Object> variables;
    public TemplateContext() {
        this.variables = new HashMap<>();
    }
    public void set(String key, Object value) {
        variables.put(key, value);
    }
    public Object get(String key) {
        return variables.get(key);
    }
    public Map<String, Object> getAll() {
        return variables;
    }
}
/**
 * JWT token generator for broker authorization
 */