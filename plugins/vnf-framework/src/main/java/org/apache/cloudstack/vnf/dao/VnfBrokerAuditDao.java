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
import org.apache.cloudstack.vnf.entity.VnfBrokerAuditVO;

import java.util.Date;
import java.util.List;

public interface VnfBrokerAuditDao extends GenericDao<VnfBrokerAuditVO, Long> {
    
    /**
     * List audit logs for a VNF appliance
     */
    List<VnfBrokerAuditVO> listByApplianceId(Long vnfApplianceId);
    
    /**
     * List failed operations for troubleshooting
     */
    List<VnfBrokerAuditVO> listFailed(Long vnfApplianceId);
    
    /**
     * List audit logs by operation type
     */
    List<VnfBrokerAuditVO> listByOperation(String operation);
    
    /**
     * Delete old audit logs (for cleanup)
     */
    int deleteOlderThan(Date cutoffDate);
}
