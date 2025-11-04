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
import org.apache.cloudstack.vnf.entity.VnfApplianceVO;
import org.apache.cloudstack.vnf.entity.VnfApplianceVO.VnfState;
import org.apache.cloudstack.vnf.entity.VnfApplianceVO.HealthStatus;

import java.util.List;

public interface VnfApplianceDao extends GenericDao<VnfApplianceVO, Long> {
    
    /**
     * Find appliance by UUID
     */
    VnfApplianceVO findByUuid(String uuid);
    
    /**
     * Find appliance by network ID
     */
    VnfApplianceVO findByNetworkId(Long networkId);
    
    /**
     * Find appliance by VM instance ID
     */
    VnfApplianceVO findByVmInstanceId(Long vmInstanceId);
    
    /**
     * List all appliances for a template
     */
    List<VnfApplianceVO> listByTemplateId(Long templateId);
    
    /**
     * List appliances by state
     */
    List<VnfApplianceVO> listByState(VnfState state);
    
    /**
     * List appliances by health status
     */
    List<VnfApplianceVO> listByHealthStatus(HealthStatus healthStatus);
    
    /**
     * List all active (non-removed) appliances
     */
    List<VnfApplianceVO> listActive();
    
    /**
     * Find appliances with stale last contact time (for health checks)
     */
    List<VnfApplianceVO> listStaleContacts(int minutesStale);
}
