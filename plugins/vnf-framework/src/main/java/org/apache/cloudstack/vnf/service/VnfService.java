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

package org.apache.cloudstack.vnf.service;

import com.cloud.exception.CloudException;
import org.apache.cloudstack.vnf.api.command.*;
import org.apache.cloudstack.vnf.api.response.*;

import org.apache.cloudstack.vnf.entity.VnfDictionaryVO;
import org.apache.cloudstack.vnf.entity.VnfInstanceVO;
import org.apache.cloudstack.vnf.VnfConnectivityResult;
import org.apache.cloudstack.vnf.VnfReconciliationResult;

import java.util.List;

/**
 * VnfService - Core service interface for VNF Framework operations.
 * Aligned with API command contract for clean design.
 */
public interface VnfService {

    // Dictionary Management
    VnfDictionaryResponse uploadVnfDictionary(UploadVnfDictionaryCmd cmd) throws CloudException;
    List<VnfDictionaryResponse> listVnfDictionaries(ListVnfDictionariesCmd cmd);

    // Firewall Rule Operations
    VnfFirewallRuleResponse createFirewallRule(CreateVnfFirewallRuleCmd cmd) throws CloudException;
    VnfFirewallRuleResponse updateVnfFirewallRule(UpdateVnfFirewallRuleCmd cmd) throws CloudException;
    boolean deleteVnfFirewallRule(DeleteVnfFirewallRuleCmd cmd) throws CloudException;

    // NAT Rule Operations
    VnfNATRuleResponse createVnfNATRule(CreateVnfNATRuleCmd cmd) throws CloudException;

    // Connectivity & Health
    VnfConnectivityResult testVnfConnectivity(TestVnfConnectivityCmd cmd) throws CloudException;

    // Network Reconciliation
    VnfReconciliationResult reconcileVnfNetwork(ReconcileVnfNetworkCmd cmd) throws CloudException;

    // Operation Tracking
    List<org.apache.cloudstack.vnf.entity.VnfOperationVO> listAllOperations(ListVnfOperationsCmd cmd);
    List<org.apache.cloudstack.vnf.entity.VnfOperationVO> listOperationsByState(org.apache.cloudstack.vnf.entity.VnfOperationVO.State state);
    List<org.apache.cloudstack.vnf.entity.VnfOperationVO> listOperationsByVnfInstance(Long vnfInstanceId);
    List<org.apache.cloudstack.vnf.entity.VnfOperationVO> listOperationsByVnfInstanceAndState(Long vnfInstanceId, org.apache.cloudstack.vnf.entity.VnfOperationVO.State state);
    org.apache.cloudstack.vnf.entity.VnfOperationVO findOperationByRuleId(String ruleId);

    // VNF Instance Management
    VnfInstanceVO getVnfInstance(Long vnfInstanceId) throws CloudException;
}
