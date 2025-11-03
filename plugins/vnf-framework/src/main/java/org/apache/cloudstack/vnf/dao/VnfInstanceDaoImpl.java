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

import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import org.apache.cloudstack.vnf.entity.VnfInstanceVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VnfInstanceDaoImpl extends GenericDaoBase<VnfInstanceVO, Long> implements VnfInstanceDao {

    private final SearchBuilder<VnfInstanceVO> AccountSearch;
    private final SearchBuilder<VnfInstanceVO> VmIdSearch;

    public VnfInstanceDaoImpl() {
        AccountSearch = createSearchBuilder();
        AccountSearch.and("accountId", AccountSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountSearch.and("state", AccountSearch.entity().getState(), SearchCriteria.Op.NEQ);
        AccountSearch.done();

        VmIdSearch = createSearchBuilder();
        VmIdSearch.and("vmInstanceId", VmIdSearch.entity().getVmInstanceId(), SearchCriteria.Op.EQ);
        VmIdSearch.done();
    }

    @Override
    public List<VnfInstanceVO> listByAccount(long accountId) {
        SearchCriteria<VnfInstanceVO> sc = AccountSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("state", VnfInstanceVO.State.Destroyed);
        return listBy(sc);
    }

    @Override
    public VnfInstanceVO findByVmInstanceId(long vmInstanceId) {
        SearchCriteria<VnfInstanceVO> sc = VmIdSearch.create();
        sc.setParameters("vmInstanceId", vmInstanceId);
        return findOneBy(sc);
    }

    @Override
    public List<VnfInstanceVO> listByZoneId(long zoneId) {
        SearchCriteria<VnfInstanceVO> sc = createSearchCriteria();
        sc.addAnd("zoneId", SearchCriteria.Op.EQ, zoneId);
        sc.addAnd("state", SearchCriteria.Op.NEQ, VnfInstanceVO.State.Destroyed);
        return listBy(sc);
    }
}
