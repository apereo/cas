package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.crypto.CipherExecutor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Test case to test the DefaultTicketRegistry based on test cases to test all
 * Ticket Registries.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class DefaultTicketRegistryTests extends BaseTicketRegistryTests {

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return new DefaultTicketRegistry(new ConcurrentHashMap<>(10, 10, 5), CipherExecutor.noOp());
    }
}
