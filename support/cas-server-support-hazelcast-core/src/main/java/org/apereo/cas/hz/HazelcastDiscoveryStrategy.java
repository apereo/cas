package org.apereo.cas.hz;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;

/**
 * This is {@link HazelcastDiscoveryStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface HazelcastDiscoveryStrategy {
    /**
     * Get discovery strategy config.
     *
     * @param cluster       the cluster
     * @param joinConfig    the join config
     * @param configuration the configuration
     * @param networkConfig the network config
     * @return the discovery strategy config
     */
    DiscoveryStrategyConfig get(HazelcastClusterProperties cluster, JoinConfig joinConfig, Config configuration, NetworkConfig networkConfig);
}
