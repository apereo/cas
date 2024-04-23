package org.apereo.cas.zookeeper;

import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;
import org.apereo.cas.hz.HazelcastDiscoveryStrategy;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.zookeeper.ZookeeperDiscoveryProperties;
import com.hazelcast.zookeeper.ZookeeperDiscoveryStrategyFactory;
import lombok.val;

import java.util.HashMap;
import java.util.Optional;

/**
 * This is {@link HazelcastZooKeeperDiscoveryStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public class HazelcastZooKeeperDiscoveryStrategy implements HazelcastDiscoveryStrategy {
    @Override
    public Optional<DiscoveryStrategyConfig> get(final HazelcastClusterProperties cluster, final JoinConfig joinConfig,
                                                 final Config configuration, final NetworkConfig networkConfig) {
        val zk = cluster.getDiscovery().getZookeeper();
        val properties = new HashMap<String, Comparable>();
        properties.put(ZookeeperDiscoveryProperties.ZOOKEEPER_URL.key(), zk.getUrl());
        properties.put(ZookeeperDiscoveryProperties.ZOOKEEPER_PATH.key(), zk.getPath());
        properties.put(ZookeeperDiscoveryProperties.GROUP.key(), zk.getGroup());
        return Optional.of(new DiscoveryStrategyConfig(new ZookeeperDiscoveryStrategyFactory(), properties));
    }
}
