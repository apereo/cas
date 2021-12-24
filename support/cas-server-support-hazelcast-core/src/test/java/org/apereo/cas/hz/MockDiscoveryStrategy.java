package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;

import java.util.Optional;

import static org.mockito.Mockito.*;

/**
 * This is {@link MockDiscoveryStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class MockDiscoveryStrategy implements HazelcastDiscoveryStrategy {
    @Override
    public Optional<DiscoveryStrategyConfig> get(final HazelcastClusterProperties cluster,
                                                 final JoinConfig joinConfig, final Config configuration,
                                                 final NetworkConfig networkConfig) {
        return Optional.of(mock(DiscoveryStrategyConfig.class));
    }
}
