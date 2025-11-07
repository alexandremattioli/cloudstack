package org.apache.cloudstack.vnf;

import com.cloud.exception.CloudException;

public class RequestBuildException extends CloudException {
    public RequestBuildException(String message) {
        super(message);
    }
    public RequestBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}