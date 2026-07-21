package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.ha.ClusterTopologyManager;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link RedisTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Redis")
@EnabledIfListeningOnPort(port = {7001, 7002, 7003, 7004, 7005, 7006})
class RedisServerClusteredTicketRegistryTests {
    @Nested
    @TestPropertySource(properties = {
        "cas.ticket.registry.redis.cluster.nodes[0].host=localhost",
        "cas.ticket.registry.redis.cluster.nodes[0].port=7001",
        "cas.ticket.registry.redis.cluster.nodes[0].type=MASTER",

        "cas.ticket.registry.redis.cluster.nodes[1].host=localhost",
        "cas.ticket.registry.redis.cluster.nodes[1].port=7002",
        "cas.ticket.registry.redis.cluster.nodes[1].type=MASTER",

        "cas.ticket.registry.redis.cluster.nodes[2].host=localhost",
        "cas.ticket.registry.redis.cluster.nodes[2].port=7003",
        "cas.ticket.registry.redis.cluster.nodes[2].type=MASTER",

        "cas.ticket.registry.redis.cluster.nodes[3].host=localhost",
        "cas.ticket.registry.redis.cluster.nodes[3].port=7004",
        "cas.ticket.registry.redis.cluster.nodes[3].type=REPLICA",

        "cas.ticket.registry.redis.cluster.nodes[4].host=localhost",
        "cas.ticket.registry.redis.cluster.nodes[4].port=7005",
        "cas.ticket.registry.redis.cluster.nodes[4].type=REPLICA",

        "cas.ticket.registry.redis.cluster.nodes[5].host=localhost",
        "cas.ticket.registry.redis.cluster.nodes[5].port=7006",
        "cas.ticket.registry.redis.cluster.nodes[5].type=REPLICA",

        "cas.ticket.registry.redis.crypto.encryption.key=AZ5y4I9qzKPYUVNL2Td4RMbpg6Z-ldui8VEFg8hsj1M",
        "cas.ticket.registry.redis.crypto.signing.key=cAPyoHMrOMWrwydOXzBA-ufZQM-TilnLjbRgMQWlUlwFmy07bOtAgCIdNBma3c5P4ae_JV6n1OpOAYqSh2NkmQ"
    })
    class DefaultTests extends BaseRedisSentinelTicketRegistryTests {
        @Autowired
        @Qualifier("redisTicketClusterTopologyManager")
        private ClusterTopologyManager redisTicketClusterTopologyManager;

        @RepeatedTest(1)
        void verifyOperation() throws Exception {
            val results = redisTicketClusterTopologyManager.discoverMembers();
            assertFalse(results.isEmpty());
        }
    }

}
