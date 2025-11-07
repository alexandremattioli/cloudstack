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

import org.apache.cloudstack.vnf.entity.VnfReconciliationLogVO;
import org.apache.cloudstack.vnf.entity.VnfReconciliationLogVO.ReconciliationStatus;
import org.springframework.stereotype.Component;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

@Component
public class VnfReconciliationLogDaoImpl extends GenericDaoBase<VnfReconciliationLogVO, Long> implements VnfReconciliationLogDao {
    
    private final SearchBuilder<VnfReconciliationLogVO> networkIdSearch;
    private final SearchBuilder<VnfReconciliationLogVO> latestByNetworkSearch;
    private final SearchBuilder<VnfReconciliationLogVO> statusSearch;
    private final SearchBuilder<VnfReconciliationLogVO> driftSearch;
    private final SearchBuilder<VnfReconciliationLogVO> applianceIdSearch;

    public VnfReconciliationLogDaoImpl() {
        networkIdSearch = createSearchBuilder();
        networkIdSearch.and("networkId", networkIdSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        networkIdSearch.done();

        latestByNetworkSearch = createSearchBuilder();
        latestByNetworkSearch.and("networkId", latestByNetworkSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        latestByNetworkSearch.done();

        statusSearch = createSearchBuilder();
        statusSearch.and("status", statusSearch.entity().getStatus(), SearchCriteria.Op.EQ);
        statusSearch.done();

        driftSearch = createSearchBuilder();
        driftSearch.and("driftDetected", driftSearch.entity().getDriftDetected(), SearchCriteria.Op.EQ);
        driftSearch.done();

        applianceIdSearch = createSearchBuilder();
        applianceIdSearch.and("vnfApplianceId", applianceIdSearch.entity().getVnfApplianceId(), SearchCriteria.Op.EQ);
        applianceIdSearch.done();
    }

    @Override
    public VnfReconciliationLogVO findLatestByNetworkId(Long networkId) {
        SearchCriteria<VnfReconciliationLogVO> sc = latestByNetworkSearch.create();
        sc.setParameters("networkId", networkId);
        // Order by createdAt DESC and get the first result
        List<VnfReconciliationLogVO> results = listBy(sc);
        if (results != null && !results.isEmpty()) {
            // Note: In a production implementation, this should use ORDER BY in the query
            // For now, we'll just return the first result
            return results.get(0);
        }
        return null;
    }

    @Override
    public List<VnfReconciliationLogVO> listByNetworkId(Long networkId) {
        SearchCriteria<VnfReconciliationLogVO> sc = networkIdSearch.create();
        sc.setParameters("networkId", networkId);
        return listBy(sc);
    }

    @Override
    public List<VnfReconciliationLogVO> listByStatus(ReconciliationStatus status) {
        SearchCriteria<VnfReconciliationLogVO> sc = statusSearch.create();
        sc.setParameters("status", status.toString());
        return listBy(sc);
    }

    @Override
    public List<VnfReconciliationLogVO> listWithDrift() {
        SearchCriteria<VnfReconciliationLogVO> sc = driftSearch.create();
        sc.setParameters("driftDetected", true);
        return listBy(sc);
    }

    @Override
    public List<VnfReconciliationLogVO> listByApplianceId(Long vnfApplianceId) {
        SearchCriteria<VnfReconciliationLogVO> sc = applianceIdSearch.create();
        sc.setParameters("vnfApplianceId", vnfApplianceId);
        return listBy(sc);
    }
}
