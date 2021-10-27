package org.jasig.cas.ticket.registry;

import static org.junit.Assert.*;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class DefaultTicketRegistryTests extends AbstractTicketRegistryTests {

    @Override
    public TicketRegistry getNewTicketRegistry() throws Exception {
        return new DefaultTicketRegistry();
    }

    public void verifyOtherConstructor() {
        assertNotNull(new DefaultTicketRegistry(10, 10F, 5));
    }
}
