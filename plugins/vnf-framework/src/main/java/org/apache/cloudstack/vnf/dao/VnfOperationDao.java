package org.apache.cloudstack.vnf.dao;

import org.apache.cloudstack.vnf.entity.VnfOperationVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface VnfOperationDao extends GenericDao<VnfOperationVO, Long> {

    /**
     * Find operation by idempotency hash
     * @param opHash The operation hash
     * @return The operation if found, null otherwise
     */
    VnfOperationVO findByOpHash(String opHash);

    /**
     * Find operation by rule ID
     * @param ruleId The rule ID
     * @return The operation if found, null otherwise
     */
    VnfOperationVO findByRuleId(String ruleId);

    /**
     * List all operations for a VNF instance
     * @param vnfInstanceId The VNF instance ID
     * @return List of operations
     */
    List<VnfOperationVO> listByVnfInstanceId(Long vnfInstanceId);

    /**
     * List operations by state
     * @param state The operation state
     * @return List of operations
     */
    List<VnfOperationVO> listByState(String state);

    /**
     * List pending operations for a VNF instance
     * @param vnfInstanceId The VNF instance ID
     * @return List of pending operations
     */
    List<VnfOperationVO> listPendingByVnfInstanceId(Long vnfInstanceId);
}
