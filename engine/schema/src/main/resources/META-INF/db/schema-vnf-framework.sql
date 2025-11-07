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

--;
-- Schema upgrade for VNF Framework
--;

--
-- Table structure for VNF Dictionary storage
--
DROP TABLE IF EXISTS `cloud`.`vnf_dictionaries`;
CREATE TABLE `cloud`.`vnf_dictionaries` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `uuid` varchar(40) NOT NULL COMMENT 'UUID of the dictionary',
  `name` varchar(255) NOT NULL COMMENT 'Name of the dictionary',
  `template_id` bigint unsigned DEFAULT NULL COMMENT 'Template ID this dictionary applies to',
  `network_id` bigint unsigned DEFAULT NULL COMMENT 'Network ID this dictionary applies to',
  `yaml_content` mediumtext NOT NULL COMMENT 'YAML dictionary content',
  `vendor` varchar(255) DEFAULT NULL COMMENT 'Vendor name',
  `product` varchar(255) DEFAULT NULL COMMENT 'Product name',
  `version` varchar(64) DEFAULT NULL COMMENT 'Product version',
  `created` datetime NOT NULL COMMENT 'Date created',
  `updated` datetime DEFAULT NULL COMMENT 'Date last updated',
  `removed` datetime DEFAULT NULL COMMENT 'Date removed',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_network_id` (`network_id`),
  KEY `idx_removed` (`removed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VNF YAML dictionaries';

--
-- Table structure for VNF Appliance instances
--
DROP TABLE IF EXISTS `cloud`.`vnf_appliances`;
CREATE TABLE `cloud`.`vnf_appliances` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `uuid` varchar(40) NOT NULL COMMENT 'UUID of the VNF appliance',
  `vm_instance_id` bigint unsigned NOT NULL COMMENT 'VM instance ID',
  `network_id` bigint unsigned NOT NULL COMMENT 'Network ID',
  `template_id` bigint unsigned NOT NULL COMMENT 'Template ID',
  `dictionary_id` bigint unsigned DEFAULT NULL COMMENT 'Associated dictionary ID',
  `management_ip` varchar(40) DEFAULT NULL COMMENT 'Management IP address',
  `state` varchar(32) NOT NULL DEFAULT 'Unknown' COMMENT 'VNF state (Unknown, Reachable, Unreachable)',
  `health_status` varchar(32) DEFAULT NULL COMMENT 'Health check status',
  `last_contact` datetime DEFAULT NULL COMMENT 'Last successful contact timestamp',
  `created` datetime NOT NULL COMMENT 'Date created',
  `removed` datetime DEFAULT NULL COMMENT 'Date removed',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  UNIQUE KEY `vm_instance_id` (`vm_instance_id`),
  KEY `idx_network_id` (`network_id`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_dictionary_id` (`dictionary_id`),
  KEY `idx_state` (`state`),
  KEY `idx_removed` (`removed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VNF appliance instances';

--
-- Table structure for VNF Operations tracking
--
DROP TABLE IF EXISTS `cloud`.`vnf_operations`;
CREATE TABLE `cloud`.`vnf_operations` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `uuid` varchar(40) NOT NULL COMMENT 'UUID of the operation',
  `operation_id` varchar(255) DEFAULT NULL COMMENT 'External operation ID from broker',
  `rule_id` varchar(255) DEFAULT NULL COMMENT 'Rule ID (for idempotency)',
  `vnf_instance_id` bigint unsigned DEFAULT NULL COMMENT 'VNF instance ID',
  `vnf_appliance_id` bigint unsigned DEFAULT NULL COMMENT 'VNF appliance ID',
  `operation_type` varchar(64) NOT NULL COMMENT 'Operation type (CreateFirewallRule, CreateNATRule, etc)',
  `op_hash` varchar(64) DEFAULT NULL COMMENT 'Hash of operation parameters for duplicate detection',
  `request_payload` mediumtext COMMENT 'Request payload sent to broker',
  `response_payload` mediumtext COMMENT 'Response payload from broker',
  `vendor_ref` varchar(255) DEFAULT NULL COMMENT 'Vendor-specific reference from response',
  `state` varchar(32) NOT NULL DEFAULT 'Pending' COMMENT 'State (Pending, InProgress, Completed, Failed)',
  `error_code` varchar(64) DEFAULT NULL COMMENT 'Error code if failed',
  `error_message` text COMMENT 'Error message if failed',
  `created_at` datetime NOT NULL COMMENT 'When operation was created',
  `started_at` datetime DEFAULT NULL COMMENT 'When operation started processing',
  `completed_at` datetime DEFAULT NULL COMMENT 'When operation completed',
  `removed` datetime DEFAULT NULL COMMENT 'Date removed',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  KEY `idx_operation_id` (`operation_id`),
  KEY `idx_rule_id` (`rule_id`),
  KEY `idx_vnf_instance_id` (`vnf_instance_id`),
  KEY `idx_vnf_appliance_id` (`vnf_appliance_id`),
  KEY `idx_state` (`state`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_op_hash` (`op_hash`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_removed` (`removed`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VNF operations tracking';

--
-- Table structure for VNF Reconciliation logs
--
DROP TABLE IF EXISTS `cloud`.`vnf_reconciliation_log`;
CREATE TABLE `cloud`.`vnf_reconciliation_log` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `uuid` varchar(40) NOT NULL COMMENT 'UUID of the reconciliation log entry',
  `network_id` bigint unsigned NOT NULL COMMENT 'Network ID',
  `vnf_appliance_id` bigint unsigned DEFAULT NULL COMMENT 'VNF appliance ID',
  `drift_detected` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Whether drift was detected',
  `drift_summary` text COMMENT 'Summary of drift (missing, extra, mismatched rules)',
  `actions_taken` text COMMENT 'Actions taken to remediate drift',
  `created_at` datetime NOT NULL COMMENT 'When reconciliation was performed',
  `dry_run` tinyint(1) NOT NULL DEFAULT 0 COMMENT 'Whether this was a dry run',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  KEY `idx_network_id` (`network_id`),
  KEY `idx_vnf_appliance_id` (`vnf_appliance_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_drift_detected` (`drift_detected`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VNF network reconciliation audit log';

--
-- Table structure for VNF Broker audit trail
--
DROP TABLE IF EXISTS `cloud`.`vnf_broker_audit`;
CREATE TABLE `cloud`.`vnf_broker_audit` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `uuid` varchar(40) NOT NULL COMMENT 'UUID of the audit entry',
  `appliance_id` bigint unsigned DEFAULT NULL COMMENT 'VNF appliance ID',
  `operation` varchar(64) NOT NULL COMMENT 'Broker operation (POST /rules, GET /health, etc)',
  `request_payload` mediumtext COMMENT 'Request payload sent',
  `response_payload` mediumtext COMMENT 'Response payload received',
  `status_code` int DEFAULT NULL COMMENT 'HTTP status code',
  `error_message` text COMMENT 'Error message if failed',
  `latency_ms` int DEFAULT NULL COMMENT 'Latency in milliseconds',
  `created_at` datetime NOT NULL COMMENT 'When broker call was made',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uuid` (`uuid`),
  KEY `idx_appliance_id` (`appliance_id`),
  KEY `idx_operation` (`operation`),
  KEY `idx_status_code` (`status_code`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VNF broker API call audit trail';
