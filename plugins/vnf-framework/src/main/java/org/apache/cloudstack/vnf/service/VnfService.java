package org.apache.cloudstack.vnf.service;

import com.cloud.exception.CloudException;
import org.apache.cloudstack.vnf.dao.VnfOperationVO;
import org.apache.cloudstack.vnf.dao.VnfOperationVO.State;
import org.apache.cloudstack.vnf.*;
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
    VnfDictionary uploadDictionary(String dictionaryData, String vendor, Long accountId) throws CloudException;
    
    // Connectivity testing
    VnfConnectivityResult testConnectivity(Long vnfApplianceId) throws CloudException;
    
    // Network reconciliation
    VnfReconciliationResult reconcileNetwork(Long networkId) throws CloudException;
}
