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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.apache.cloudstack.vnf.dao.VnfOperationDao;
import org.apache.cloudstack.vnf.dao.VnfOperationVO;
import org.apache.cloudstack.vnf.client.VnfBrokerClient;

import com.cloud.exception.InvalidParameterValueException;

@RunWith(MockitoJUnitRunner.class)
public class VnfServiceImplTest {

    @Mock
    private VnfOperationDao vnfOperationDao;

    @Mock
    private VnfBrokerClient brokerClient;

    private VnfServiceImpl vnfService;

    @Before
    public void setUp() {
        vnfService = new VnfServiceImpl();
        vnfService.vnfOperationDao = vnfOperationDao;
        vnfService.brokerClient = brokerClient;
    }

    @Test
    public void testCreateFirewallRuleWithRuleId() {
        // Given
        Long networkId = 1L;
        Long vnfInstanceId = 100L;
        String ruleId = "fw-rule-123";
        String action = "allow";
        String protocol = "tcp";
        String sourceAddress = "10.0.0.1/32";
        String destAddress = "10.0.0.2/32";
        String destPorts = "443";

        // Mock: No existing operation
        when(vnfOperationDao.findByRuleId(ruleId)).thenReturn(null);
        when(vnfOperationDao.findByOpHash(anyString())).thenReturn(null);

        // Mock broker response
        VnfBrokerClient.FirewallRuleResponse brokerResponse = new VnfBrokerClient.FirewallRuleResponse();
        brokerResponse.vendorRef = "pf-rule-456";
        brokerResponse.state = "active";
        when(brokerClient.createFirewallRule(any())).thenReturn(brokerResponse);

        // When
        VnfOperationVO result = vnfService.createFirewallRule(
            networkId, vnfInstanceId, ruleId, action, protocol,
            sourceAddress, destAddress, destPorts, null
        );

        // Then
        assertNotNull(result);
        assertEquals(ruleId, result.getRuleId());
        assertEquals(VnfOperationVO.State.Completed, result.getState());
        verify(vnfOperationDao, times(1)).persist(any(VnfOperationVO.class));
        verify(brokerClient, times(1)).createFirewallRule(any());
    }

    @Test
    public void testCreateFirewallRuleIdempotentByRuleId() {
        // Given
        String ruleId = "fw-rule-existing";
        VnfOperationVO existingOp = new VnfOperationVO();
        existingOp.setId(1L);
        existingOp.setRuleId(ruleId);
        existingOp.setState(VnfOperationVO.State.Completed);

        when(vnfOperationDao.findByRuleId(ruleId)).thenReturn(existingOp);

        // When
        VnfOperationVO result = vnfService.createFirewallRule(
            1L, 100L, ruleId, "allow", "tcp",
            "10.0.0.1/32", "10.0.0.2/32", "443", null
        );

        // Then
        assertNotNull(result);
        assertEquals(existingOp.getId(), result.getId());
        assertEquals(VnfOperationVO.State.Completed, result.getState());
        verify(brokerClient, never()).createFirewallRule(any());
    }

    @Test
    public void testCreateFirewallRuleIdempotentByOpHash() {
        // Given
        Long networkId = 1L;
        Long vnfInstanceId = 100L;
        String action = "allow";
        String protocol = "tcp";

        VnfOperationVO existingOp = new VnfOperationVO();
        existingOp.setId(2L);
        existingOp.setOpHash("computed-hash");
        existingOp.setState(VnfOperationVO.State.Completed);

        when(vnfOperationDao.findByRuleId(null)).thenReturn(null);
        when(vnfOperationDao.findByOpHash(anyString())).thenReturn(existingOp);

        // When
        VnfOperationVO result = vnfService.createFirewallRule(
            networkId, vnfInstanceId, null, action, protocol,
            "10.0.0.1/32", "10.0.0.2/32", "443", null
        );

        // Then
        assertNotNull(result);
        assertEquals(existingOp.getId(), result.getId());
        verify(brokerClient, never()).createFirewallRule(any());
    }

    @Test
    public void testComputeOperationHash() {
        // Given
        Long networkId = 1L;
        Long vnfInstanceId = 100L;
        String action = "allow";
        String protocol = "tcp";
        String sourceAddress = "10.0.0.1/32";
        String destAddress = "10.0.0.2/32";
        String destPorts = "443";

        // When
        String hash1 = vnfService.computeOperationHash(
            networkId, vnfInstanceId, action, protocol,
            sourceAddress, destAddress, destPorts, null
        );

        String hash2 = vnfService.computeOperationHash(
            networkId, vnfInstanceId, action, protocol,
            sourceAddress, destAddress, destPorts, null
        );

        // Then
        assertNotNull(hash1);
        assertEquals(64, hash1.length()); // SHA-256 produces 64 hex characters
        assertEquals(hash1, hash2); // Same inputs produce same hash
    }

    @Test
    public void testComputeOperationHashDifferentInputs() {
        // Given
        Long networkId = 1L;
        Long vnfInstanceId = 100L;

        // When
        String hash1 = vnfService.computeOperationHash(
            networkId, vnfInstanceId, "allow", "tcp",
            "10.0.0.1/32", "10.0.0.2/32", "443", null
        );

        String hash2 = vnfService.computeOperationHash(
            networkId, vnfInstanceId, "allow", "tcp",
            "10.0.0.1/32", "10.0.0.2/32", "80", null  // Different port
        );

        // Then
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotEquals(hash1, hash2); // Different inputs produce different hashes
    }

    @Test(expected = InvalidParameterValueException.class)
    public void testCreateFirewallRuleInvalidAction() {
        // Given
        String invalidAction = "invalid";

        // When
        vnfService.createFirewallRule(
            1L, 100L, null, invalidAction, "tcp",
            "10.0.0.1/32", "10.0.0.2/32", "443", null
        );

        // Then - should throw exception
    }

    @Test
    public void testDeleteFirewallRule() {
        // Given
        String ruleId = "fw-rule-delete";
        Long vnfInstanceId = 100L;

        VnfOperationVO createOp = new VnfOperationVO();
        createOp.setId(1L);
        createOp.setRuleId(ruleId);
        createOp.setState(VnfOperationVO.State.Completed);
        createOp.setResponsePayload("{\"vendorRef\": \"pf-rule-123\"}");

        when(vnfOperationDao.findByRuleId(ruleId)).thenReturn(createOp);
        when(vnfOperationDao.findByOpHash(anyString())).thenReturn(null);

        VnfBrokerClient.DeleteResponse brokerResponse = new VnfBrokerClient.DeleteResponse();
        brokerResponse.success = true;
        when(brokerClient.deleteFirewallRule(anyString())).thenReturn(brokerResponse);

        // When
        VnfOperationVO result = vnfService.deleteFirewallRule(vnfInstanceId, ruleId);

        // Then
        assertNotNull(result);
        assertEquals(VnfOperationVO.State.Completed, result.getState());
        verify(brokerClient, times(1)).deleteFirewallRule(anyString());
    }

    @Test
    public void testDeleteFirewallRuleNotFound() {
        // Given
        String nonExistentRuleId = "fw-rule-notfound";
        when(vnfOperationDao.findByRuleId(nonExistentRuleId)).thenReturn(null);

        // When
        VnfOperationVO result = vnfService.deleteFirewallRule(100L, nonExistentRuleId);

        // Then
        assertNull(result);
        verify(brokerClient, never()).deleteFirewallRule(anyString());
    }

    @Test
    public void testCreateFirewallRuleBrokerError() {
        // Given
        when(vnfOperationDao.findByRuleId(any())).thenReturn(null);
        when(vnfOperationDao.findByOpHash(any())).thenReturn(null);

        // Mock broker error
        when(brokerClient.createFirewallRule(any()))
            .thenThrow(new RuntimeException("VNF_TIMEOUT"));

        // When
        try {
            vnfService.createFirewallRule(
                1L, 100L, null, "allow", "tcp",
                "10.0.0.1/32", "10.0.0.2/32", "443", null
            );
            fail("Should throw exception");
        } catch (Exception e) {
            // Then
            assertTrue(e.getMessage().contains("VNF_TIMEOUT"));
        }
    }

    @Test
    public void testExtractJwtToken() {
        // Given
        String authHeader = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";

        // When
        String token = vnfService.extractJwtToken(authHeader);

        // Then
        assertEquals("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token", token);
    }

    @Test
    public void testExtractJwtTokenInvalid() {
        // Given
        String invalidHeader = "InvalidHeader";

        // When
        String token = vnfService.extractJwtToken(invalidHeader);

        // Then
        assertNull(token);
    }

    @Test
    public void testOperationStateTransition() {
        // Given
        VnfOperationVO operation = new VnfOperationVO();
        operation.setState(VnfOperationVO.State.Pending);

        when(vnfOperationDao.findByRuleId(any())).thenReturn(null);
        when(vnfOperationDao.findByOpHash(any())).thenReturn(null);
        when(vnfOperationDao.persist(any())).thenAnswer(invocation -> {
            VnfOperationVO op = invocation.getArgument(0);
            assertEquals(VnfOperationVO.State.InProgress, op.getState());
            return op;
        });

        VnfBrokerClient.FirewallRuleResponse brokerResponse = new VnfBrokerClient.FirewallRuleResponse();
        brokerResponse.vendorRef = "pf-rule-789";
        brokerResponse.state = "active";
        when(brokerClient.createFirewallRule(any())).thenReturn(brokerResponse);

        // When
        VnfOperationVO result = vnfService.createFirewallRule(
            1L, 100L, "test-rule", "allow", "tcp",
            "10.0.0.1/32", "10.0.0.2/32", "443", null
        );

        // Then
        assertEquals(VnfOperationVO.State.Completed, result.getState());
        assertNotNull(result.getCompletedAt());
    }
}
