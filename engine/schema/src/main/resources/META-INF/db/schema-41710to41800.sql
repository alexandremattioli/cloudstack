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
-- Schema upgrade from 4.17.1.0 to 4.18.0.0
--;
-- Enable CPU cap for default system offerings;
UPDATE `cloud`.`service_offering` so
SET so.limit_cpu_use = 1
WHERE so.default_use = 1 AND so.vm_type IN ('domainrouter', 'secondarystoragevm', 'consoleproxy', 'internalloadbalancervm', 'elasticloadbalancervm');

-- Add cidr_list column to load_balancing_rules
ALTER TABLE `cloud`.`load_balancing_rules`
ADD cidr_list VARCHAR(4096);

-- savely add resources in parallel
-- PR#5984 Create table to persist VM stats.
DROP TABLE IF EXISTS `cloud`.`resource_reservation`;
CREATE TABLE `cloud`.`resource_reservation` (
  `id` bigint unsigned NOT NULL auto_increment COMMENT 'id',
  `account_id` bigint unsigned NOT NULL,
  `domain_id` bigint unsigned NOT NULL,
  `resource_type` varchar(255) NOT NULL,
  `amount` bigint unsigned NOT NULL,
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Alter networks table to add ip6dns1 and ip6dns2
ALTER TABLE `cloud`.`networks`
    ADD COLUMN `ip6dns1` varchar(255) DEFAULT NULL COMMENT 'first IPv6 DNS for the network' AFTER `dns2`,
    ADD COLUMN `ip6dns2` varchar(255) DEFAULT NULL COMMENT 'second IPv6 DNS for the network' AFTER `ip6dns1`;
-- Alter vpc table to add dns1, dns2, ip6dns1 and ip6dns2
ALTER TABLE `cloud`.`vpc`
    ADD COLUMN `dns1` varchar(255) DEFAULT NULL COMMENT 'first IPv4 DNS for the vpc' AFTER `network_domain`,
    ADD COLUMN `dns2` varchar(255) DEFAULT NULL COMMENT 'second IPv4 DNS for the vpc' AFTER `dns1`,
    ADD COLUMN `ip6dns1` varchar(255) DEFAULT NULL COMMENT 'first IPv6 DNS for the vpc' AFTER `dns2`,
    ADD COLUMN `ip6dns2` varchar(255) DEFAULT NULL COMMENT 'second IPv6 DNS for the vpc' AFTER `ip6dns1`;

-- VM autoscaling

-- Idempotent ADD COLUMN
DROP PROCEDURE IF EXISTS `cloud`.`IDEMPOTENT_ADD_COLUMN`;
CREATE PROCEDURE `cloud`.`IDEMPOTENT_ADD_COLUMN` (
    IN in_table_name VARCHAR(200)
, IN in_column_name VARCHAR(200)
, IN in_column_definition VARCHAR(1000)
)
BEGIN
    DECLARE CONTINUE HANDLER FOR 1060 BEGIN END; SET @ddl = CONCAT('ALTER TABLE ', in_table_name); SET @ddl = CONCAT(@ddl, ' ', 'ADD COLUMN') ; SET @ddl = CONCAT(@ddl, ' ', in_column_name); SET @ddl = CONCAT(@ddl, ' ', in_column_definition); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt; END;

-- Idempotent RENAME COLUMN
DROP PROCEDURE IF EXISTS `cloud`.`IDEMPOTENT_CHANGE_COLUMN`;
CREATE PROCEDURE `cloud`.`IDEMPOTENT_CHANGE_COLUMN` (
    IN in_table_name VARCHAR(200)
, IN in_column_name VARCHAR(200)
, IN in_column_new_name VARCHAR(200)
, IN in_column_new_definition VARCHAR(1000)
)
BEGIN
    DECLARE CONTINUE HANDLER FOR 1060 BEGIN END; SET @ddl = CONCAT('ALTER TABLE ', in_table_name); SET @ddl = CONCAT(@ddl, ' ', 'CHANGE COLUMN') ; SET @ddl = CONCAT(@ddl, ' ', in_column_name); SET @ddl = CONCAT(@ddl, ' ', in_column_new_name); SET @ddl = CONCAT(@ddl, ' ', in_column_new_definition); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt; END;

-- Idempotent ADD UNIQUE KEY
DROP PROCEDURE IF EXISTS `cloud`.`IDEMPOTENT_ADD_UNIQUE_KEY`;
CREATE PROCEDURE `cloud`.`IDEMPOTENT_ADD_UNIQUE_KEY` (
    IN in_table_name VARCHAR(200)
, IN in_key_name VARCHAR(200)
, IN in_key_definition VARCHAR(1000)
)
BEGIN
    DECLARE CONTINUE HANDLER FOR 1061 BEGIN END; SET @ddl = CONCAT('ALTER TABLE ', in_table_name); SET @ddl = CONCAT(@ddl, ' ', 'ADD UNIQUE KEY ', in_key_name); SET @ddl = CONCAT(@ddl, ' ', in_key_definition); PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt; END;

-- Add column 'name' to 'autoscale_vmgroups' table
CALL `cloud`.`IDEMPOTENT_ADD_COLUMN`('cloud.autoscale_vmgroups', 'name', 'VARCHAR(255) DEFAULT NULL COMMENT "name of the autoscale vm group" AFTER `load_balancer_id`');
UPDATE `cloud`.`autoscale_vmgroups` SET `name` = CONCAT('AutoScale-VmGroup-',id) WHERE `name` IS NULL;

-- Add column 'user_data' to 'autoscale_vmprofiles' table
CALL `cloud`.`IDEMPOTENT_ADD_COLUMN`('cloud.autoscale_vmprofiles', 'user_data', 'TEXT(32768) AFTER `counter_params`');

-- Add column 'name' to 'autoscale_policies' table
CALL `cloud`.`IDEMPOTENT_ADD_COLUMN`('cloud.autoscale_policies', 'name', 'VARCHAR(255) DEFAULT NULL COMMENT "name of the autoscale policy" AFTER `uuid`');

-- Add column 'provider' and update values
CALL `cloud`.`IDEMPOTENT_ADD_COLUMN`('cloud.counter', 'provider', 'VARCHAR(255) NOT NULL COMMENT "Network provider name" AFTER `uuid`');
UPDATE `cloud`.`counter` SET provider = 'Netscaler' WHERE `provider` IS NULL OR `provider` = '';

CALL `cloud`.`IDEMPOTENT_ADD_UNIQUE_KEY`('cloud.counter', 'uc_counter__provider__source__value', '(provider, source, value)');

-- Add new counters for VM autoscaling

INSERT IGNORE INTO `cloud`.`counter` (uuid, provider, source, name, value, created) VALUES (UUID(), 'VirtualRouter', 'cpu', 'VM CPU - average percentage', 'vm.cpu.average.percentage', NOW());
INSERT IGNORE INTO `cloud`.`counter` (uuid, provider, source, name, value, created) VALUES (UUID(), 'VirtualRouter', 'memory', 'VM Memory - average percentage', 'vm.memory.average.percentage', NOW());
INSERT IGNORE INTO `cloud`.`counter` (uuid, provider, source, name, value, created) VALUES (UUID(), 'VirtualRouter', 'virtualrouter', 'Virtual Network - Received per vm (in Bytes per second)', 'virtual.network.received.average.Bps', NOW());
INSERT IGNORE INTO `cloud`.`counter` (uuid, provider, source, name, value, created) VALUES (UUID(), 'VirtualRouter', 'virtualrouter', 'Virtual Network - Transmit per vm (in Bytes per second)', 'virtual.network.transmit.average.Bps', NOW());
INSERT IGNORE INTO `cloud`.`counter` (uuid, provider, source, name, value, created) VALUES (UUID(), 'VirtualRouter', 'virtualrouter', 'Load Balancer - average connections per vm', 'virtual.network.lb.average.connections', NOW());

INSERT IGNORE INTO `cloud`.`counter` (uuid, provider, source, name, value, created) VALUES (UUID(), 'VpcVirtualRouter', 'cpu', 'VM CPU - average percentage', 'vm.cpu.average.percentage', NOW());
INSERT IGNORE INTO `cloud`.`counter` (uuid, provider, source, name, value, created) VALUES (UUID(), 'VpcVirtualRouter', 'memory', 'VM Memory - average percentage', 'vm.memory.average.percentage', NOW());
INSERT IGNORE INTO `cloud`.`counter` (uuid, provider, source, name, value, created) VALUES (UUID(), 'VpcVirtualRouter', 'virtualrouter', 'Virtual Network - Received (in Bytes per second)', 'virtual.network.received.Bps', NOW());
INSERT IGNORE INTO `cloud`.`counter` (uuid, provider, source, name, value, created) VALUES (UUID(), 'VpcVirtualRouter', 'virtualrouter', 'Virtual Network - Transmit (in Bytes per second)', 'virtual.network.transmit.Bps', NOW());
INSERT IGNORE INTO `cloud`.`counter` (uuid, provider, source, name, value, created) VALUES (UUID(), 'VpcVirtualRouter', 'virtualrouter', 'Load Balancer - average connections per vm', 'virtual.network.lb.average.connections', NOW());

INSERT IGNORE INTO `cloud`.`counter` (uuid, provider, source, name, value, created) VALUES (UUID(), 'None', 'cpu', 'VM CPU - average percentage', 'vm.cpu.average.percentage', NOW());
INSERT IGNORE INTO `cloud`.`counter` (uuid, provider, source, name, value, created) VALUES (UUID(), 'None', 'memory', 'VM Memory - average percentage', 'vm.memory.average.percentage', NOW());

-- Update autoscale_vmgroups to new state

UPDATE `cloud`.`autoscale_vmgroups` SET state= UPPER(state);

-- Update autoscale_vmgroups so records will not be removed when LB rule is removed

ALTER TABLE `cloud`.`autoscale_vmgroups` DROP FOREIGN KEY `fk_autoscale_vmgroup__load_balancer_id`;

-- Update autoscale_vmprofiles to make autoscale_user_id optional

ALTER TABLE `cloud`.`autoscale_vmprofiles` MODIFY COLUMN `autoscale_user_id` bigint unsigned;

-- Update autoscale_vmprofiles to rename destroy_vm_grace_period

CALL `cloud`.`IDEMPOTENT_CHANGE_COLUMN`('cloud.autoscale_vmprofiles', 'destroy_vm_grace_period', 'expunge_vm_grace_period', 'int unsigned COMMENT "the time allowed for existing connections to get closed before a vm is expunged"');

-- Create table for VM autoscaling historic data

CREATE TABLE IF NOT EXISTS `cloud`.`autoscale_vmgroup_statistics` (
  `id` bigint unsigned NOT NULL auto_increment,
  `vmgroup_id` bigint unsigned NOT NULL,
  `policy_id` bigint unsigned NOT NULL,
  `counter_id` bigint unsigned NOT NULL,
  `resource_id` bigint unsigned DEFAULT NULL,
  `resource_type` varchar(255) NOT NULL,
  `raw_value` double NOT NULL,
  `value_type` varchar(255) NOT NULL,
  `created` datetime NOT NULL COMMENT 'Date this data is created',
  `state` varchar(255) NOT NULL COMMENT 'State of the data',
  PRIMARY KEY  (`id`),
  CONSTRAINT `fk_autoscale_vmgroup_statistics__vmgroup_id` FOREIGN KEY `fk_autoscale_vmgroup_statistics__vmgroup_id` (`vmgroup_id`) REFERENCES `autoscale_vmgroups` (`id`) ON DELETE CASCADE,
  INDEX `i_autoscale_vmgroup_statistics__vmgroup_id`(`vmgroup_id`),
  INDEX `i_autoscale_vmgroup_statistics__policy_id`(`policy_id`),
  INDEX `i_autoscale_vmgroup_statistics__counter_id`(`counter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Update user vm view to include autoscalie vm group id
DROP VIEW IF EXISTS `cloud`.`user_vm_view`;
CREATE
    VIEW `user_vm_view` AS
SELECT
    `vm_instance`.`id` AS `id`,
    `vm_instance`.`name` AS `name`,
    `user_vm`.`display_name` AS `display_name`,
    `user_vm`.`user_data` AS `user_data`,
    `account`.`id` AS `account_id`,
    `account`.`uuid` AS `account_uuid`,
    `account`.`account_name` AS `account_name`,
    `account`.`type` AS `account_type`,
    `domain`.`id` AS `domain_id`,
    `domain`.`uuid` AS `domain_uuid`,
    `domain`.`name` AS `domain_name`,
    `domain`.`path` AS `domain_path`,
    `projects`.`id` AS `project_id`,
    `projects`.`uuid` AS `project_uuid`,
    `projects`.`name` AS `project_name`,
    `instance_group`.`id` AS `instance_group_id`,
    `instance_group`.`uuid` AS `instance_group_uuid`,
    `instance_group`.`name` AS `instance_group_name`,
    `vm_instance`.`uuid` AS `uuid`,
    `vm_instance`.`user_id` AS `user_id`,
    `vm_instance`.`last_host_id` AS `last_host_id`,
    `vm_instance`.`vm_type` AS `type`,
    `vm_instance`.`limit_cpu_use` AS `limit_cpu_use`,
    `vm_instance`.`created` AS `created`,
    `vm_instance`.`state` AS `state`,
    `vm_instance`.`update_time` AS `update_time`,
    `vm_instance`.`removed` AS `removed`,
    `vm_instance`.`ha_enabled` AS `ha_enabled`,
    `vm_instance`.`hypervisor_type` AS `hypervisor_type`,
    `vm_instance`.`instance_name` AS `instance_name`,
    `vm_instance`.`guest_os_id` AS `guest_os_id`,
    `vm_instance`.`display_vm` AS `display_vm`,
    `guest_os`.`uuid` AS `guest_os_uuid`,
    `vm_instance`.`pod_id` AS `pod_id`,
    `host_pod_ref`.`uuid` AS `pod_uuid`,
    `vm_instance`.`private_ip_address` AS `private_ip_address`,
    `vm_instance`.`private_mac_address` AS `private_mac_address`,
    `vm_instance`.`vm_type` AS `vm_type`,
    `data_center`.`id` AS `data_center_id`,
    `data_center`.`uuid` AS `data_center_uuid`,
    `data_center`.`name` AS `data_center_name`,
    `data_center`.`is_security_group_enabled` AS `security_group_enabled`,
    `data_center`.`networktype` AS `data_center_type`,
    `host`.`id` AS `host_id`,
    `host`.`uuid` AS `host_uuid`,
    `host`.`name` AS `host_name`,
    `host`.`cluster_id` AS `cluster_id`,
    `vm_template`.`id` AS `template_id`,
    `vm_template`.`uuid` AS `template_uuid`,
    `vm_template`.`name` AS `template_name`,
    `vm_template`.`display_text` AS `template_display_text`,
    `vm_template`.`enable_password` AS `password_enabled`,
    `iso`.`id` AS `iso_id`,
    `iso`.`uuid` AS `iso_uuid`,
    `iso`.`name` AS `iso_name`,
    `iso`.`display_text` AS `iso_display_text`,
    `service_offering`.`id` AS `service_offering_id`,
    `service_offering`.`uuid` AS `service_offering_uuid`,
    `disk_offering`.`uuid` AS `disk_offering_uuid`,
    `disk_offering`.`id` AS `disk_offering_id`,
    (CASE
         WHEN ISNULL(`service_offering`.`cpu`) THEN `custom_cpu`.`value`
         ELSE `service_offering`.`cpu`
        END) AS `cpu`,
    (CASE
         WHEN ISNULL(`service_offering`.`speed`) THEN `custom_speed`.`value`
         ELSE `service_offering`.`speed`
        END) AS `speed`,
    (CASE
         WHEN ISNULL(`service_offering`.`ram_size`) THEN `custom_ram_size`.`value`
         ELSE `service_offering`.`ram_size`
        END) AS `ram_size`,
    `backup_offering`.`uuid` AS `backup_offering_uuid`,
    `backup_offering`.`id` AS `backup_offering_id`,
    `service_offering`.`name` AS `service_offering_name`,
    `disk_offering`.`name` AS `disk_offering_name`,
    `backup_offering`.`name` AS `backup_offering_name`,
    `storage_pool`.`id` AS `pool_id`,
    `storage_pool`.`uuid` AS `pool_uuid`,
    `storage_pool`.`pool_type` AS `pool_type`,
    `volumes`.`id` AS `volume_id`,
    `volumes`.`uuid` AS `volume_uuid`,
    `volumes`.`device_id` AS `volume_device_id`,
    `volumes`.`volume_type` AS `volume_type`,
    `security_group`.`id` AS `security_group_id`,
    `security_group`.`uuid` AS `security_group_uuid`,
    `security_group`.`name` AS `security_group_name`,
    `security_group`.`description` AS `security_group_description`,
    `nics`.`id` AS `nic_id`,
    `nics`.`uuid` AS `nic_uuid`,
    `nics`.`device_id` AS `nic_device_id`,
    `nics`.`network_id` AS `network_id`,
    `nics`.`ip4_address` AS `ip_address`,
    `nics`.`ip6_address` AS `ip6_address`,
    `nics`.`ip6_gateway` AS `ip6_gateway`,
    `nics`.`ip6_cidr` AS `ip6_cidr`,
    `nics`.`default_nic` AS `is_default_nic`,
    `nics`.`gateway` AS `gateway`,
    `nics`.`netmask` AS `netmask`,
    `nics`.`mac_address` AS `mac_address`,
    `nics`.`broadcast_uri` AS `broadcast_uri`,
    `nics`.`isolation_uri` AS `isolation_uri`,
    `vpc`.`id` AS `vpc_id`,
    `vpc`.`uuid` AS `vpc_uuid`,
    `networks`.`uuid` AS `network_uuid`,
    `networks`.`name` AS `network_name`,
    `networks`.`traffic_type` AS `traffic_type`,
    `networks`.`guest_type` AS `guest_type`,
    `user_ip_address`.`id` AS `public_ip_id`,
    `user_ip_address`.`uuid` AS `public_ip_uuid`,
    `user_ip_address`.`public_ip_address` AS `public_ip_address`,
    `ssh_details`.`value` AS `keypair_names`,
    `resource_tags`.`id` AS `tag_id`,
    `resource_tags`.`uuid` AS `tag_uuid`,
    `resource_tags`.`key` AS `tag_key`,
    `resource_tags`.`value` AS `tag_value`,
    `resource_tags`.`domain_id` AS `tag_domain_id`,
    `domain`.`uuid` AS `tag_domain_uuid`,
    `domain`.`name` AS `tag_domain_name`,
    `resource_tags`.`account_id` AS `tag_account_id`,
    `account`.`account_name` AS `tag_account_name`,
    `resource_tags`.`resource_id` AS `tag_resource_id`,
    `resource_tags`.`resource_uuid` AS `tag_resource_uuid`,
    `resource_tags`.`resource_type` AS `tag_resource_type`,
    `resource_tags`.`customer` AS `tag_customer`,
    `async_job`.`id` AS `job_id`,
    `async_job`.`uuid` AS `job_uuid`,
    `async_job`.`job_status` AS `job_status`,
    `async_job`.`account_id` AS `job_account_id`,
    `affinity_group`.`id` AS `affinity_group_id`,
    `affinity_group`.`uuid` AS `affinity_group_uuid`,
    `affinity_group`.`name` AS `affinity_group_name`,
    `affinity_group`.`description` AS `affinity_group_description`,
    `autoscale_vmgroups`.`id` AS `autoscale_vmgroup_id`,
    `autoscale_vmgroups`.`uuid` AS `autoscale_vmgroup_uuid`,
    `autoscale_vmgroups`.`name` AS `autoscale_vmgroup_name`,
    `vm_instance`.`dynamically_scalable` AS `dynamically_scalable`
FROM
    ((((((((((((((((((((((((((((((((((`user_vm`
        JOIN `vm_instance` ON (((`vm_instance`.`id` = `user_vm`.`id`)
            AND ISNULL(`vm_instance`.`removed`))))
        JOIN `account` ON ((`vm_instance`.`account_id` = `account`.`id`)))
        JOIN `domain` ON ((`vm_instance`.`domain_id` = `domain`.`id`)))
        LEFT JOIN `guest_os` ON ((`vm_instance`.`guest_os_id` = `guest_os`.`id`)))
        LEFT JOIN `host_pod_ref` ON ((`vm_instance`.`pod_id` = `host_pod_ref`.`id`)))
        LEFT JOIN `projects` ON ((`projects`.`project_account_id` = `account`.`id`)))
        LEFT JOIN `instance_group_vm_map` ON ((`vm_instance`.`id` = `instance_group_vm_map`.`instance_id`)))
        LEFT JOIN `instance_group` ON ((`instance_group_vm_map`.`group_id` = `instance_group`.`id`)))
        LEFT JOIN `data_center` ON ((`vm_instance`.`data_center_id` = `data_center`.`id`)))
        LEFT JOIN `host` ON ((`vm_instance`.`host_id` = `host`.`id`)))
        LEFT JOIN `vm_template` ON ((`vm_instance`.`vm_template_id` = `vm_template`.`id`)))
        LEFT JOIN `vm_template` `iso` ON ((`iso`.`id` = `user_vm`.`iso_id`)))
        LEFT JOIN `volumes` ON ((`vm_instance`.`id` = `volumes`.`instance_id`)))
        LEFT JOIN `service_offering` ON ((`vm_instance`.`service_offering_id` = `service_offering`.`id`)))
        LEFT JOIN `disk_offering` `svc_disk_offering` ON ((`volumes`.`disk_offering_id` = `svc_disk_offering`.`id`)))
        LEFT JOIN `disk_offering` ON ((`volumes`.`disk_offering_id` = `disk_offering`.`id`)))
        LEFT JOIN `backup_offering` ON ((`vm_instance`.`backup_offering_id` = `backup_offering`.`id`)))
        LEFT JOIN `storage_pool` ON ((`volumes`.`pool_id` = `storage_pool`.`id`)))
        LEFT JOIN `security_group_vm_map` ON ((`vm_instance`.`id` = `security_group_vm_map`.`instance_id`)))
        LEFT JOIN `security_group` ON ((`security_group_vm_map`.`security_group_id` = `security_group`.`id`)))
        LEFT JOIN `nics` ON (((`vm_instance`.`id` = `nics`.`instance_id`)
        AND ISNULL(`nics`.`removed`))))
        LEFT JOIN `networks` ON ((`nics`.`network_id` = `networks`.`id`)))
        LEFT JOIN `vpc` ON (((`networks`.`vpc_id` = `vpc`.`id`)
        AND ISNULL(`vpc`.`removed`))))
        LEFT JOIN `user_ip_address` ON ((`user_ip_address`.`vm_id` = `vm_instance`.`id`)))
        LEFT JOIN `user_vm_details` `ssh_details` ON (((`ssh_details`.`vm_id` = `vm_instance`.`id`)
        AND (`ssh_details`.`name` = 'SSH.KeyPairNames'))))
        LEFT JOIN `resource_tags` ON (((`resource_tags`.`resource_id` = `vm_instance`.`id`)
        AND (`resource_tags`.`resource_type` = 'UserVm'))))
        LEFT JOIN `async_job` ON (((`async_job`.`instance_id` = `vm_instance`.`id`)
        AND (`async_job`.`instance_type` = 'VirtualMachine')
        AND (`async_job`.`job_status` = 0))))
        LEFT JOIN `affinity_group_vm_map` ON ((`vm_instance`.`id` = `affinity_group_vm_map`.`instance_id`)))
        LEFT JOIN `affinity_group` ON ((`affinity_group_vm_map`.`affinity_group_id` = `affinity_group`.`id`)))
        LEFT JOIN `autoscale_vmgroup_vm_map` ON ((`autoscale_vmgroup_vm_map`.`instance_id` = `vm_instance`.`id`)))
        LEFT JOIN `autoscale_vmgroups` ON ((`autoscale_vmgroup_vm_map`.`vmgroup_id` = `autoscale_vmgroups`.`id`)))
        LEFT JOIN `user_vm_details` `custom_cpu` ON (((`custom_cpu`.`vm_id` = `vm_instance`.`id`)
        AND (`custom_cpu`.`name` = 'CpuNumber'))))
        LEFT JOIN `user_vm_details` `custom_speed` ON (((`custom_speed`.`vm_id` = `vm_instance`.`id`)
        AND (`custom_speed`.`name` = 'CpuSpeed'))))
        LEFT JOIN `user_vm_details` `custom_ram_size` ON (((`custom_ram_size`.`vm_id` = `vm_instance`.`id`)
        AND (`custom_ram_size`.`name` = 'memory'))));
