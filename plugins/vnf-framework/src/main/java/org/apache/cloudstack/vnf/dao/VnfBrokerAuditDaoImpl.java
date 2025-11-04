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
import org.apache.cloudstack.vnf.entity.VnfBrokerAuditVO;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

@Component
public class VnfBrokerAuditDaoImpl extends GenericDaoBase<VnfBrokerAuditVO, Long> implements VnfBrokerAuditDao {

    private SearchBuilder<VnfBrokerAuditVO> applianceSearch;
    private SearchBuilder<VnfBrokerAuditVO> failedSearch;
    private SearchBuilder<VnfBrokerAuditVO> operationSearch;
    private SearchBuilder<VnfBrokerAuditVO> oldRecordsSearch;

    @PostConstruct
    public void init() {
        applianceSearch = createSearchBuilder();
        applianceSearch.and("vnfApplianceId", applianceSearch.entity().getVnfApplianceId(), SearchCriteria.Op.EQ);
        applianceSearch.done();

        failedSearch = createSearchBuilder();
        failedSearch.and("vnfApplianceId", failedSearch.entity().getVnfApplianceId(), SearchCriteria.Op.EQ);
        failedSearch.and("success", failedSearch.entity().getSuccess(), SearchCriteria.Op.EQ);
        failedSearch.done();

        operationSearch = createSearchBuilder();
        operationSearch.and("operation", operationSearch.entity().getOperation(), SearchCriteria.Op.EQ);
        operationSearch.done();

        oldRecordsSearch = createSearchBuilder();
        oldRecordsSearch.and("requestTimestamp", oldRecordsSearch.entity().getRequestTimestamp(), SearchCriteria.Op.LT);
        oldRecordsSearch.done();
    }

    @Override
    public List<VnfBrokerAuditVO> listByApplianceId(Long vnfApplianceId) {
        SearchCriteria<VnfBrokerAuditVO> sc = applianceSearch.create();
        sc.setParameters("vnfApplianceId", vnfApplianceId);
        Filter filter = new Filter(VnfBrokerAuditVO.class, "requestTimestamp", false);
        return listBy(sc, filter);
    }

    @Override
    public List<VnfBrokerAuditVO> listFailed(Long vnfApplianceId) {
        SearchCriteria<VnfBrokerAuditVO> sc = failedSearch.create();
        sc.setParameters("vnfApplianceId", vnfApplianceId);
        sc.setParameters("success", false);
        Filter filter = new Filter(VnfBrokerAuditVO.class, "requestTimestamp", false);
        return listBy(sc, filter);
    }

    @Override
    public List<VnfBrokerAuditVO> listByOperation(String operation) {
        SearchCriteria<VnfBrokerAuditVO> sc = operationSearch.create();
        sc.setParameters("operation", operation);
        Filter filter = new Filter(VnfBrokerAuditVO.class, "requestTimestamp", false);
        return listBy(sc, filter);
    }

    @Override
    public int deleteOlderThan(Date cutoffDate) {
        SearchCriteria<VnfBrokerAuditVO> sc = oldRecordsSearch.create();
        sc.setParameters("requestTimestamp", cutoffDate);
        return expunge(sc);
    }
}
