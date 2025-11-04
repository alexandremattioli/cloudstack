package org.apache.cloudstack.vnf.dao;

import org.apache.cloudstack.vnf.entity.VnfDeviceVO;
import com.cloud.utils.db.GenericDao;

import java.util.List;

public interface VnfDeviceDao extends GenericDao<VnfDeviceVO, Long> {

    /**
     * Find VNF device by VNF instance ID
     * @param vnfInstanceId The VNF instance ID
     * @return The VNF device if found, null otherwise
     */
    VnfDeviceVO findByVnfInstanceId(Long vnfInstanceId);

    /**
     * List VNF devices by network ID
     * @param networkId The network ID
     * @return List of VNF devices
     */
    List<VnfDeviceVO> listByNetworkId(Long networkId);
}
