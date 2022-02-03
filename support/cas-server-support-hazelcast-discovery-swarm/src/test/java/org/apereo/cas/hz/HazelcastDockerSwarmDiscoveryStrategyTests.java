package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MemberAddressProviderConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.properties.PropertyDefinition;
import lombok.val;
import org.bitsofinfo.hazelcast.discovery.docker.swarm.DockerSwarmDiscoveryStrategyFactory;
import org.bitsofinfo.hazelcast.spi.docker.swarm.dnsrr.discovery.DockerDNSRRDiscoveryStrategyFactory;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link HazelcastDockerSwarmDiscoveryStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Hazelcast")
@Isolated
public class HazelcastDockerSwarmDiscoveryStrategyTests {
    private static void assertAllPropsAreValid(final Map<String, Comparable> properties,
                                               final Collection<PropertyDefinition> configurationProperties) {
        for (val propertyDefinition : configurationProperties) {
            val value = properties.get(propertyDefinition.key());
            if (value == null) {
                assertTrue(propertyDefinition.optional(),
                    () -> "Property " + propertyDefinition.key() + " is not optional and should be given");
            } else {
                assertDoesNotThrow(
                    () -> propertyDefinition.typeConverter().convert(value),
                    () -> "Property " + propertyDefinition.key() + " has invalid value '" + value + '\'');
            }
        }
    }

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
        assertTrue(result.isPresent());

        val configurationProperties = new DockerDNSRRDiscoveryStrategyFactory().getConfigurationProperties();
        assertAllPropsAreValid(result.get().getProperties(), configurationProperties);
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

        val hazelcastPeerPortProperty = "hazelcastPeerPort";
        val origHazelcastPeerPort = System.getProperty(hazelcastPeerPortProperty);
        try {
            System.setProperty(hazelcastPeerPortProperty, "5601");
            val hz = new HazelcastDockerSwarmDiscoveryStrategy();
            val networkConfig = mock(NetworkConfig.class);
            when(networkConfig.getMemberAddressProviderConfig()).thenReturn(new MemberAddressProviderConfig());
            val result = hz.get(cluster, mock(JoinConfig.class), mock(Config.class), networkConfig);
            assertNotNull(result);
            assertTrue(result.isPresent());

            val configurationProperties = new DockerSwarmDiscoveryStrategyFactory().getConfigurationProperties();
            assertAllPropsAreValid(result.get().getProperties(), configurationProperties);
        } finally {
            if (origHazelcastPeerPort == null) {
                System.clearProperty(hazelcastPeerPortProperty);
            } else {
                System.setProperty(hazelcastPeerPortProperty, origHazelcastPeerPort);
            }
        }
    }

}
