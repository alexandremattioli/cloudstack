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

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

/**
 * VnfDictionaryVO - Represents a VNF dictionary stored in database
 * Maps to vnf_dictionaries table
 */
@Entity
@Table(name = "vnf_dictionaries")
public class VnfDictionaryVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "uuid", nullable = false, length = 40)
    private String uuid;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "network_id")
    private Long networkId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "yaml_content", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String yamlContent;

    @Column(name = "schema_version", nullable = false, length = 10)
    private String schemaVersion = "1.0";

    @Column(name = "vendor", length = 100)
    private String vendor;

    @Column(name = "product", length = 100)
    private String product;

    @Column(name = "created", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    @Column(name = "removed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date removed;

    public VnfDictionaryVO() {
        this.uuid = UUID.randomUUID().toString();
        this.created = new Date();
    }

    public VnfDictionaryVO(Long templateId, Long networkId, String name, String yamlContent) {
        this();
        this.templateId = templateId;
        this.networkId = networkId;
        this.name = name;
        this.yamlContent = yamlContent;
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

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public Long getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Long networkId) {
        this.networkId = networkId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getYamlContent() {
        return yamlContent;
    }

    public void setYamlContent(String yamlContent) {
        this.yamlContent = yamlContent;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Date getRemoved() {
        return removed;
    }

    public void setRemoved(Date removed) {
        this.removed = removed;
    }
}
