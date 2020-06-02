package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.IgniteTicketRegistryConfiguration;
import org.apereo.cas.config.IgniteTicketRegistryTicketCatalogConfiguration;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Unit test for {@link IgniteTicketRegistry}.
 *
 * @author Scott Battaglia
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 3.0.0
 */
@Tag("Ignite")
@SpringBootTest(classes = {
    IgniteTicketRegistryConfiguration.class,
    IgniteTicketRegistryTicketCatalogConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
},
    properties = {
        "cas.ticket.registry.ignite.tickets-cache.write-synchronization-mode=FULL_ASYNC",
        "cas.ticket.registry.ignite.tickets-cache.atomicity-mode=ATOMIC",
        "cas.ticket.registry.ignite.tickets-cache.cache-mode=REPLICATED",
        "cas.ticket.registry.ignite.ignite-address[0]=localhost:47500"
    })
public class IgniteTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Override
    protected TicketRegistry getNewTicketRegistry() {
        return ticketRegistry;
    }
}
