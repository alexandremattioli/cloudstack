package org.apache.cloudstack.vnf;

import java.util.Map;

public class ResponseMapping {
    private int successCode = 200;
    private String idPath;           // JSONPath to extract ID
    private String listPath;         // JSONPath to list of items
    private Map<String, String> itemPaths;  // Field mappings
    public int getSuccessCode() { return successCode; }
    public void setSuccessCode(int code) { this.successCode = code; }
    public String getIdPath() { return idPath; }
    public void setIdPath(String path) { this.idPath = path; }
    public String getListPath() { return listPath; }
    public void setListPath(String listPath) { this.listPath = listPath; }
    public Map<String, String> getItemPaths() { return itemPaths; }
    public void setItemPaths(Map<String, String> itemPaths) { this.itemPaths = itemPaths; }
}
/**
 * Represents a VNF appliance instance
 */