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

import org.apache.cloudstack.vnf.entity.VnfApplianceVO;
import org.apache.cloudstack.vnf.entity.VnfApplianceVO.VnfState;
import org.apache.cloudstack.vnf.entity.VnfApplianceVO.HealthStatus;
import org.springframework.stereotype.Component;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;
import java.util.Date;

@Component
public class VnfApplianceDaoImpl extends GenericDaoBase<VnfApplianceVO, Long> implements VnfApplianceDao {
    
    private final SearchBuilder<VnfApplianceVO> uuidSearch;
    private final SearchBuilder<VnfApplianceVO> networkIdSearch;
    private final SearchBuilder<VnfApplianceVO> vmInstanceIdSearch;
    private final SearchBuilder<VnfApplianceVO> templateIdSearch;
    private final SearchBuilder<VnfApplianceVO> stateSearch;
    private final SearchBuilder<VnfApplianceVO> healthStatusSearch;
    private final SearchBuilder<VnfApplianceVO> activeSearch;
    private final SearchBuilder<VnfApplianceVO> staleContactSearch;

    public VnfApplianceDaoImpl() {
        uuidSearch = createSearchBuilder();
        uuidSearch.and("uuid", uuidSearch.entity().getUuid(), SearchCriteria.Op.EQ);
        uuidSearch.done();

        networkIdSearch = createSearchBuilder();
        networkIdSearch.and("networkId", networkIdSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        networkIdSearch.and("removed", networkIdSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        networkIdSearch.done();

        vmInstanceIdSearch = createSearchBuilder();
        vmInstanceIdSearch.and("vmInstanceId", vmInstanceIdSearch.entity().getVmInstanceId(), SearchCriteria.Op.EQ);
        vmInstanceIdSearch.and("removed", vmInstanceIdSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        vmInstanceIdSearch.done();

        templateIdSearch = createSearchBuilder();
        templateIdSearch.and("templateId", templateIdSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        templateIdSearch.and("removed", templateIdSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        templateIdSearch.done();

        stateSearch = createSearchBuilder();
        stateSearch.and("state", stateSearch.entity().getState(), SearchCriteria.Op.EQ);
        stateSearch.and("removed", stateSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        stateSearch.done();

        healthStatusSearch = createSearchBuilder();
        healthStatusSearch.and("healthStatus", healthStatusSearch.entity().getHealthStatus(), SearchCriteria.Op.EQ);
        healthStatusSearch.and("removed", healthStatusSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        healthStatusSearch.done();

        activeSearch = createSearchBuilder();
        activeSearch.and("removed", activeSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        activeSearch.done();

        staleContactSearch = createSearchBuilder();
        staleContactSearch.and("lastContact", staleContactSearch.entity().getLastContact(), SearchCriteria.Op.LT);
        staleContactSearch.and("removed", staleContactSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        staleContactSearch.done();
    }

    @Override
    public VnfApplianceVO findByUuid(String uuid) {
        SearchCriteria<VnfApplianceVO> sc = uuidSearch.create();
        sc.setParameters("uuid", uuid);
        return findOneBy(sc);
    }

    @Override
    public VnfApplianceVO findByNetworkId(Long networkId) {
        SearchCriteria<VnfApplianceVO> sc = networkIdSearch.create();
        sc.setParameters("networkId", networkId);
        return findOneBy(sc);
    }

    @Override
    public VnfApplianceVO findByVmInstanceId(Long vmInstanceId) {
        SearchCriteria<VnfApplianceVO> sc = vmInstanceIdSearch.create();
        sc.setParameters("vmInstanceId", vmInstanceId);
        return findOneBy(sc);
    }

    @Override
    public List<VnfApplianceVO> listByTemplateId(Long templateId) {
        SearchCriteria<VnfApplianceVO> sc = templateIdSearch.create();
        sc.setParameters("templateId", templateId);
        return listBy(sc);
    }

    @Override
    public List<VnfApplianceVO> listByState(VnfState state) {
        SearchCriteria<VnfApplianceVO> sc = stateSearch.create();
        sc.setParameters("state", state.toString());
        return listBy(sc);
    }

    @Override
    public List<VnfApplianceVO> listByHealthStatus(HealthStatus healthStatus) {
        SearchCriteria<VnfApplianceVO> sc = healthStatusSearch.create();
        sc.setParameters("healthStatus", healthStatus.toString());
        return listBy(sc);
    }

    @Override
    public List<VnfApplianceVO> listActive() {
        SearchCriteria<VnfApplianceVO> sc = activeSearch.create();
        return listBy(sc);
    }

    @Override
    public List<VnfApplianceVO> listStaleContacts(int minutesStale) {
        Date staleThreshold = new Date(System.currentTimeMillis() - (minutesStale * 60 * 1000));
        SearchCriteria<VnfApplianceVO> sc = staleContactSearch.create();
        sc.setParameters("lastContact", staleThreshold);
        return listBy(sc);
    }
}
