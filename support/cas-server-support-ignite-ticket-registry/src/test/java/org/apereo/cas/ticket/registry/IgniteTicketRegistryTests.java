package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasIgniteTicketRegistryAutoConfiguration;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

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
}
