package org.apereo.cas.zookeeper;

import module java.base;
import org.apereo.cas.configuration.model.support.hazelcast.HazelcastClusterProperties;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link HazelcastZooKeeperDiscoveryStrategyTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("ZooKeeper")
@EnabledIfListeningOnPort(port = 2181)
class HazelcastZooKeeperDiscoveryStrategyTests {
    @Test
    void verifyOperation() {
        val cluster = new HazelcastClusterProperties();
        val zk = cluster.getDiscovery().getZookeeper();
        zk.setUrl("localhost:2181");
        zk.setGroup("clusterId");
        zk.setPath("/hazelcast");
        val hz = new HazelcastZooKeeperDiscoveryStrategy();
        val result = hz.get(cluster, mock(JoinConfig.class), mock(Config.class), mock(NetworkConfig.class));
        assertNotNull(result);
    }
}
