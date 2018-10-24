package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.ConditionalIgnoreRule;

import org.junit.Rule;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public abstract class BaseSpringRunnableTicketRegistryTests extends BaseTicketRegistryTests {

    @Rule
    public final ConditionalIgnoreRule conditionalIgnoreRule = new ConditionalIgnoreRule();

    public BaseSpringRunnableTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }
}
