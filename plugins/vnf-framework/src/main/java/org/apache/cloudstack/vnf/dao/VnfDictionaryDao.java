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
import org.apache.cloudstack.vnf.entity.VnfDictionaryVO;

import java.util.List;

public interface VnfDictionaryDao extends GenericDao<VnfDictionaryVO, Long> {
    
    /**
     * Find dictionary by UUID
     */
    VnfDictionaryVO findByUuid(String uuid);
    
    /**
     * Find dictionary by template ID
     */
    VnfDictionaryVO findByTemplateId(Long templateId);
    
    /**
     * Find dictionary by network ID (override dictionary)
     */
    VnfDictionaryVO findByNetworkId(Long networkId);
    
    /**
     * Find all dictionaries for a vendor
     */
    List<VnfDictionaryVO> listByVendor(String vendor);
    
    /**
     * Find active (non-removed) dictionaries
     */
    List<VnfDictionaryVO> listActive();
}
