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

package org.apache.cloudstack.vnf;

import org.apache.cloudstack.vnf.dao.VnfOperationVO;
import com.cloud.exception.CloudException;
import java.util.List;

public interface VnfService {
    
    /**
     * Find VNF operation by rule ID
     */
    VnfOperationVO findOperationByRuleId(String ruleId) throws CloudException;
    
    /**
     * List all VNF dictionaries
     */
    List<VnfDictionary> listDictionaries(Long zoneId);
    
    /**
     * Upload VNF dictionary
     */
    VnfDictionary uploadDictionary(String name, String content, Long zoneId) throws CloudException;
    
    /**
     * Test VNF connectivity
     */
    VnfConnectivityResult testConnectivity(Long applianceId) throws CloudException;
    
    /**
     * Reconcile VNF network
     */
    VnfReconciliationResult reconcileNetwork(Long networkId) throws CloudException;
}
