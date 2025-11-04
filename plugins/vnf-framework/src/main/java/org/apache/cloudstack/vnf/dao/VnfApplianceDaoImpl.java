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
import org.apache.cloudstack.vnf.entity.VnfApplianceVO;
import org.apache.cloudstack.vnf.entity.VnfApplianceVO.VnfState;
import org.apache.cloudstack.vnf.entity.VnfApplianceVO.HealthStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

@Component
public class VnfApplianceDaoImpl extends GenericDaoBase<VnfApplianceVO, Long> implements VnfApplianceDao {

    private SearchBuilder<VnfApplianceVO> uuidSearch;
    private SearchBuilder<VnfApplianceVO> networkSearch;
    private SearchBuilder<VnfApplianceVO> vmSearch;
    private SearchBuilder<VnfApplianceVO> templateSearch;
    private SearchBuilder<VnfApplianceVO> stateSearch;
    private SearchBuilder<VnfApplianceVO> healthSearch;
    private SearchBuilder<VnfApplianceVO> activeSearch;
    private SearchBuilder<VnfApplianceVO> staleContactSearch;

    @PostConstruct
    public void init() {
        uuidSearch = createSearchBuilder();
        uuidSearch.and("uuid", uuidSearch.entity().getUuid(), SearchCriteria.Op.EQ);
        uuidSearch.done();

        networkSearch = createSearchBuilder();
        networkSearch.and("networkId", networkSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        networkSearch.and("removed", networkSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        networkSearch.done();

        vmSearch = createSearchBuilder();
        vmSearch.and("vmInstanceId", vmSearch.entity().getVmInstanceId(), SearchCriteria.Op.EQ);
        vmSearch.and("removed", vmSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        vmSearch.done();

        templateSearch = createSearchBuilder();
        templateSearch.and("templateId", templateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        templateSearch.and("removed", templateSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        templateSearch.done();

        stateSearch = createSearchBuilder();
        stateSearch.and("state", stateSearch.entity().getState(), SearchCriteria.Op.EQ);
        stateSearch.and("removed", stateSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        stateSearch.done();

        healthSearch = createSearchBuilder();
        healthSearch.and("healthStatus", healthSearch.entity().getHealthStatus(), SearchCriteria.Op.EQ);
        healthSearch.and("removed", healthSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        healthSearch.done();

        activeSearch = createSearchBuilder();
        activeSearch.and("removed", activeSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        activeSearch.done();

        staleContactSearch = createSearchBuilder();
        staleContactSearch.and("lastContact", staleContactSearch.entity().getLastContact(), SearchCriteria.Op.LT);
        staleContactSearch.and("state", staleContactSearch.entity().getState(), SearchCriteria.Op.EQ);
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
        SearchCriteria<VnfApplianceVO> sc = networkSearch.create();
        sc.setParameters("networkId", networkId);
        return findOneBy(sc);
    }

    @Override
    public VnfApplianceVO findByVmInstanceId(Long vmInstanceId) {
        SearchCriteria<VnfApplianceVO> sc = vmSearch.create();
        sc.setParameters("vmInstanceId", vmInstanceId);
        return findOneBy(sc);
    }

    @Override
    public List<VnfApplianceVO> listByTemplateId(Long templateId) {
        SearchCriteria<VnfApplianceVO> sc = templateSearch.create();
        sc.setParameters("templateId", templateId);
        return listBy(sc);
    }

    @Override
    public List<VnfApplianceVO> listByState(VnfState state) {
        SearchCriteria<VnfApplianceVO> sc = stateSearch.create();
        sc.setParameters("state", state);
        return listBy(sc);
    }

    @Override
    public List<VnfApplianceVO> listByHealthStatus(HealthStatus healthStatus) {
        SearchCriteria<VnfApplianceVO> sc = healthSearch.create();
        sc.setParameters("healthStatus", healthStatus);
        return listBy(sc);
    }

    @Override
    public List<VnfApplianceVO> listActive() {
        SearchCriteria<VnfApplianceVO> sc = activeSearch.create();
        Filter filter = new Filter(VnfApplianceVO.class, "created", false);
        return listBy(sc, filter);
    }

    @Override
    public List<VnfApplianceVO> listStaleContacts(int minutesStale) {
        // Calculate cutoff time
        long cutoffMs = System.currentTimeMillis() - (minutesStale * 60 * 1000L);
        Date cutoff = new Date(cutoffMs);

        SearchCriteria<VnfApplianceVO> sc = staleContactSearch.create();
        sc.setParameters("lastContact", cutoff);
        sc.setParameters("state", VnfState.Running);
        return listBy(sc);
    }
}
