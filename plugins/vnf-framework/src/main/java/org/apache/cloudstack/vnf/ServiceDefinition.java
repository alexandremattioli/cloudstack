package org.apache.cloudstack.vnf;

public class ServiceDefinition {
    private String name;
    private Map<String, OperationDefinition> operations;
    public OperationDefinition getOperation(String opName) {
        return operations.get(opName);
    }
    public void setOperations(Map<String, OperationDefinition> ops) {
        this.operations = ops;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Map<String, OperationDefinition> getOperations() { return operations; }
}