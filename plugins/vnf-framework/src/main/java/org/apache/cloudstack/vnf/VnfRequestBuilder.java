package org.apache.cloudstack.vnf;

public interface VnfRequestBuilder {
    /**
     * Build a request for a firewall rule operation
     */
    VnfRequest buildFirewallRequest(
        VnfDictionary dictionary,
        FirewallRuleOperation operation,
        FirewallRule rule
    ) throws RequestBuildException;
    /**
     * Build a request for NAT operation
     */
    VnfRequest buildNatRequest(
        VnfDictionary dictionary,
        NatOperation operation,
        PortForwardingRule rule
    ) throws RequestBuildException;
    /**
     * Build a request for load balancer operation
     */
    VnfRequest buildLoadBalancerRequest(
        VnfDictionary dictionary,
        LoadBalancerOperation operation,
        LoadBalancingRule rule
    ) throws RequestBuildException;
    /**
     * Build a list request to query device state
     */
    VnfRequest buildListRequest(
        VnfDictionary dictionary,
        String serviceName
    ) throws RequestBuildException;
}
/**
 * Broker client interface for communicating with VNF via VR or direct
 */