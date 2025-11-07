package org.apache.cloudstack.vnf;

import com.cloud.exception.CloudException;
import org.apache.cloudstack.vnf.dao.VnfOperationVO;
import org.apache.cloudstack.vnf.dao.VnfOperationVO.State;
import org.apache.cloudstack.vnf.api.command.*;
import java.util.List;

public interface VnfService {
    
    // Operation queries
    VnfOperationVO findOperationByRuleId(String ruleId) throws CloudException;
    List<VnfOperationVO> listOperationsByVnfInstance(Long vnfInstanceId) throws CloudException;
    List<VnfOperationVO> listOperationsByVnfInstanceAndState(Long vnfInstanceId, State state) throws CloudException;
    List<VnfOperationVO> listOperationsByState(State state) throws CloudException;
    List<VnfOperationVO> listAllOperations(Long startIndex, Long pageSize) throws CloudException;
    
    // Dictionary management
    List<VnfDictionary> listDictionaries(Long accountId) throws CloudException;
    List<VnfDictionary> listVnfDictionaries(ListVnfDictionariesCmd cmd) throws CloudException;
    VnfDictionary uploadDictionary(String dictionaryData, String vendor, Long accountId) throws CloudException;
    VnfDictionary uploadVnfDictionary(UploadVnfDictionaryCmd cmd) throws CloudException;
    
    // Firewall rules
    String createVnfFirewallRule(CreateVnfFirewallRuleCmd cmd) throws CloudException;
    String deleteVnfFirewallRule(DeleteVnfFirewallRuleCmd cmd) throws CloudException;
    String updateVnfFirewallRule(UpdateVnfFirewallRuleCmd cmd) throws CloudException;
    
    // NAT rules
    String createVnfNATRule(CreateVnfNATRuleCmd cmd) throws CloudException;
    
    // Network operations
    VnfReconciliationResult reconcileVnfNetwork(ReconcileVnfNetworkCmd cmd) throws CloudException;
    VnfReconciliationResult reconcileNetwork(Long networkId) throws CloudException;
    
    // Connectivity testing
    VnfConnectivityResult testConnectivity(Long vnfApplianceId) throws CloudException;
    VnfConnectivityResult testVnfConnectivity(TestVnfConnectivityCmd cmd) throws CloudException;
}
