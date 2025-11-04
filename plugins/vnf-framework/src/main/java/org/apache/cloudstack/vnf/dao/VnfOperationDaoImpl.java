package org.apache.cloudstack.vnf.dao;

import org.apache.cloudstack.vnf.entity.VnfOperationVO;
import org.springframework.stereotype.Component;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

@Component
public class VnfOperationDaoImpl extends GenericDaoBase<VnfOperationVO, Long> implements VnfOperationDao {

    private final SearchBuilder<VnfOperationVO> opHashSearch;
    private final SearchBuilder<VnfOperationVO> ruleIdSearch;
    private final SearchBuilder<VnfOperationVO> vnfInstanceIdSearch;
    private final SearchBuilder<VnfOperationVO> stateSearch;
    private final SearchBuilder<VnfOperationVO> pendingByVnfInstanceSearch;

    public VnfOperationDaoImpl() {
        opHashSearch = createSearchBuilder();
        opHashSearch.and("opHash", opHashSearch.entity().getOpHash(), SearchCriteria.Op.EQ);
        opHashSearch.and("removed", opHashSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        opHashSearch.done();

        ruleIdSearch = createSearchBuilder();
        ruleIdSearch.and("ruleId", ruleIdSearch.entity().getRuleId(), SearchCriteria.Op.EQ);
        ruleIdSearch.and("removed", ruleIdSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        ruleIdSearch.done();

        vnfInstanceIdSearch = createSearchBuilder();
        vnfInstanceIdSearch.and("vnfInstanceId", vnfInstanceIdSearch.entity().getVnfInstanceId(), SearchCriteria.Op.EQ);
        vnfInstanceIdSearch.and("removed", vnfInstanceIdSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        vnfInstanceIdSearch.done();

        stateSearch = createSearchBuilder();
        stateSearch.and("state", stateSearch.entity().getState(), SearchCriteria.Op.EQ);
        stateSearch.and("removed", stateSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        stateSearch.done();

        pendingByVnfInstanceSearch = createSearchBuilder();
        pendingByVnfInstanceSearch.and("vnfInstanceId", pendingByVnfInstanceSearch.entity().getVnfInstanceId(), SearchCriteria.Op.EQ);
        pendingByVnfInstanceSearch.and("state", pendingByVnfInstanceSearch.entity().getState(), SearchCriteria.Op.IN);
        pendingByVnfInstanceSearch.and("removed", pendingByVnfInstanceSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        pendingByVnfInstanceSearch.done();
    }

    @Override
    public VnfOperationVO findByOpHash(String opHash) {
        SearchCriteria<VnfOperationVO> sc = opHashSearch.create();
        sc.setParameters("opHash", opHash);
        return findOneBy(sc);
    }

    @Override
    public VnfOperationVO findByRuleId(String ruleId) {
        SearchCriteria<VnfOperationVO> sc = ruleIdSearch.create();
        sc.setParameters("ruleId", ruleId);
        return findOneBy(sc);
    }

    @Override
    public List<VnfOperationVO> listByVnfInstanceId(Long vnfInstanceId) {
        SearchCriteria<VnfOperationVO> sc = vnfInstanceIdSearch.create();
        sc.setParameters("vnfInstanceId", vnfInstanceId);
        return listBy(sc);
    }

    @Override
    public List<VnfOperationVO> listByState(String state) {
        SearchCriteria<VnfOperationVO> sc = stateSearch.create();
        sc.setParameters("state", state);
        return listBy(sc);
    }

    @Override
    public List<VnfOperationVO> listPendingByVnfInstanceId(Long vnfInstanceId) {
        SearchCriteria<VnfOperationVO> sc = pendingByVnfInstanceSearch.create();
        sc.setParameters("vnfInstanceId", vnfInstanceId);
        sc.setParameters("state", VnfOperationVO.State.Pending.toString(), VnfOperationVO.State.InProgress.toString());
        return listBy(sc);
    }
}
