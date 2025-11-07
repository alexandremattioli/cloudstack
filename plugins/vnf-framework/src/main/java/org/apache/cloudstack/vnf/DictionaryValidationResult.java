package org.apache.cloudstack.vnf;

import java.util.ArrayList;

import java.util.List;

public class DictionaryValidationResult {
    private boolean valid;
    private List<String> errors;
    private List<String> warnings;
    private List<String> servicesFound;
    public DictionaryValidationResult() {
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.servicesFound = new ArrayList<>();
        this.valid = true;
    }
    public boolean isValid() { return valid && errors.isEmpty(); }
    public void setValid(boolean valid) { this.valid = valid; }
    public List<String> getErrors() { return errors; }
    public void addError(String error) {
        errors.add(error);
        valid = false;
    }
    public List<String> getWarnings() { return warnings; }
    public void addWarning(String warning) { warnings.add(warning); }
    public List<String> getServicesFound() { return servicesFound; }
    public void addService(String service) { servicesFound.add(service); }
}
// =====================================================
// 6. HELPER CLASSES
// =====================================================
/**
 * Template rendering context with placeholders
 */