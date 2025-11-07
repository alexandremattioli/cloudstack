package org.apache.cloudstack.vnf;

import java.util.List;

import com.cloud.exception.CloudException;
public class DictionaryParseException extends CloudException {
    private List<String> errors;
    public DictionaryParseException(String message) {
        super(message);
    }
    public DictionaryParseException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }
    public List<String> getErrors() { return errors; }
}