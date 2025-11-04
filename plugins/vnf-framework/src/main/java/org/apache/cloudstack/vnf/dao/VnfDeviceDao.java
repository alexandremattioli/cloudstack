package org.apache.cloudstack.vnf.dao;

import org.apache.cloudstack.vnf.entity.VnfDeviceVO;
import com.cloud.utils.db.GenericDao;

public interface VnfDeviceDao extends GenericDao<VnfDeviceVO, Long> {

    /**
     * Find VNF device by VNF instance ID
     * @param vnfInstanceId The VNF instance ID
     * @return The VNF device if found, null otherwise
     */
    VnfDeviceVO findByVnfInstanceId(Long vnfInstanceId);
}
