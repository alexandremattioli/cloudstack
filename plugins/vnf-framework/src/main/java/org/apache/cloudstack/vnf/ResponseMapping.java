package org.apache.cloudstack.vnf;

public class ResponseMapping {
    private int successCode = 200;
    private String idPath;           // JSONPath to extract ID
    private String listPath;         // JSONPath to list of items
    private Map<String, String> itemPaths;  // Field mappings
    public int getSuccessCode() { return successCode; }
    public void setSuccessCode(int code) { this.successCode = code; }
    public String getIdPath() { return idPath; }
    public void setIdPath(String path) { this.idPath = path; }
}
/**
 * Represents a VNF appliance instance
 */