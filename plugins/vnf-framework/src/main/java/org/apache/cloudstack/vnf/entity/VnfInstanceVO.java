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

import com.cloud.utils.db.GenericDao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "vnf_instance")
public class VnfInstanceVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "vnf_template_id")
    private Long vnfTemplateId;

    @Column(name = "vm_instance_id")
    private Long vmInstanceId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "domain_id")
    private Long domainId;

    @Column(name = "zone_id")
    private Long zoneId;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(name = "management_ip")
    private String managementIp;

    @Column(name = GenericDao.CREATED_COLUMN)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = GenericDao.REMOVED_COLUMN)
    @Temporal(TemporalType.TIMESTAMP)
    private Date removed;

    public enum State {
        Creating,
        Running,
        Stopped,
        Destroying,
        Destroyed,
        Error
    }

    public VnfInstanceVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    public VnfInstanceVO(String name, Long vnfTemplateId, Long vmInstanceId, Long accountId, Long domainId, Long zoneId) {
        this();
        this.name = name;
        this.vnfTemplateId = vnfTemplateId;
        this.vmInstanceId = vmInstanceId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.zoneId = zoneId;
        this.state = State.Creating;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getVnfTemplateId() { return vnfTemplateId; }
    public void setVnfTemplateId(Long vnfTemplateId) { this.vnfTemplateId = vnfTemplateId; }

    public Long getVmInstanceId() { return vmInstanceId; }
    public void setVmInstanceId(Long vmInstanceId) { this.vmInstanceId = vmInstanceId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Long getDomainId() { return domainId; }
    public void setDomainId(Long domainId) { this.domainId = domainId; }

    public Long getZoneId() { return zoneId; }
    public void setZoneId(Long zoneId) { this.zoneId = zoneId; }

    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public String getManagementIp() { return managementIp; }
    public void setManagementIp(String managementIp) { this.managementIp = managementIp; }

    public Date getCreated() { return created; }
    public void setCreated(Date created) { this.created = created; }

    public Date getRemoved() { return removed; }
    public void setRemoved(Date removed) { this.removed = removed; }
}
