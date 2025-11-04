package org.apache.cloudstack.vnf.dao;

import org.apache.cloudstack.vnf.entity.VnfDeviceVO;
import org.springframework.stereotype.Component;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

@Component
public class VnfDeviceDaoImpl extends GenericDaoBase<VnfDeviceVO, Long> implements VnfDeviceDao {

    private final SearchBuilder<VnfDeviceVO> vnfInstanceIdSearch;

    public VnfDeviceDaoImpl() {
        vnfInstanceIdSearch = createSearchBuilder();
        vnfInstanceIdSearch.and("vnfInstanceId", vnfInstanceIdSearch.entity().getVnfInstanceId(), SearchCriteria.Op.EQ);
        vnfInstanceIdSearch.and("removed", vnfInstanceIdSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        vnfInstanceIdSearch.done();
    }

    @Override
    public VnfDeviceVO findByVnfInstanceId(Long vnfInstanceId) {
        SearchCriteria<VnfDeviceVO> sc = vnfInstanceIdSearch.create();
        sc.setParameters("vnfInstanceId", vnfInstanceId);
        return findOneBy(sc);
    }
}
