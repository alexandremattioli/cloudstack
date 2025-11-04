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
import org.apache.cloudstack.vnf.entity.VnfDictionaryVO;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class VnfDictionaryDaoImpl extends GenericDaoBase<VnfDictionaryVO, Long> implements VnfDictionaryDao {

    private SearchBuilder<VnfDictionaryVO> uuidSearch;
    private SearchBuilder<VnfDictionaryVO> templateSearch;
    private SearchBuilder<VnfDictionaryVO> networkSearch;
    private SearchBuilder<VnfDictionaryVO> vendorSearch;
    private SearchBuilder<VnfDictionaryVO> activeSearch;

    @PostConstruct
    public void init() {
        uuidSearch = createSearchBuilder();
        uuidSearch.and("uuid", uuidSearch.entity().getUuid(), SearchCriteria.Op.EQ);
        uuidSearch.done();

        templateSearch = createSearchBuilder();
        templateSearch.and("templateId", templateSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        templateSearch.and("removed", templateSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        templateSearch.done();

        networkSearch = createSearchBuilder();
        networkSearch.and("networkId", networkSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        networkSearch.and("removed", networkSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        networkSearch.done();

        vendorSearch = createSearchBuilder();
        vendorSearch.and("vendor", vendorSearch.entity().getVendor(), SearchCriteria.Op.EQ);
        vendorSearch.and("removed", vendorSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        vendorSearch.done();

        activeSearch = createSearchBuilder();
        activeSearch.and("removed", activeSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        activeSearch.done();
    }

    @Override
    public VnfDictionaryVO findByUuid(String uuid) {
        SearchCriteria<VnfDictionaryVO> sc = uuidSearch.create();
        sc.setParameters("uuid", uuid);
        return findOneBy(sc);
    }

    @Override
    public VnfDictionaryVO findByTemplateId(Long templateId) {
        SearchCriteria<VnfDictionaryVO> sc = templateSearch.create();
        sc.setParameters("templateId", templateId);
        return findOneBy(sc);
    }

    @Override
    public VnfDictionaryVO findByNetworkId(Long networkId) {
        SearchCriteria<VnfDictionaryVO> sc = networkSearch.create();
        sc.setParameters("networkId", networkId);
        return findOneBy(sc);
    }

    @Override
    public List<VnfDictionaryVO> listByVendor(String vendor) {
        SearchCriteria<VnfDictionaryVO> sc = vendorSearch.create();
        sc.setParameters("vendor", vendor);
        return listBy(sc);
    }

    @Override
    public List<VnfDictionaryVO> listActive() {
        SearchCriteria<VnfDictionaryVO> sc = activeSearch.create();
        Filter filter = new Filter(VnfDictionaryVO.class, "created", false);
        return listBy(sc, filter);
    }
}
