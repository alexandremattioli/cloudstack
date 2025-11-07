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

import org.apache.cloudstack.vnf.entity.VnfDictionaryVO;
import org.springframework.stereotype.Component;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.List;

@Component
public class VnfDictionaryDaoImpl extends GenericDaoBase<VnfDictionaryVO, Long> implements VnfDictionaryDao {
    
    private final SearchBuilder<VnfDictionaryVO> uuidSearch;
    private final SearchBuilder<VnfDictionaryVO> templateIdSearch;
    private final SearchBuilder<VnfDictionaryVO> networkIdSearch;
    private final SearchBuilder<VnfDictionaryVO> vendorSearch;
    private final SearchBuilder<VnfDictionaryVO> activeSearch;

    public VnfDictionaryDaoImpl() {
        uuidSearch = createSearchBuilder();
        uuidSearch.and("uuid", uuidSearch.entity().getUuid(), SearchCriteria.Op.EQ);
        uuidSearch.done();

        templateIdSearch = createSearchBuilder();
        templateIdSearch.and("templateId", templateIdSearch.entity().getTemplateId(), SearchCriteria.Op.EQ);
        templateIdSearch.and("removed", templateIdSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        templateIdSearch.done();

        networkIdSearch = createSearchBuilder();
        networkIdSearch.and("networkId", networkIdSearch.entity().getNetworkId(), SearchCriteria.Op.EQ);
        networkIdSearch.and("removed", networkIdSearch.entity().getRemoved(), SearchCriteria.Op.NULL);
        networkIdSearch.done();

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
        SearchCriteria<VnfDictionaryVO> sc = templateIdSearch.create();
        sc.setParameters("templateId", templateId);
        return findOneBy(sc);
    }

    @Override
    public VnfDictionaryVO findByNetworkId(Long networkId) {
        SearchCriteria<VnfDictionaryVO> sc = networkIdSearch.create();
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
        return listBy(sc);
    }
}
