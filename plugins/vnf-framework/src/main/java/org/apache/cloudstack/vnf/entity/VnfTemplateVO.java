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
@Table(name = "vnf_template")
public class VnfTemplateVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid")
    private String uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "domain_id")
    private Long domainId;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(name = "vnfd_content", length = 65535)
    private String vnfdContent;

    @Column(name = GenericDao.CREATED_COLUMN)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = GenericDao.REMOVED_COLUMN)
    @Temporal(TemporalType.TIMESTAMP)
    private Date removed;

    public enum State {
        Active,
        Inactive,
        Deleted
    }

    public VnfTemplateVO() {
        this.uuid = UUID.randomUUID().toString();
    }

    public VnfTemplateVO(String name, String description, Long templateId, Long accountId, Long domainId, String vnfdContent) {
        this();
        this.name = name;
        this.description = description;
        this.templateId = templateId;
        this.accountId = accountId;
        this.domainId = domainId;
        this.vnfdContent = vnfdContent;
        this.state = State.Active;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Long getDomainId() { return domainId; }
    public void setDomainId(Long domainId) { this.domainId = domainId; }

    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public String getVnfdContent() { return vnfdContent; }
    public void setVnfdContent(String vnfdContent) { this.vnfdContent = vnfdContent; }

    public Date getCreated() { return created; }
    public void setCreated(Date created) { this.created = created; }

    public Date getRemoved() { return removed; }
    public void setRemoved(Date removed) { this.removed = removed; }
}
