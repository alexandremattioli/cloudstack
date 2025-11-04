package org.apache.cloudstack.vnf.provider;

import com.cloud.agent.api.to.FirewallRuleTO;
import com.cloud.deploy.DeployDestination;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.network.NetworkModel;
import com.cloud.network.PhysicalNetworkServiceProvider;
import com.cloud.network.element.NetworkElement;
import com.cloud.network.element.FirewallServiceProvider;
import com.cloud.network.rules.FirewallRule;
import com.cloud.offering.NetworkOffering;
import com.cloud.vm.NicProfile;
import com.cloud.vm.ReservationContext;
import com.cloud.vm.VirtualMachineProfile;
import org.apache.cloudstack.vnf.client.VnfBrokerClient;
import org.apache.cloudstack.vnf.dao.VnfDeviceDao;
import org.apache.cloudstack.vnf.dao.VnfInstanceDao;
import org.apache.cloudstack.vnf.dao.VnfOperationDao;
import org.apache.cloudstack.vnf.entity.VnfDeviceVO;
import org.apache.cloudstack.vnf.entity.VnfInstanceVO;
import org.apache.cloudstack.vnf.entity.VnfOperationVO;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import java.util.List;

/**
 * NetworkElement provider for VNF Framework
 * Enables CloudStack to apply network rules to VNF appliances
 */
public class VnfNetworkElement implements NetworkElement, FirewallServiceProvider {
    private static final Logger LOGGER = Logger.getLogger(VnfNetworkElement.class);

    @Inject
    private VnfInstanceDao vnfInstanceDao;

    @Inject
    private VnfDeviceDao vnfDeviceDao;

    @Inject
    private VnfOperationDao vnfOperationDao;

    @Inject
    private NetworkModel networkModel;

    @Override
    public boolean implement(Network network, NetworkOffering offering, DeployDestination dest, 
                             ReservationContext context) throws ConcurrentOperationException, 
                             ResourceUnavailableException, InsufficientCapacityException {
        LOGGER.info("VNF NetworkElement implementing network: " + network.getId());
        
        // Check if VNF is configured for this network
        List<VnfDeviceVO> devices = vnfDeviceDao.listByNetworkId(network.getId());
        if (devices == null || devices.isEmpty()) {
            LOGGER.debug("No VNF devices configured for network " + network.getId());
            return false;
        }

        LOGGER.info("Found " + devices.size() + " VNF device(s) for network " + network.getId());
        return true;
    }

    @Override
    public boolean prepare(Network network, NicProfile nic, VirtualMachineProfile vm, 
                          DeployDestination dest, ReservationContext context) 
                          throws ConcurrentOperationException, ResourceUnavailableException, 
                          InsufficientCapacityException {
        LOGGER.debug("VNF NetworkElement preparing network: " + network.getId());
        return true;
    }

    @Override
    public boolean release(Network network, NicProfile nic, VirtualMachineProfile vm, 
                          ReservationContext context) throws ConcurrentOperationException, 
                          ResourceUnavailableException {
        LOGGER.debug("VNF NetworkElement releasing network: " + network.getId());
        return true;
    }

    @Override
    public boolean shutdown(Network network, ReservationContext context, boolean cleanup) 
                           throws ConcurrentOperationException, ResourceUnavailableException {
        LOGGER.info("VNF NetworkElement shutting down network: " + network.getId());
        return true;
    }

    @Override
    public boolean destroy(Network network, ReservationContext context) 
                          throws ConcurrentOperationException, ResourceUnavailableException {
        LOGGER.info("VNF NetworkElement destroying network: " + network.getId());
        
        // Clean up VNF devices associated with this network
        List<VnfDeviceVO> devices = vnfDeviceDao.listByNetworkId(network.getId());
        if (devices != null) {
            for (VnfDeviceVO device : devices) {
                LOGGER.info("Removing VNF device " + device.getUuid() + " from network " + network.getId());
                vnfDeviceDao.remove(device.getId());
            }
        }
        
        return true;
    }

    @Override
    public boolean isReady(PhysicalNetworkServiceProvider provider) {
        return true;
    }

    @Override
    public boolean shutdownProviderInstances(PhysicalNetworkServiceProvider provider, 
                                             ReservationContext context) 
                                             throws ConcurrentOperationException, 
                                             ResourceUnavailableException {
        return true;
    }

    @Override
    public boolean canEnableIndividualServices() {
        return true;
    }

    @Override
    public boolean verifyServicesCombination(List<String> services) {
        // VNF can provide Firewall, SourceNat, PortForwarding, StaticNat, Vpn
        return true;
    }

    @Override
    public boolean applyFWRules(Network network, List<? extends FirewallRule> rules) 
                                throws ResourceUnavailableException {
        LOGGER.info("VNF NetworkElement applying " + rules.size() + " firewall rules to network " + network.getId());
        
        if (rules == null || rules.isEmpty()) {
            return true;
        }

        // Get VNF device for this network
        List<VnfDeviceVO> devices = vnfDeviceDao.listByNetworkId(network.getId());
        if (devices == null || devices.isEmpty()) {
            LOGGER.warn("No VNF device found for network " + network.getId());
            return false;
        }

        VnfDeviceVO device = devices.get(0); // Use first device
        VnfInstanceVO vnfInstance = vnfInstanceDao.findById(device.getVnfInstanceId());
        
        if (vnfInstance == null) {
            LOGGER.error("VNF instance not found: " + device.getVnfInstanceId());
            return false;
        }

        try {
            VnfBrokerClient client = new VnfBrokerClient(
                device.getBrokerUrl(),
                extractJwtToken(device.getApiCredentials())
            );

            for (FirewallRule rule : rules) {
                applyFirewallRule(client, vnfInstance, rule);
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to apply firewall rules: " + e.getMessage(), e);
            throw new ResourceUnavailableException("Failed to apply VNF firewall rules", 
                                                   Network.class, network.getId());
        }
    }

    /**
     * Apply a single firewall rule to VNF
     */
    private void applyFirewallRule(VnfBrokerClient client, VnfInstanceVO vnfInstance, 
                                   FirewallRule rule) throws Exception {
        String ruleId = rule.getUuid();
        
        // Check if already applied (idempotency)
        VnfOperationVO existingOp = vnfOperationDao.findByRuleId(ruleId);
        if (existingOp != null && existingOp.getState().equals(VnfOperationVO.State.Completed.toString())) {
            LOGGER.debug("Firewall rule " + ruleId + " already applied");
            return;
        }

        // Create operation record
        VnfOperationVO operation = new VnfOperationVO(
            vnfInstance.getId(),
            VnfOperationVO.OperationType.CREATE_FIREWALL_RULE.toString(),
            ruleId,
            computeRuleHash(rule)
        );
        operation.setState(VnfOperationVO.State.Pending.toString());
        operation = vnfOperationDao.persist(operation);

        try {
            // Build request
            VnfBrokerClient.CreateFirewallRuleRequest request = new VnfBrokerClient.CreateFirewallRuleRequest();
            request.setRuleId(ruleId);
            request.setAction(rule.getPurpose() == FirewallRule.Purpose.Firewall ? "allow" : "deny");
            request.setProtocol(rule.getProtocol() != null ? rule.getProtocol().toLowerCase() : "any");
            request.setSourceAddressing(formatCidrList(rule.getSourceCidrList()));
            request.setDestinationAddressing(formatCidrList(rule.getDestinationCidrList()));
            request.setSourcePorts(formatPortRange(rule.getSourcePortStart(), rule.getSourcePortEnd()));
            request.setDestinationPorts(formatPortRange(rule.getSourcePortStart(), rule.getSourcePortEnd()));

            // Execute
            operation.setState(VnfOperationVO.State.InProgress.toString());
            vnfOperationDao.update(operation.getId(), operation);

            VnfBrokerClient.VnfOperationResponse response = client.createFirewallRule(request);

            // Update success
            operation.setState(VnfOperationVO.State.Completed.toString());
            operation.setVendorRef(response.getVendorRef());
            operation.setCompletedAt(new java.util.Date());
            vnfOperationDao.update(operation.getId(), operation);

            LOGGER.info("Applied firewall rule " + ruleId + " to VNF, vendor ref: " + response.getVendorRef());

        } catch (VnfBrokerClient.VnfBrokerException e) {
            operation.setState(VnfOperationVO.State.Failed.toString());
            operation.setErrorCode(e.getErrorCode());
            operation.setErrorMessage(e.getMessage());
            operation.setCompletedAt(new java.util.Date());
            vnfOperationDao.update(operation.getId(), operation);
            throw e;
        }
    }

    /**
     * Format CIDR list for addressing
     */
    private String formatCidrList(List<String> cidrList) {
        if (cidrList == null || cidrList.isEmpty()) {
            return "any";
        }
        return String.join(",", cidrList);
    }

    /**
     * Format port range
     */
    private String formatPortRange(Integer startPort, Integer endPort) {
        if (startPort == null) {
            return "any";
        }
        if (endPort == null || startPort.equals(endPort)) {
            return startPort.toString();
        }
        return startPort + "-" + endPort;
    }

    /**
     * Compute hash of firewall rule for deduplication
     */
    private String computeRuleHash(FirewallRule rule) {
        try {
            String canonical = String.format("%s:%s:%s:%s:%s:%s",
                rule.getPurpose(),
                rule.getProtocol(),
                formatCidrList(rule.getSourceCidrList()),
                formatCidrList(rule.getDestinationCidrList()),
                formatPortRange(rule.getSourcePortStart(), rule.getSourcePortEnd()),
                formatPortRange(rule.getSourcePortStart(), rule.getSourcePortEnd())
            );

            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            LOGGER.error("Failed to compute rule hash", e);
            return java.util.UUID.randomUUID().toString();
        }
    }

    /**
     * Extract JWT token from credentials JSON
     */
    private String extractJwtToken(String apiCredentials) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> creds = mapper.readValue(apiCredentials, java.util.Map.class);
            return (String) creds.get("jwtToken");
        } catch (Exception e) {
            LOGGER.error("Failed to parse API credentials", e);
            return null;
        }
    }
}
