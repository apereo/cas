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
 * This is {@link HazelcastAzureDiscoveryStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Hazelcast")
public class HazelcastAzureDiscoveryStrategyTests {
    @Test
    public void verifyOperation() {
        val cluster = new HazelcastClusterProperties();
        val azure = cluster.getDiscovery().getAzure();
        val id = UUID.randomUUID().toString();
        azure.setClientId(id);
        azure.setClientSecret(id);
        azure.setClusterId(id);
        azure.setGroupName(id);
        azure.setSubscriptionId(id);
        azure.setTenantId(id);
        val hz = new HazelcastAzureDiscoveryStrategy();
        val result = hz.get(cluster, mock(JoinConfig.class), mock(Config.class), mock(NetworkConfig.class));
        assertNotNull(result);
    }
}
