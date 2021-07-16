package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.BaseHazelcastProperties;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastWANReplicationTargetClusterProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HazelcastConfigurationFactoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Hazelcast")
public class HazelcastConfigurationFactoryTests {
    @Test
    public void verifyReplicationMaps() {
        val hz = new BaseHazelcastProperties();
        hz.getCluster().getCore().setReplicated(true);
        hz.getCluster().getCore().setPartitionMemberGroupType("ZONE_AWARE");
        val mapConfig = HazelcastConfigurationFactory.buildMapConfig(hz, "mapName", 10);
        assertNotNull(mapConfig);
        assertNotNull(HazelcastConfigurationFactory.build(hz, mapConfig));
    }

    @Test
    public void verifyMergePolicy() {
        val policies = new String[]{"discard", "pass_through", "expiration_time", "higher_hits",
            "latest_update", "latest_access", "put_if_absent", "other"};
        Arrays.stream(policies).forEach(policy -> {
            val hz = new BaseHazelcastProperties();
            hz.getCluster().getCore().setMapMergePolicy(policy);
            val mapConfig = HazelcastConfigurationFactory.buildMapConfig(hz, "mapName", 10);
            assertNotNull(mapConfig);
            assertNotNull(HazelcastConfigurationFactory.build(hz, mapConfig));
        });
    }

    @Test
    public void verifyLocalPublic() {
        val hz = new BaseHazelcastProperties();
        hz.getCluster().getNetwork().setLocalAddress("127.0.0.1");
        hz.getCluster().getNetwork().setPublicAddress("127.0.0.1");
        hz.getCluster().getNetwork().setNetworkInterfaces("127.0.0.1,*");
        hz.getCluster().getCore().setCpMemberCount(3);
        val result = HazelcastConfigurationFactory.build(hz);
        assertNotNull(result);
    }

    @Test
    public void verifyDefaultJoinConfig() {
        val hz = new BaseHazelcastProperties();
        hz.getCluster().getDiscovery().getMulticast().setEnabled(true);
        hz.getCluster().getDiscovery().getMulticast().setGroup("127.0.0.1");
        hz.getCluster().getDiscovery().getMulticast().setPort(8765);
        hz.getCluster().getDiscovery().getMulticast().setTrustedInterfaces("127.0.0.1");
        val result = HazelcastConfigurationFactory.build(hz);
        assertNotNull(result);
    }

    @Test
    public void verifyDiscoveryConfig() {
        val hz = new BaseHazelcastProperties();
        hz.getCluster().getDiscovery().setEnabled(true);
        val result = HazelcastConfigurationFactory.build(hz);
        assertNotNull(result);
    }

    @Test
    public void verifyWAN() {
        val hz = new BaseHazelcastProperties();
        hz.getCluster().getWanReplication().setEnabled(true);
        assertThrows(IllegalArgumentException.class, () -> HazelcastConfigurationFactory.build(hz));
        hz.getCore().setLicenseKey(UUID.randomUUID().toString());
        hz.getCluster().getWanReplication().getTargets()
            .add(new HazelcastWANReplicationTargetClusterProperties()
                .setClusterName(UUID.randomUUID().toString())
                .setEndpoints("127.0.0.1")
                .setPublisherId(UUID.randomUUID().toString()));
        val result = HazelcastConfigurationFactory.build(hz);
        assertNotNull(result);
    }
}
