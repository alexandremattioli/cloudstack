-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements.  See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership.  The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License.  You may obtain a copy of the License at
--
--   http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied.  See the License for the
-- specific language governing permissions and limitations
-- under the License.

-- VNF Framework Schema

CREATE TABLE IF NOT EXISTS `vnf_template` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `uuid` varchar(255) NOT NULL UNIQUE,
    `name` varchar(255) NOT NULL,
    `description` text,
    `template_id` bigint(20) unsigned NOT NULL,
    `account_id` bigint(20) unsigned NOT NULL,
    `domain_id` bigint(20) unsigned NOT NULL,
    `state` varchar(32) NOT NULL DEFAULT 'Active',
    `vnfd_content` text,
    `created` datetime NOT NULL,
    `removed` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `i_vnf_template__account_id` (`account_id`),
    INDEX `i_vnf_template__removed` (`removed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `vnf_instance` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
    `uuid` varchar(255) NOT NULL UNIQUE,
    `name` varchar(255) NOT NULL,
    `vnf_template_id` bigint(20) unsigned NOT NULL,
    `vm_instance_id` bigint(20) unsigned NOT NULL,
    `account_id` bigint(20) unsigned NOT NULL,
    `domain_id` bigint(20) unsigned NOT NULL,
    `zone_id` bigint(20) unsigned NOT NULL,
    `state` varchar(32) NOT NULL DEFAULT 'Creating',
    `management_ip` varchar(40),
    `created` datetime NOT NULL,
    `removed` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    INDEX `i_vnf_instance__account_id` (`account_id`),
    INDEX `i_vnf_instance__zone_id` (`zone_id`),
    INDEX `i_vnf_instance__vm_instance_id` (`vm_instance_id`),
    INDEX `i_vnf_instance__removed` (`removed`),
    CONSTRAINT `fk_vnf_instance__vnf_template_id` FOREIGN KEY (`vnf_template_id`) REFERENCES `vnf_template` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
