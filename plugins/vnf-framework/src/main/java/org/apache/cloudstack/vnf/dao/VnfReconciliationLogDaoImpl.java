// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.cloudstack.vnf.dao;

import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.vnf.entity.VnfReconciliationLogVO;
import org.apache.cloudstack.vnf.entity.VnfReconciliationLogVO.ReconciliationStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class VnfReconciliationLogDaoImpl extends GenericDaoBase<VnfReconciliationLogVO, Long> implements VnfReconciliationLogDao {

    private SearchBuilder<VnfReconciliationLogVO> networkSearch;
    private SearchBuilder<VnfReconciliationLogVO> statusSearch;
    private SearchBuilder<VnfReconciliationLogVO> driftSearch;
    private SearchBuilder<VnfReconciliationLogVO> applianceSearch;

    @PostConstruct
    public void init() {
        networkSearch = createSearchBuilder();
        networkSearch.and("networkId", networkSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        networkSearch.done();

        statusSearch = createSearchBuilder();
        statusSearch.and("status", statusSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        statusSearch.done();

        driftSearch = createSearchBuilder();
        driftSearch.and("driftDetected", driftSearch.entity().getDriftDetected(), SearchCriteria.Op.EQ);
        driftSearch.done();

        applianceSearch = createSearchBuilder();
        applianceSearch.and("vnfApplianceId", applianceSearch.entity().getVnfApplianceId(), SearchCriteria.Op.EQ);
        applianceSearch.done();
    }

    @Override
    public VnfReconciliationLogVO findLatestByNetworkId(Long networkId) {
        SearchCriteria<VnfReconciliationLogVO> sc = networkSearch.create();
        sc.setParameters("networkId", networkId);
        Filter filter = new Filter(VnfReconciliationLogVO.class, "started", false);
        filter.setLimit(1L);
        List<VnfReconciliationLogVO> results = listBy(sc, filter);
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<VnfReconciliationLogVO> listByNetworkId(Long networkId) {
        SearchCriteria<VnfReconciliationLogVO> sc = networkSearch.create();
        sc.setParameters("networkId", networkId);
        Filter filter = new Filter(VnfReconciliationLogVO.class, "started", false);
        return listBy(sc, filter);
    }

    @Override
    public List<VnfReconciliationLogVO> listByStatus(ReconciliationStatus status) {
        SearchCriteria<VnfReconciliationLogVO> sc = statusSearch.create();
        sc.setParameters("status", status);
        return listBy(sc);
    }

    @Override
    public List<VnfReconciliationLogVO> listWithDrift() {
        SearchCriteria<VnfReconciliationLogVO> sc = driftSearch.create();
        sc.setParameters("driftDetected", true);
        Filter filter = new Filter(VnfReconciliationLogVO.class, "started", false);
        return listBy(sc, filter);
    }

    @Override
    public List<VnfReconciliationLogVO> listByApplianceId(Long vnfApplianceId) {
        SearchCriteria<VnfReconciliationLogVO> sc = applianceSearch.create();
        sc.setParameters("vnfApplianceId", vnfApplianceId);
        Filter filter = new Filter(VnfReconciliationLogVO.class, "started", false);
        return listBy(sc, filter);
    }
}
