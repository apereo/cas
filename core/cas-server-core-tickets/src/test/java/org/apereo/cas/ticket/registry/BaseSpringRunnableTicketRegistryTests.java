package org.apereo.cas.ticket.registry;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public abstract class BaseSpringRunnableTicketRegistryTests extends BaseTicketRegistryTests {

    public BaseSpringRunnableTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }
}
