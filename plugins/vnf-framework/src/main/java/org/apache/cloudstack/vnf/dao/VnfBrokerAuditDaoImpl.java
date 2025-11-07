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

import org.apache.cloudstack.vnf.entity.VnfBrokerAuditVO;
import org.springframework.stereotype.Component;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;

import java.util.Date;
import java.util.List;

@Component
public class VnfBrokerAuditDaoImpl extends GenericDaoBase<VnfBrokerAuditVO, Long> implements VnfBrokerAuditDao {
    
    private final SearchBuilder<VnfBrokerAuditVO> applianceIdSearch;
    private final SearchBuilder<VnfBrokerAuditVO> failedSearch;
    private final SearchBuilder<VnfBrokerAuditVO> operationSearch;
    private final SearchBuilder<VnfBrokerAuditVO> olderThanSearch;

    public VnfBrokerAuditDaoImpl() {
        applianceIdSearch = createSearchBuilder();
        applianceIdSearch.and("vnfApplianceId", applianceIdSearch.entity().getVnfApplianceId(), SearchCriteria.Op.EQ);
        applianceIdSearch.done();

        failedSearch = createSearchBuilder();
        failedSearch.and("vnfApplianceId", failedSearch.entity().getVnfApplianceId(), SearchCriteria.Op.EQ);
        failedSearch.and("statusCode", failedSearch.entity().getStatusCode(), SearchCriteria.Op.GTEQ);
        failedSearch.done();

        operationSearch = createSearchBuilder();
        operationSearch.and("operation", operationSearch.entity().getOperation(), SearchCriteria.Op.EQ);
        operationSearch.done();

        olderThanSearch = createSearchBuilder();
        olderThanSearch.and("requestTimestamp", olderThanSearch.entity().getRequestTimestamp(), SearchCriteria.Op.LT);
        olderThanSearch.done();
    }

    @Override
    public List<VnfBrokerAuditVO> listByApplianceId(Long vnfApplianceId) {
        SearchCriteria<VnfBrokerAuditVO> sc = applianceIdSearch.create();
        sc.setParameters("vnfApplianceId", vnfApplianceId);
        return listBy(sc);
    }

    @Override
    public List<VnfBrokerAuditVO> listFailed(Long vnfApplianceId) {
        SearchCriteria<VnfBrokerAuditVO> sc = failedSearch.create();
        sc.setParameters("vnfApplianceId", vnfApplianceId);
        sc.setParameters("statusCode", 400); // HTTP 400+ are errors
        return listBy(sc);
    }

    @Override
    public List<VnfBrokerAuditVO> listByOperation(String operation) {
        SearchCriteria<VnfBrokerAuditVO> sc = operationSearch.create();
        sc.setParameters("operation", operation);
        return listBy(sc);
    }

    @Override
    public int deleteOlderThan(Date cutoffDate) {
        SearchCriteria<VnfBrokerAuditVO> sc = olderThanSearch.create();
        sc.setParameters("requestTimestamp", cutoffDate);
        return expunge(sc);
    }
}
