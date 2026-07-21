package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.config.CasIgniteTicketRegistryAutoConfiguration;
import org.apereo.cas.ha.ClusterTopologyManager;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link IgniteTicketRegistry}.
 *
 * @author Scott Battaglia
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 3.0.0
 */
@Tag("Ignite")
@ImportAutoConfiguration(CasIgniteTicketRegistryAutoConfiguration.class)
@TestPropertySource(
    properties = {
        "cas.ticket.registry.ignite.ignite-servers=localhost:47500",
        "cas.ticket.registry.ignite.initialize-cluster=true"
    })
@Getter
class IgniteTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier("igniteClusterTopologyManager")
    private ClusterTopologyManager igniteClusterTopologyManager;

    @RepeatedTest(1)
    void verifyOperation() throws Exception {
        val results = igniteClusterTopologyManager.discoverMembers();
        assertFalse(results.isEmpty());
    }
}
