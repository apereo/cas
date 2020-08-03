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
 * This is {@link HazelcastKubernetesDiscoveryStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Hazelcast")
public class HazelcastKubernetesDiscoveryStrategyTests {
    @Test
    public void verifyOperation() {
        val cluster = new HazelcastClusterProperties();
        val kb = cluster.getDiscovery().getKubernetes();
        val id = UUID.randomUUID().toString();
        kb.setApiToken(id);
        kb.setKubernetesMaster(id);
        kb.setNamespace(id);
        kb.setResolveNotReadyAddresses(true);
        kb.setServiceDns(id);
        kb.setServiceDnsTimeout(1000);
        kb.setServiceLabelName(id);
        kb.setServiceLabelValue(id);
        kb.setServiceName(id);
        val hz = new HazelcastKubernetesDiscoveryStrategy();
        val result = hz.get(cluster, mock(JoinConfig.class), mock(Config.class), mock(NetworkConfig.class));
        assertNotNull(result);
    }
}
