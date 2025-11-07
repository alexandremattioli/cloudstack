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

import com.cloud.utils.db.GenericDao;
import org.apache.cloudstack.vnf.entity.VnfReconciliationLogVO;
import org.apache.cloudstack.vnf.entity.VnfReconciliationLogVO.ReconciliationStatus;

import java.util.List;

public interface VnfReconciliationLogDao extends GenericDao<VnfReconciliationLogVO, Long> {
    
    /**
     * Find most recent reconciliation for a network
     */
    VnfReconciliationLogVO findLatestByNetworkId(Long networkId);
    
    /**
     * List all reconciliations for a network
     */
    List<VnfReconciliationLogVO> listByNetworkId(Long networkId);
    
    /**
     * List reconciliations by status
     */
    List<VnfReconciliationLogVO> listByStatus(ReconciliationStatus status);
    
    /**
     * List reconciliations with drift detected
     */
    List<VnfReconciliationLogVO> listWithDrift();
    
    /**
     * List reconciliations for a VNF appliance
     */
    List<VnfReconciliationLogVO> listByApplianceId(Long vnfApplianceId);
}
