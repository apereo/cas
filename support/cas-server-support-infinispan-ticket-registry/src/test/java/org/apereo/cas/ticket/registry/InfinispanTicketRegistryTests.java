package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.registry.config.InfinispanTicketRegistryConfiguration;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;

/**
 * This is {@link InfinispanTicketRegistryTests}.
 *
 * @since 4.2.0
 * @deprecated since 6.6 and scheduled for removal.
 */
@Import(InfinispanTicketRegistryConfiguration.class)
@Tag("Infinispan")
@Getter
@Deprecated(since = "6.6")
public class InfinispanTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;
}
