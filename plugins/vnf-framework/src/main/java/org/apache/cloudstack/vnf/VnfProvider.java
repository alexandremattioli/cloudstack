package org.apache.cloudstack.vnf;

import com.cloud.exception.CloudException;
import com.cloud.network.Network;

import com.cloud.network.element.NetworkElement;
import com.cloud.network.element.FirewallServiceProvider;
import com.cloud.network.element.PortForwardingServiceProvider;
import com.cloud.network.element.StaticNatServiceProvider;
import com.cloud.network.element.LoadBalancingServiceProvider;
public interface VnfProvider extends NetworkElement,
                                      FirewallServiceProvider,
                                      PortForwardingServiceProvider,
                                      StaticNatServiceProvider,
                                      LoadBalancingServiceProvider {
    /**
     * Check if this provider can handle the given network
     */
    boolean canHandle(Network network);
    /**
     * Deploy a VNF appliance for a network
     */
    VnfAppliance deployVnfAppliance(Network network, VnfTemplate template)
        throws CloudException;
    /**
     * Destroy a VNF appliance
     */
    boolean destroyVnfAppliance(VnfAppliance appliance)
        throws CloudException;
    /**
     * Get VNF appliance for a network
     */
    VnfAppliance getVnfApplianceForNetwork(long networkId);
    /**
     * Test connectivity to VNF device
     */
    VnfConnectivityResult testConnectivity(VnfAppliance appliance)
        throws CloudException;
    /**
     * Reconcile network state with VNF device
     */
    VnfReconciliationResult reconcileNetwork(Network network, boolean dryRun)
        throws CloudException;
}
/**
 * Dictionary management interface
 */