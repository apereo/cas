package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link HazelcastJCloudsDiscoveryStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Hazelcast")
public class HazelcastJCloudsDiscoveryStrategyTests {
    @Test
    public void verifyOperation() {
        val cluster = new HazelcastClusterProperties();
        val clouds = cluster.getDiscovery().getJclouds();
        val id = UUID.randomUUID().toString();
        clouds.setCredential(id);
        clouds.setCredentialPath(id);
        clouds.setEndpoint(id);
        clouds.setGroup(id);
        clouds.setIdentity(id);
        clouds.setPort(1234);
        clouds.setProvider(id);
        clouds.setRegions(id);
        clouds.setRoleName(id);
        clouds.setTagKeys(id);
        clouds.setTagValues(id);
        clouds.setZones(id);
        val hz = new HazelcastJCloudsDiscoveryStrategy();
        val result = hz.get(cluster, mock(JoinConfig.class), mock(Config.class), mock(NetworkConfig.class));
        assertNotNull(result);
    }
}
