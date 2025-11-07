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
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.db.TransactionCallback;
import com.cloud.utils.db.TransactionStatus;
import org.apache.cloudstack.vnf.dao.*;
import org.apache.cloudstack.vnf.entity.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * VnfServiceImpl - Implementation of VNF Framework service layer
 */
@Component
public class VnfServiceImpl extends ManagerBase implements VnfService {
    
    private static final Logger s_logger = LogManager.getLogger(VnfServiceImpl.class);
    
    @Inject
    private VnfDictionaryDao vnfDictionaryDao;
    
    @Inject
    private VnfApplianceDao vnfApplianceDao;
    
    @Inject
    private VnfReconciliationLogDao vnfReconciliationLogDao;
    
    @Inject
    private VnfBrokerAuditDao vnfBrokerAuditDao;
    
    // =====================================================
    // Dictionary Management
    // =====================================================
    
    @Override
    public VnfDictionaryVO createOrUpdateDictionary(Long templateId, Long networkId, String name, String yamlContent) throws CloudException {
        if ((templateId == null && networkId == null) || (templateId != null && networkId != null)) {
            throw new InvalidParameterValueException("Either templateId or networkId must be specified, but not both");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidParameterValueException("Dictionary name is required");
        }
        
        if (yamlContent == null || yamlContent.trim().isEmpty()) {
            throw new InvalidParameterValueException("YAML content is required");
        }
        
        return Transaction.execute(new TransactionCallback<VnfDictionaryVO>() {
            @Override
            public VnfDictionaryVO doInTransaction(TransactionStatus status) {
                // Check if dictionary already exists
                VnfDictionaryVO existing = null;
                if (templateId != null) {
                    existing = vnfDictionaryDao.findByTemplateId(templateId);
                } else {
                    existing = vnfDictionaryDao.findByNetworkId(networkId);
                }
                
                if (existing != null) {
                    // Update existing
                    existing.setName(name);
                    existing.setYamlContent(yamlContent);
                    existing.setUpdated(new Date());
                    vnfDictionaryDao.update(existing.getId(), existing);
                    s_logger.info("Updated VNF dictionary: " + existing.getUuid());
                    return existing;
                } else {
                    // Create new
                    VnfDictionaryVO dictionary = new VnfDictionaryVO(templateId, networkId, name, yamlContent);
                    vnfDictionaryDao.persist(dictionary);
                    s_logger.info("Created VNF dictionary: " + dictionary.getUuid());
                    return dictionary;
                }
            }
        });
    }
    
    @Override
    public VnfDictionaryVO getDictionary(Long templateId, Long networkId) throws CloudException {
        // Network-specific dictionary takes precedence
        if (networkId != null) {
            VnfDictionaryVO networkDict = vnfDictionaryDao.findByNetworkId(networkId);
            if (networkDict != null) {
                return networkDict;
            }
        }
        
        // Fall back to template dictionary
        if (templateId != null) {
            return vnfDictionaryDao.findByTemplateId(templateId);
        }
        
        return null;
    }
    
    @Override
    public boolean deleteDictionary(String uuid) throws CloudException {
        VnfDictionaryVO dictionary = vnfDictionaryDao.findByUuid(uuid);
        if (dictionary == null) {
            throw new InvalidParameterValueException("Dictionary not found: " + uuid);
        }
        
        // Soft delete
        dictionary.setRemoved(new Date());
        vnfDictionaryDao.update(dictionary.getId(), dictionary);
        s_logger.info("Deleted VNF dictionary: " + uuid);
        return true;
    }
    
    @Override
    public List<VnfDictionaryVO> listDictionaries() {
        return vnfDictionaryDao.listActive();
    }
    
    @Override
    public boolean validateDictionary(String yamlContent) throws CloudException {
        // TODO: Implement YAML validation with dictionary parser
        // For now, just check if it's not empty
        return yamlContent != null && !yamlContent.trim().isEmpty();
    }
    
    // =====================================================
    // VNF Appliance Management
    // =====================================================
    
    @Override
    public VnfApplianceVO deployVnfAppliance(Long networkId, Long templateId, Long vmInstanceId) throws CloudException {
        if (networkId == null || templateId == null || vmInstanceId == null) {
            throw new InvalidParameterValueException("networkId, templateId, and vmInstanceId are required");
        }
        
        // Check if appliance already exists for network
        VnfApplianceVO existing = vnfApplianceDao.findByNetworkId(networkId);
        if (existing != null && existing.getRemoved() == null) {
            throw new InvalidParameterValueException("VNF appliance already exists for network: " + networkId);
        }
        
        return Transaction.execute(new TransactionCallback<VnfApplianceVO>() {
            @Override
            public VnfApplianceVO doInTransaction(TransactionStatus status) {
                VnfApplianceVO appliance = new VnfApplianceVO(vmInstanceId, networkId, templateId);
                appliance.setState(VnfApplianceVO.VnfState.Deploying);
                appliance.setHealthStatus(VnfApplianceVO.HealthStatus.Unknown);
                vnfApplianceDao.persist(appliance);
                s_logger.info("Deployed VNF appliance: " + appliance.getUuid() + " for network: " + networkId);
                return appliance;
            }
        });
    }
    
    @Override
    public VnfApplianceVO getVnfApplianceForNetwork(Long networkId) {
        return vnfApplianceDao.findByNetworkId(networkId);
    }
    
    @Override
    public VnfApplianceVO getVnfAppliance(String uuid) {
        return vnfApplianceDao.findByUuid(uuid);
    }
    
    @Override
    public VnfApplianceVO updateApplianceState(Long applianceId, VnfApplianceVO.VnfState state) throws CloudException {
        VnfApplianceVO appliance = vnfApplianceDao.findById(applianceId);
        if (appliance == null) {
            throw new InvalidParameterValueException("VNF appliance not found: " + applianceId);
        }
        
        appliance.setState(state);
        vnfApplianceDao.update(applianceId, appliance);
        s_logger.info("Updated VNF appliance " + appliance.getUuid() + " state to: " + state);
        return appliance;
    }
    
    @Override
    public VnfApplianceVO updateHealthStatus(Long applianceId, VnfApplianceVO.HealthStatus status) throws CloudException {
        VnfApplianceVO appliance = vnfApplianceDao.findById(applianceId);
        if (appliance == null) {
            throw new InvalidParameterValueException("VNF appliance not found: " + applianceId);
        }
        
        appliance.setHealthStatus(status);
        appliance.setLastContact(new Date());
        vnfApplianceDao.update(applianceId, appliance);
        s_logger.debug("Updated VNF appliance " + appliance.getUuid() + " health to: " + status);
        return appliance;
    }
    
    @Override
    public List<VnfApplianceVO> listAppliances() {
        return vnfApplianceDao.listActive();
    }
    
    @Override
    public List<VnfApplianceVO> listAppliancesByState(VnfApplianceVO.VnfState state) {
        return vnfApplianceDao.listByState(state);
    }
    
    @Override
    public boolean destroyVnfAppliance(Long applianceId) throws CloudException {
        VnfApplianceVO appliance = vnfApplianceDao.findById(applianceId);
        if (appliance == null) {
            throw new InvalidParameterValueException("VNF appliance not found: " + applianceId);
        }
        
        // Soft delete
        appliance.setRemoved(new Date());
        appliance.setState(VnfApplianceVO.VnfState.Destroyed);
        vnfApplianceDao.update(applianceId, appliance);
        s_logger.info("Destroyed VNF appliance: " + appliance.getUuid());
        return true;
    }
    
    // =====================================================
    // Health Check Operations
    // =====================================================
    
    @Override
    public boolean performHealthCheck(Long applianceId) throws CloudException {
        VnfApplianceVO appliance = vnfApplianceDao.findById(applianceId);
        if (appliance == null) {
            throw new InvalidParameterValueException("VNF appliance not found: " + applianceId);
        }
        
        // TODO: Implement actual health check via broker client
        // For now, just update last contact
        appliance.setLastContact(new Date());
        appliance.setHealthStatus(VnfApplianceVO.HealthStatus.Healthy);
        vnfApplianceDao.update(applianceId, appliance);
        
        s_logger.debug("Health check performed on appliance: " + appliance.getUuid());
        return true;
    }
    
    @Override
    public List<VnfApplianceVO> performHealthChecks() {
        List<VnfApplianceVO> appliances = vnfApplianceDao.listByState(VnfApplianceVO.VnfState.Running);
        
        for (VnfApplianceVO appliance : appliances) {
            try {
                performHealthCheck(appliance.getId());
            } catch (CloudException e) {
                s_logger.warn("Health check failed for appliance " + appliance.getUuid() + ": " + e.getMessage());
            }
        }
        
        return appliances;
    }
    
    @Override
    public List<VnfApplianceVO> getStaleAppliances(int minutesStale) {
        return vnfApplianceDao.listStaleContacts(minutesStale);
    }
    
    // =====================================================
    // Reconciliation Operations
    // =====================================================
    
    @Override
    public VnfReconciliationLogVO reconcileNetwork(Long networkId, boolean dryRun) throws CloudException {
        VnfApplianceVO appliance = vnfApplianceDao.findByNetworkId(networkId);
        if (appliance == null) {
            throw new InvalidParameterValueException("No VNF appliance found for network: " + networkId);
        }
        
        return Transaction.execute(new TransactionCallback<VnfReconciliationLogVO>() {
            @Override
            public VnfReconciliationLogVO doInTransaction(TransactionStatus status) {
                VnfReconciliationLogVO log = new VnfReconciliationLogVO(networkId, appliance.getId());
                log.setStatus(VnfReconciliationLogVO.ReconciliationStatus.Running);
                vnfReconciliationLogDao.persist(log);
                
                try {
                    // TODO: Implement actual reconciliation logic
                    // 1. Query rules from CloudStack DB
                    // 2. Query rules from VNF device
                    // 3. Compare and detect drift
                    // 4. If not dryRun, fix missing rules
                    
                    // For now, simulate successful reconciliation
                    log.setCompleted(new Date());
                    log.setStatus(VnfReconciliationLogVO.ReconciliationStatus.Success);
                    log.setRulesChecked(0);
                    log.setDriftDetected(false);
                    vnfReconciliationLogDao.update(log.getId(), log);
                    
                    s_logger.info("Reconciliation completed for network: " + networkId + " (dryRun=" + dryRun + ")");
                    
                } catch (Exception e) {
                    log.setCompleted(new Date());
                    log.setStatus(VnfReconciliationLogVO.ReconciliationStatus.Failed);
                    log.setErrorMessage(e.getMessage());
                    vnfReconciliationLogDao.update(log.getId(), log);
                    s_logger.error("Reconciliation failed for network: " + networkId, e);
                }
                
                return log;
            }
        });
    }
    
    @Override
    public VnfReconciliationLogVO getLatestReconciliation(Long networkId) {
        return vnfReconciliationLogDao.findLatestByNetworkId(networkId);
    }
    
    @Override
    public List<VnfReconciliationLogVO> listReconciliations(Long networkId) {
        return vnfReconciliationLogDao.listByNetworkId(networkId);
    }
    
    @Override
    public List<VnfReconciliationLogVO> listDriftReconciliations() {
        return vnfReconciliationLogDao.listWithDrift();
    }
    
    // =====================================================
    // Firewall Rule Operations
    // =====================================================
    
    @Override
    public String applyFirewallRule(Long ruleId) throws CloudException {
        // TODO: Implement firewall rule application
        // 1. Load rule from CloudStack DB
        // 2. Get VNF appliance for rule's network
        // 3. Get dictionary
        // 4. Build request using dictionary template
        // 5. Send request via broker client
        // 6. Parse response and extract external ID
        // 7. Update rule with external ID
        
        s_logger.info("Apply firewall rule: " + ruleId);
        return "vnf-rule-" + ruleId; // Placeholder
    }
    
    @Override
    public boolean deleteFirewallRule(Long ruleId) throws CloudException {
        // TODO: Implement firewall rule deletion
        // 1. Load rule from CloudStack DB
        // 2. Get external ID from rule
        // 3. Get VNF appliance for rule's network
        // 4. Get dictionary
        // 5. Build delete request
        // 6. Send request via broker client
        
        s_logger.info("Delete firewall rule: " + ruleId);
        return true; // Placeholder
    }
    
    @Override
    public List<String> listFirewallRules(Long networkId) throws CloudException {
        // TODO: Implement rule listing from VNF device
        // 1. Get VNF appliance for network
        // 2. Get dictionary
        // 3. Build list request
        // 4. Send request via broker client
        // 5. Parse response and return rules
        
        s_logger.info("List firewall rules for network: " + networkId);
        return List.of(); // Placeholder
    }
    
    // =====================================================
    // Query Operations
    // =====================================================
    
    @Override
    public List<Object> getOperationAuditLogs(Long applianceId, String operation, int limit) {
        List<VnfBrokerAuditVO> logs;
        if (operation != null) {
            logs = vnfBrokerAuditDao.listByOperation(operation);
        } else {
            logs = vnfBrokerAuditDao.listByApplianceId(applianceId);
        }
        
        // Return up to 'limit' results
        return logs.stream()
                   .limit(limit > 0 ? limit : logs.size())
                   .map(log -> (Object) log)
                   .toList();
    }
    
    @Override
    public List<Object> getFailedOperations(Long applianceId, int limit) {
        List<VnfBrokerAuditVO> logs = vnfBrokerAuditDao.listFailed(applianceId);
        
        return logs.stream()
                   .limit(limit > 0 ? limit : logs.size())
                   .map(log -> (Object) log)
                   .toList();
    }
}
