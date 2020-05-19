package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.Ehcache3TicketRegistryConfiguration;
import org.apereo.cas.config.Ehcache3TicketRegistryTicketCatalogConfiguration;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Unit test for {@link EhCache3TicketRegistry}.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
@SpringBootTest(classes = {
    Ehcache3TicketRegistryConfiguration.class,
    Ehcache3TicketRegistryTicketCatalogConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
})
@Tag("Ehcache")
public class EhCache3TicketRegistryTests extends BaseTicketRegistryTests {

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return ticketRegistry;
    }
}
