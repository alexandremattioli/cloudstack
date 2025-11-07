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

package org.apache.cloudstack.vnf.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * VnfApplianceVO - Represents a VNF appliance VM instance
 * Maps to vnf_appliances table
 */
@Entity
@Table(name = "vnf_appliances")
public class VnfApplianceVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", nullable = false, length = 40)
    private String uuid;

    @Column(name = "vm_instance_id", nullable = false)
    private Long vmInstanceId;

    @Column(name = "network_id", nullable = false)
    private Long networkId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "dictionary_id")
    private Long dictionaryId;

    @Column(name = "management_ip", length = 40)
    private String managementIp;

    @Column(name = "guest_ip", length = 40)
    private String guestIp;

    @Column(name = "public_ip", length = 40)
    private String publicIp;

    @Column(name = "broker_vm_id")
    private Long brokerVmId;

    @Column(name = "state", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private VnfState state = VnfState.Deploying;

    @Column(name = "last_contact")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastContact;

    @Column(name = "health_status", length = 32)
    @Enumerated(EnumType.STRING)
    private HealthStatus healthStatus = HealthStatus.Unknown;

    @Column(name = "created", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "removed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date removed;

    public enum VnfState {
        Deploying,
        Running,
        Stopped,
        Error,
        Destroyed
    }

    public enum HealthStatus {
        Healthy,
        Unhealthy,
        Unknown
    }

    public VnfApplianceVO() {
        this.uuid = UUID.randomUUID().toString();
        this.created = new Date();
    }

    public VnfApplianceVO(Long vmInstanceId, Long networkId, Long templateId) {
        this();
        this.vmInstanceId = vmInstanceId;
        this.networkId = networkId;
        this.templateId = templateId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Long getVmInstanceId() {
        return vmInstanceId;
    }

    public void setVmInstanceId(Long vmInstanceId) {
        this.vmInstanceId = vmInstanceId;
    }

    public Long getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Long networkId) {
        this.networkId = networkId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getDictionaryId() {
        return dictionaryId;
    }

    public void setDictionaryId(Long dictionaryId) {
        this.dictionaryId = dictionaryId;
    }

    public String getManagementIp() {
        return managementIp;
    }

    public void setManagementIp(String managementIp) {
        this.managementIp = managementIp;
    }

    public String getGuestIp() {
        return guestIp;
    }

    public void setGuestIp(String guestIp) {
        this.guestIp = guestIp;
    }

    public String getPublicIp() {
        return publicIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public Long getBrokerVmId() {
        return brokerVmId;
    }

    public void setBrokerVmId(Long brokerVmId) {
        this.brokerVmId = brokerVmId;
    }

    public VnfState getState() {
        return state;
    }

    public void setState(VnfState state) {
        this.state = state;
    }

    public Date getLastContact() {
        return lastContact;
    }

    public void setLastContact(Date lastContact) {
        this.lastContact = lastContact;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(Date removed) {
        this.removed = removed;
    }
}
