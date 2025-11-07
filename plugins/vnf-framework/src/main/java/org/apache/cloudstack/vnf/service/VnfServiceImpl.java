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
import com.cloud.utils.component.ManagerBase;
import org.apache.cloudstack.vnf.api.command.*;
import org.apache.cloudstack.vnf.api.response.*;
import org.apache.cloudstack.vnf.dao.*;
import org.apache.cloudstack.vnf.entity.*;
import org.apache.cloudstack.vnf.VnfConnectivityResult;
import org.apache.cloudstack.vnf.VnfReconciliationResult;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Component;
import org.apache.cloudstack.vnf.dictionary.VnfDictionaryParser;
import org.apache.cloudstack.vnf.dictionary.VnfDictionaryParser.ParsedDictionary;
import org.apache.cloudstack.vnf.broker.VnfBrokerClient;
import org.apache.cloudstack.vnf.broker.VnfCorrelationIds;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.apache.cloudstack.vnf.dictionary.VnfDictionaryParser;
import org.apache.cloudstack.vnf.dictionary.VnfDictionaryParser.ParsedDictionary;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * VnfServiceImpl - Core business logic for VNF Framework.
 * Clean implementation aligned with API contract and Methodology.
 */
@Component
public class VnfServiceImpl extends ManagerBase implements VnfService {
    
    private static final Logger LOGGER = LogManager.getLogger(VnfServiceImpl.class);

    @Inject
    private VnfDictionaryDao vnfDictionaryDao;
    
    @Inject
    private VnfApplianceDao vnfApplianceDao;
    
    @Inject
    private VnfOperationDao vnfOperationDao;
    
    @Inject
    private VnfReconciliationLogDao vnfReconciliationLogDao;
    
    @Inject
    private VnfBrokerAuditDao vnfBrokerAuditDao;
    @Inject
    private VnfInstanceDao vnfInstanceDao;

    private final VnfDictionaryParser dictionaryParser = new VnfDictionaryParser();
    private final VnfBrokerClient brokerClient = new VnfBrokerClient();

    // Utility: Build a deterministic hash of a basis string for idempotency
    private String buildOpHash(String basis) {
        if (basis == null) basis = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] dig = md.digest(basis.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : dig) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(basis.hashCode());
        }
    }

    // ==================================================================
    // Dictionary Management
    // ==================================================================

    @Override
    public VnfDictionaryResponse uploadVnfDictionary(UploadVnfDictionaryCmd cmd) throws CloudException {
        LOGGER.info("uploadVnfDictionary called vendor=" + cmd.getVendor());
        ParsedDictionary parsed = dictionaryParser.parse(cmd.getDictionary());
        String name = cmd.getVendor() + ":" + cmd.getVersion();
        VnfDictionaryVO existing = vnfDictionaryDao.findByUuid(name);
        if (existing != null && !cmd.getOverwrite()) {
            throw new CloudException("Dictionary already exists (use overwrite=true)");
        }
        VnfDictionaryVO vo = existing != null ? existing : new VnfDictionaryVO(null, null, name, cmd.getDictionary());
        vo.setVendor(parsed.vendor);
        vo.setProduct(parsed.product);
        vo.setSchemaVersion(parsed.version);
        if (existing == null) {
            vnfDictionaryDao.persist(vo);
        } else {
            vo.setUpdated(new java.util.Date());
            vnfDictionaryDao.update(vo.getId(), vo);
        }
        VnfDictionaryResponse resp = new VnfDictionaryResponse();
        resp.setId(vo.getUuid());
        resp.setVendor(vo.getVendor());
        resp.setVersion(vo.getSchemaVersion());
        resp.setUploaded(String.valueOf(vo.getCreated()));
        resp.setSize((long) cmd.getDictionary().getBytes(java.nio.charset.StandardCharsets.UTF_8).length);
        resp.setOperations(0); // placeholder until we parse operations section
        return resp;
    }

    @Override
    public List<VnfDictionaryResponse> listVnfDictionaries(ListVnfDictionariesCmd cmd) {
        LOGGER.info("listVnfDictionaries called vendor=" + cmd.getVendor() + " version=" + cmd.getVersion());
        List<VnfDictionaryVO> source = (cmd.getVendor() != null && !cmd.getVendor().isEmpty()) ? vnfDictionaryDao.listByVendor(cmd.getVendor()) : vnfDictionaryDao.listActive();
        List<VnfDictionaryResponse> responses = new ArrayList<>();
        for (VnfDictionaryVO vo : source) {
            if (cmd.getVersion() != null && !cmd.getVersion().isEmpty() && !cmd.getVersion().equals(vo.getSchemaVersion())) {
                continue;
            }
            VnfDictionaryResponse r = new VnfDictionaryResponse();
            r.setId(vo.getUuid());
            r.setVendor(vo.getVendor());
            r.setVersion(vo.getSchemaVersion());
            r.setUploaded(String.valueOf(vo.getCreated()));
            r.setSize(vo.getYamlContent() != null ? (long) vo.getYamlContent().getBytes(StandardCharsets.UTF_8).length : 0L);
            r.setOperations(0); // future enhancement
            responses.add(r);
        }
        return responses;
    }

    // ==================================================================
    // Firewall Rule Operations
    // ==================================================================

    @Override
    public VnfFirewallRuleResponse createFirewallRule(CreateVnfFirewallRuleCmd cmd) throws CloudException {
        LOGGER.info("createFirewallRule called");
        String correlationId = VnfCorrelationIds.newId();
        String ruleId = (cmd.getRuleId() != null && !cmd.getRuleId().isEmpty()) ? cmd.getRuleId() : correlationId;
        VnfOperationVO existing = vnfOperationDao.findByRuleId(ruleId);
        if (existing != null && "Completed".equalsIgnoreCase(existing.getState())) {
            return buildFirewallResponseFromOperation(existing, cmd.getNetworkId());
        }
        String payload = "{}"; // minimal payload; future: include full rule spec
        String opHash = buildOpHash(cmd.getVnfInstanceId() + ":" + ruleId + ":" + payload);
        VnfOperationVO op = existing != null ? existing : new VnfOperationVO(cmd.getVnfInstanceId(), VnfOperationVO.OperationType.CREATE_FIREWALL_RULE.name(), ruleId, opHash);
        op.setRequestPayload(payload);
        op.setStartedAt(new Date());
        try {
            String resp = brokerClient.createFirewallRule(payload, correlationId);
            op.setResponsePayload(resp);
            op.setState("Completed");
        } catch (CloudException ce) {
            op.setState("Failed");
            op.setErrorMessage(ce.getMessage());
            throw ce;
        } finally {
            op.setCompletedAt(new Date());
            if (existing == null) vnfOperationDao.persist(op); else vnfOperationDao.update(op.getId(), op);
        }
        return buildFirewallResponseFromOperation(op, cmd.getNetworkId());
    }
    
    private VnfFirewallRuleResponse buildFirewallResponseFromOperation(VnfOperationVO op, Long networkId) {
        VnfFirewallRuleResponse r = new VnfFirewallRuleResponse();
        r.setId(op.getUuid());
        r.setRuleId(op.getRuleId());
        r.setVnfInstanceId(String.valueOf(op.getVnfInstanceId()));
        if (networkId != null) {
            r.setNetworkId(String.valueOf(networkId));
        }
        r.setState(op.getState());
        r.setErrorMessage(op.getErrorMessage());
        r.setCreated(String.valueOf(op.getCreatedAt()));
        return r;
    }

    @Override
    public VnfFirewallRuleResponse updateVnfFirewallRule(UpdateVnfFirewallRuleCmd cmd) throws CloudException {
        LOGGER.info("updateVnfFirewallRule called");
        String correlationId = VnfCorrelationIds.newId();
        if (cmd.getId() == null) {
            throw new CloudException("Firewall rule ID required for update");
        }
        String ruleId = String.valueOf(cmd.getId());
        VnfOperationVO op = vnfOperationDao.findByRuleId(ruleId);
        if (op == null) {
            throw new CloudException("Cannot update unknown firewall rule: " + ruleId);
        }
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        if (cmd.getAction() != null) { sb.append("\"action\":\"" + cmd.getAction() + "\""); first = false; }
        if (cmd.getEnabled() != null) { if (!first) sb.append(','); sb.append("\"enabled\":" + cmd.getEnabled()); }
        sb.append('}');
        String payload = sb.toString();
        op.setStartedAt(new Date());
        op.setRequestPayload(payload);
        try {
            String brokerResp = brokerClient.updateFirewallRule(ruleId, payload, correlationId);
            op.setResponsePayload(brokerResp);
            op.setState("Completed");
        } catch (CloudException ce) {
            op.setState("Failed");
            op.setErrorMessage(ce.getMessage());
            throw ce;
        } finally {
            op.setCompletedAt(new Date());
            vnfOperationDao.update(op.getId(), op);
        }
        return buildFirewallResponseFromOperation(op, null);
    }

    @Override
    public boolean deleteVnfFirewallRule(DeleteVnfFirewallRuleCmd cmd) throws CloudException {
        LOGGER.info("deleteVnfFirewallRule called");
        if (cmd.getId() == null) throw new CloudException("Firewall rule ID required for delete");
        String ruleId = String.valueOf(cmd.getId());
        String correlationId = VnfCorrelationIds.newId();
        VnfOperationVO found = vnfOperationDao.findByRuleId(ruleId);
        boolean isNew = (found == null);
        VnfOperationVO op = isNew ? new VnfOperationVO(cmd.getVnfInstanceId(), VnfOperationVO.OperationType.DELETE_FIREWALL_RULE.name(), ruleId, buildOpHash("DEL:" + ruleId)) : found;
        op.setStartedAt(new Date());
        try {
            brokerClient.deleteFirewallRule(ruleId, correlationId);
            op.setState("Completed");
        } catch (CloudException ce) {
            op.setState("Failed");
            op.setErrorMessage(ce.getMessage());
            throw ce;
        } finally {
            op.setCompletedAt(new Date());
            if (isNew) vnfOperationDao.persist(op); else vnfOperationDao.update(op.getId(), op);
        }
        return "Completed".equalsIgnoreCase(op.getState());
    }

    // ==================================================================
    // NAT Rule Operations
    // ==================================================================

    @Override
    public VnfNATRuleResponse createVnfNATRule(CreateVnfNATRuleCmd cmd) throws CloudException {
        LOGGER.info("createVnfNATRule called");
        String correlationId = VnfCorrelationIds.newId();
        String ruleId = correlationId; // NAT rule id, can be improved to accept external id
        String payload = "{" +
                "\"vnfInstanceId\":\"" + cmd.getVnfInstanceId() + "\"," +
                "\"networkId\":\"" + cmd.getNetworkId() + "\"," +
                "\"publicIp\":\"" + cmd.getPublicIp() + "\"," +
                "\"publicPort\":" + cmd.getPublicPort() + "," +
                "\"privateIp\":\"" + cmd.getPrivateIp() + "\"," +
                "\"privatePort\":" + cmd.getPrivatePort() + "," +
                "\"protocol\":\"" + cmd.getProtocol() + "\"" +
                "}";
        String opHash = buildOpHash(cmd.getVnfInstanceId() + ":" + ruleId + ":" + payload);
        VnfOperationVO op = new VnfOperationVO(cmd.getVnfInstanceId(), VnfOperationVO.OperationType.CREATE_NAT_RULE.name(), ruleId, opHash);
        op.setRequestPayload(payload);
        op.setStartedAt(new Date());
        try {
            String resp = brokerClient.createNatRule(payload, correlationId);
            op.setResponsePayload(resp);
            op.setState("Completed");
        } catch (CloudException ce) {
            op.setState("Failed");
            op.setErrorMessage(ce.getMessage());
            throw ce;
        } finally {
            op.setCompletedAt(new Date());
            vnfOperationDao.persist(op);
        }
        VnfNATRuleResponse r = new VnfNATRuleResponse();
        r.setId(ruleId);
        r.setRuleId(ruleId);
        r.setVnfInstanceId(String.valueOf(cmd.getVnfInstanceId()));
        r.setNetworkId(String.valueOf(cmd.getNetworkId()));
        r.setPublicIp(cmd.getPublicIp());
        r.setPublicPort(cmd.getPublicPort());
        r.setPrivateIp(cmd.getPrivateIp());
        r.setPrivatePort(cmd.getPrivatePort());
        r.setProtocol(cmd.getProtocol());
        r.setState(op.getState());
        return r;
    }

    // ==================================================================
    // Connectivity & Health
    // ==================================================================

    @Override
    public VnfConnectivityResult testVnfConnectivity(TestVnfConnectivityCmd cmd) throws CloudException {
        LOGGER.info("testVnfConnectivity called");
        String correlationId = VnfCorrelationIds.newId();
        long start = System.currentTimeMillis();
        try {
            brokerClient.testConnectivity(correlationId);
            long latency = System.currentTimeMillis() - start;
            VnfConnectivityResult res = new VnfConnectivityResult();
            res.setReachable(true);
            res.setLatencyMs(latency);
            return res;
        } catch (CloudException ce) {
            VnfConnectivityResult res = new VnfConnectivityResult();
            res.setReachable(false);
            res.setLatencyMs(0);
            return res;
        }
    }

    // ==================================================================
    // Network Reconciliation
    // ==================================================================

    @Override
    public VnfReconciliationResult reconcileVnfNetwork(ReconcileVnfNetworkCmd cmd) throws CloudException {
        LOGGER.info("reconcileVnfNetwork called networkId=" + cmd.getNetworkId());
        String correlationId = VnfCorrelationIds.newId();
        String payload = "{" + "\"force\":" + cmd.getForce() + "}";
        try {
            brokerClient.reconcileNetwork(String.valueOf(cmd.getNetworkId()), payload, correlationId);
            VnfReconciliationResult r = new VnfReconciliationResult();
            // Minimal result - success path, refine when broker schema is defined
            // Setting driftDetected=false by default
            r.setDriftDetected(false);
            return r;
        } catch (CloudException ce) {
            VnfReconciliationResult r = new VnfReconciliationResult();
            r.setDriftDetected(false);
            return r;
        }
    }

    // ==================================================================
    // Operation Tracking
    // ==================================================================

    @Override
    public List<org.apache.cloudstack.vnf.entity.VnfOperationVO> listAllOperations(ListVnfOperationsCmd cmd) {
        LOGGER.info("listAllOperations called - implementing per Methodology step 7");
        // TODO: Step 7 - Query operations with filters
        LOGGER.info("listAllOperations called");
        // Minimal viable implementation: return all operations (no filters yet)
        return vnfOperationDao.listAll();
    }

    @Override
    public List<org.apache.cloudstack.vnf.entity.VnfOperationVO> listOperationsByState(org.apache.cloudstack.vnf.entity.VnfOperationVO.State state) {
        LOGGER.info("listOperationsByState called state=" + state);
        return vnfOperationDao.listByState(state.name());
    }

    @Override
    public List<org.apache.cloudstack.vnf.entity.VnfOperationVO> listOperationsByVnfInstance(Long vnfInstanceId) {
        LOGGER.info("listOperationsByVnfInstance called vnfInstanceId=" + vnfInstanceId);
        return vnfOperationDao.listByVnfInstanceId(vnfInstanceId);
    }

    @Override
    public List<org.apache.cloudstack.vnf.entity.VnfOperationVO> listOperationsByVnfInstanceAndState(Long vnfInstanceId, org.apache.cloudstack.vnf.entity.VnfOperationVO.State state) {
        LOGGER.info("listOperationsByVnfInstanceAndState called vnfInstanceId=" + vnfInstanceId + " state=" + state);
        List<org.apache.cloudstack.vnf.entity.VnfOperationVO> list = vnfOperationDao.listByVnfInstanceId(vnfInstanceId);
        List<org.apache.cloudstack.vnf.entity.VnfOperationVO> filtered = new ArrayList<>();
        for (org.apache.cloudstack.vnf.entity.VnfOperationVO vo : list) {
            if (state.name().equalsIgnoreCase(vo.getState())) {
                filtered.add(vo);
            }
        }
        return filtered;
    }

    @Override
    public org.apache.cloudstack.vnf.entity.VnfOperationVO findOperationByRuleId(String ruleId) {
        LOGGER.info("findOperationByRuleId called ruleId=" + ruleId);
        return vnfOperationDao.findByRuleId(ruleId);
    }

    // ==================================================================
    // VNF Instance Management
    // ==================================================================

    @Override
    public VnfInstanceVO getVnfInstance(Long vnfInstanceId) throws CloudException {
        LOGGER.info("getVnfInstance called - implementing per Methodology step 7");
        // TODO: Step 7 - Query instance
        LOGGER.info("getVnfInstance called id=" + vnfInstanceId);
        VnfInstanceVO vo = vnfInstanceDao.findById(vnfInstanceId);
        if (vo == null) {
            throw new CloudException("VNF instance not found: " + vnfInstanceId);
        }
        return vo;
    }
}
