package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MemberAddressProviderConfig;
import com.hazelcast.config.NetworkConfig;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link HazelcastDockerSwarmDiscoveryStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Hazelcast")
public class HazelcastDockerSwarmDiscoveryStrategyTests {
    @Test
    public void verifyOperationDns() {
        val cluster = new HazelcastClusterProperties();
        val swarm = cluster.getDiscovery().getDockerSwarm();

        swarm.getDnsProvider().setEnabled(true);
        swarm.getDnsProvider().setPeerServices("apereo.org");
        swarm.getDnsProvider().setServiceName("google.com");
        swarm.getDnsProvider().setServicePort(1234);

        val hz = new HazelcastDockerSwarmDiscoveryStrategy();
        val networkConfig = mock(NetworkConfig.class);
        when(networkConfig.getMemberAddressProviderConfig()).thenReturn(new MemberAddressProviderConfig());
        val result = hz.get(cluster, mock(JoinConfig.class), mock(Config.class), networkConfig);
        assertNotNull(result);
    }

    @Test
    public void verifyOperationMembers() {
        val cluster = new HazelcastClusterProperties();
        val swarm = cluster.getDiscovery().getDockerSwarm();

        swarm.getMemberProvider().setEnabled(true);
        swarm.getMemberProvider().setDockerNetworkNames("network-names");
        swarm.getMemberProvider().setDockerServiceNames("service-names");
        swarm.getMemberProvider().setDockerServiceLabels("label-names");
        swarm.getMemberProvider().setSwarmMgrUri("https://swarm.uri");
        swarm.getMemberProvider().setHazelcastPeerPort(1234);


        val hz = new HazelcastDockerSwarmDiscoveryStrategy();
        val networkConfig = mock(NetworkConfig.class);
        when(networkConfig.getMemberAddressProviderConfig()).thenReturn(new MemberAddressProviderConfig());
        val result = hz.get(cluster, mock(JoinConfig.class), mock(Config.class), networkConfig);
        assertNotNull(result);
    }

}
