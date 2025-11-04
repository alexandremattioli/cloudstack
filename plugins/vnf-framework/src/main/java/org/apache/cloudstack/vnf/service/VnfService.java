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
import com.cloud.network.Network;
import org.apache.cloudstack.vnf.entity.VnfApplianceVO;
import org.apache.cloudstack.vnf.entity.VnfDictionaryVO;
import org.apache.cloudstack.vnf.entity.VnfReconciliationLogVO;

import java.util.List;

/**
 * VnfService - Main service interface for VNF Framework operations
 */
public interface VnfService {
    
    // =====================================================
    // Dictionary Management
    // =====================================================
    
    /**
     * Create or update a VNF dictionary
     */
    VnfDictionaryVO createOrUpdateDictionary(Long templateId, Long networkId, String name, String yamlContent) throws CloudException;
    
    /**
     * Get dictionary for a template or network
     * Network-specific dictionary takes precedence over template dictionary
     */
    VnfDictionaryVO getDictionary(Long templateId, Long networkId) throws CloudException;
    
    /**
     * Delete a dictionary by UUID
     */
    boolean deleteDictionary(String uuid) throws CloudException;
    
    /**
     * List all active dictionaries
     */
    List<VnfDictionaryVO> listDictionaries();
    
    /**
     * Parse and validate dictionary YAML
     */
    boolean validateDictionary(String yamlContent) throws CloudException;
    
    // =====================================================
    // VNF Appliance Management
    // =====================================================
    
    /**
     * Deploy a VNF appliance for a network
     */
    VnfApplianceVO deployVnfAppliance(Long networkId, Long templateId, Long vmInstanceId) throws CloudException;
    
    /**
     * Get VNF appliance for a network
     */
    VnfApplianceVO getVnfApplianceForNetwork(Long networkId);
    
    /**
     * Get VNF appliance by UUID
     */
    VnfApplianceVO getVnfAppliance(String uuid);
    
    /**
     * Update appliance state
     */
    VnfApplianceVO updateApplianceState(Long applianceId, VnfApplianceVO.VnfState state) throws CloudException;
    
    /**
     * Update appliance health status
     */
    VnfApplianceVO updateHealthStatus(Long applianceId, VnfApplianceVO.HealthStatus status) throws CloudException;
    
    /**
     * List all active appliances
     */
    List<VnfApplianceVO> listAppliances();
    
    /**
     * List appliances by state
     */
    List<VnfApplianceVO> listAppliancesByState(VnfApplianceVO.VnfState state);
    
    /**
     * Destroy a VNF appliance
     */
    boolean destroyVnfAppliance(Long applianceId) throws CloudException;
    
    // =====================================================
    // Health Check Operations
    // =====================================================
    
    /**
     * Perform health check on a VNF appliance
     */
    boolean performHealthCheck(Long applianceId) throws CloudException;
    
    /**
     * Perform health checks on all running appliances
     */
    List<VnfApplianceVO> performHealthChecks();
    
    /**
     * Get appliances with stale last contact time
     */
    List<VnfApplianceVO> getStaleAppliances(int minutesStale);
    
    // =====================================================
    // Reconciliation Operations
    // =====================================================
    
    /**
     * Reconcile a network's rules with VNF device
     * @param networkId Network to reconcile
     * @param dryRun If true, only detect drift without fixing
     * @return Reconciliation log
     */
    VnfReconciliationLogVO reconcileNetwork(Long networkId, boolean dryRun) throws CloudException;
    
    /**
     * Get latest reconciliation result for a network
     */
    VnfReconciliationLogVO getLatestReconciliation(Long networkId);
    
    /**
     * List all reconciliations for a network
     */
    List<VnfReconciliationLogVO> listReconciliations(Long networkId);
    
    /**
     * List reconciliations with drift detected
     */
    List<VnfReconciliationLogVO> listDriftReconciliations();
    
    // =====================================================
    // Firewall Rule Operations
    // =====================================================
    
    /**
     * Apply a firewall rule to VNF device
     * @param ruleId CloudStack firewall rule ID
     * @return External ID from VNF device
     */
    String applyFirewallRule(Long ruleId) throws CloudException;
    
    /**
     * Delete a firewall rule from VNF device
     * @param ruleId CloudStack firewall rule ID
     */
    boolean deleteFirewallRule(Long ruleId) throws CloudException;
    
    /**
     * List firewall rules on VNF device
     */
    List<String> listFirewallRules(Long networkId) throws CloudException;
    
    // =====================================================
    // Query Operations
    // =====================================================
    
    /**
     * Get VNF operation audit logs
     */
    List<Object> getOperationAuditLogs(Long applianceId, String operation, int limit);
    
    /**
     * Get failed operations for troubleshooting
     */
    List<Object> getFailedOperations(Long applianceId, int limit);
}
