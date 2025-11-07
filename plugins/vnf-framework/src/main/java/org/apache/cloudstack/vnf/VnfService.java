package org.apache.cloudstack.vnf;

import com.cloud.exception.CloudException;
import org.apache.cloudstack.vnf.dao.VnfOperationVO;
import java.util.List;

public interface VnfService {
    
    VnfOperationVO findOperationByRuleId(String ruleId) throws CloudException;
    
    List<VnfOperationVO> listOperationsByVnfInstance(Long vnfInstanceId) throws CloudException;
    
    List<VnfOperationVO> listAllOperations(Long startIndex, Long pageSize) throws CloudException;
    
    List<VnfDictionary> listDictionaries(Long accountId) throws CloudException;
    
    VnfDictionary uploadDictionary(String dictionaryData, String vendor, Long accountId) throws CloudException;
    
    VnfConnectivityResult testConnectivity(Long vnfApplianceId) throws CloudException;
    
    VnfReconciliationResult reconcileNetwork(Long networkId) throws CloudException;
}
