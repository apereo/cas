package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.config.CasGeodeTicketRegistryAutoConfiguration;
import org.apereo.cas.ha.ClusterTopologyManager;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for {@link GeodeTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Tickets")
@ImportAutoConfiguration(CasGeodeTicketRegistryAutoConfiguration.class)
@TestPropertySource(properties = "cas.ticket.registry.geode.locators=none")
@Getter
@Execution(ExecutionMode.SAME_THREAD)
class GeodeTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier("geodeClusterTopologyManager")
    private ClusterTopologyManager geodeClusterTopologyManager;

    @RepeatedTest(1)
    void verifyOperation() throws Exception {
        val results = geodeClusterTopologyManager.discoverMembers();
        assertFalse(results.isEmpty());
    }

}
