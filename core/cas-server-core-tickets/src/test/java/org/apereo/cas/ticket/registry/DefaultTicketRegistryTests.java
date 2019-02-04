package org.apereo.cas.ticket.registry;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;

import org.springframework.boot.test.context.SpringBootTest;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@SpringBootTest(classes = {
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class
})
public class DefaultTicketRegistryTests extends BaseTicketRegistryTests {

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return new DefaultTicketRegistry(10, 10, 5, CipherExecutor.noOp());
    }
}
