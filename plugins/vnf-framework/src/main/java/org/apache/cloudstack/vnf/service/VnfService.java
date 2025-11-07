package org.apache.cloudstack.vnf.service;

import com.cloud.exception.CloudException;
import org.apache.cloudstack.vnf.dao.VnfOperationVO;
import org.apache.cloudstack.vnf.dao.VnfOperationVO.State;
import org.apache.cloudstack.vnf.*;
import java.util.List;

// Forward declarations to avoid circular dependencies
// Command classes are in api.command package and will be compiled separately

public interface VnfService {
    
    // Operation queries
    VnfOperationVO findOperationByRuleId(String ruleId) throws CloudException;
    List<VnfOperationVO> listOperationsByVnfInstance(Long vnfInstanceId) throws CloudException;
    List<VnfOperationVO> listOperationsByVnfInstanceAndState(Long vnfInstanceId, State state) throws CloudException;
    List<VnfOperationVO> listOperationsByState(State state) throws CloudException;
    List<VnfOperationVO> listAllOperations(Long startIndex, Long pageSize) throws CloudException;
    
    // Dictionary management
    List<VnfDictionary> listDictionaries(Long accountId) throws CloudException;
    VnfDictionary uploadDictionary(String dictionaryData, String vendor, Long accountId) throws CloudException;
    
    // Command-based methods (use Object to avoid circular dependency)
    String uploadVnfDictionary(Object cmd) throws CloudException;
    List<VnfDictionary> listVnfDictionaries(Object cmd) throws CloudException;
    
    // Firewall rule methods
    String createFirewallRule(Object cmd) throws CloudException;
    String deleteVnfFirewallRule(Object cmd) throws CloudException;
    String updateVnfFirewallRule(Object cmd) throws CloudException;
    
    // NAT rule methods
    String createVnfNATRule(Object cmd) throws CloudException;
    
    // Network operations
    VnfReconciliationResult reconcileVnfNetwork(Object cmd) throws CloudException;
    VnfReconciliationResult reconcileNetwork(Long networkId) throws CloudException;
    
    // Connectivity testing
    VnfConnectivityResult testConnectivity(Long vnfApplianceId) throws CloudException;
    VnfConnectivityResult testVnfConnectivity(Object cmd) throws CloudException;
}
