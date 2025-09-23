package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;
import org.apereo.cas.test.CasTestExtension;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

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
@ExtendWith(CasTestExtension.class)
class HazelcastKubernetesDiscoveryStrategyTests {
    @Test
    void verifyOperation() {
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
        kb.setApiRetries(3);
        kb.setServicePort(1234);
        val hz = new HazelcastKubernetesDiscoveryStrategy();
        val result = hz.get(cluster, mock(JoinConfig.class), mock(Config.class), mock(NetworkConfig.class));

        assertNotNull(result);
        assertTrue(result.isPresent());
        val properties = result.get().getProperties();
        val configurationProperties = new HazelcastKubernetesDiscoveryStrategyFactory().getConfigurationProperties();
        for (val propertyDefinition : configurationProperties) {
            val value = properties.get(propertyDefinition.key());
            if (value == null) {
                assertTrue(propertyDefinition.optional(),
                    () -> "Property " + propertyDefinition.key() + " is not Optional and should be given");
            } else {
                assertDoesNotThrow(
                    () -> propertyDefinition.typeConverter().convert(value),
                    () -> "Property " + propertyDefinition.key() + " has invalid value '" + value + '\'');
            }
        }
    }
}
