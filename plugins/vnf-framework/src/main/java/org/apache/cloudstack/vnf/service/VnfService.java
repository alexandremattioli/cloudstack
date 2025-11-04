package org.apache.cloudstack.vnf.service;

import org.apache.cloudstack.vnf.api.command.CreateVnfFirewallRuleCmd;
import org.apache.cloudstack.vnf.api.response.VnfFirewallRuleResponse;

/**
 * Service interface for VNF operations
 */
public interface VnfService {

    /**
     * Create a firewall rule on a VNF instance
     * @param cmd The command containing rule parameters
     * @return VnfFirewallRuleResponse with operation result
     * @throws Exception if operation fails
     */
    VnfFirewallRuleResponse createFirewallRule(CreateVnfFirewallRuleCmd cmd) throws Exception;

    /**
     * Delete a firewall rule from a VNF instance
     * @param ruleId The rule ID to delete
     * @return true if successful
     * @throws Exception if operation fails
     */
    boolean deleteFirewallRule(String ruleId) throws Exception;
}
