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
import org.apache.cloudstack.vnf.entity.VnfTemplateVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VnfTemplateDaoImpl extends GenericDaoBase<VnfTemplateVO, Long> implements VnfTemplateDao {

    private final SearchBuilder<VnfTemplateVO> AccountSearch;
    private final SearchBuilder<VnfTemplateVO> NameSearch;

    public VnfTemplateDaoImpl() {
        AccountSearch = createSearchBuilder();
        AccountSearch.and("accountId", AccountSearch.entity().getAccountId(), SearchCriteria.Op.EQ);
        AccountSearch.and("state", AccountSearch.entity().getState(), SearchCriteria.Op.NEQ);
        AccountSearch.done();

        NameSearch = createSearchBuilder();
        NameSearch.and("name", NameSearch.entity().getName(), SearchCriteria.Op.EQ);
        NameSearch.done();
    }

    @Override
    public List<VnfTemplateVO> listByAccount(long accountId) {
        SearchCriteria<VnfTemplateVO> sc = AccountSearch.create();
        sc.setParameters("accountId", accountId);
        sc.setParameters("state", VnfTemplateVO.State.Deleted);
        return listBy(sc);
    }

    @Override
    public VnfTemplateVO findByName(String name) {
        SearchCriteria<VnfTemplateVO> sc = NameSearch.create();
        sc.setParameters("name", name);
        return findOneBy(sc);
    }
}
