package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.ha.ClusterTopologyManager;
import org.apereo.cas.test.CasTestExtension;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HazelcastClusterTopologyManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 8.0.0
 */
@Tag("Hazelcast")
@ExtendWith(CasTestExtension.class)
@TestPropertySource(properties = "cas.ticket.registry.hazelcast.cluster.network.members=localhost")
class HazelcastClusterTopologyManagerTests extends BaseHazelcastTests {
    @Autowired
    @Qualifier("hazelcastClusterTopologyManager")
    private ClusterTopologyManager hazelcastClusterTopologyManager;

    @Test
    void verifyOperation() throws Exception {
        val results = hazelcastClusterTopologyManager.discoverMembers();
        assertFalse(results.isEmpty());
    }
}
