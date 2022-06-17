package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.Ehcache3TicketRegistryConfiguration;
import org.apereo.cas.config.Ehcache3TicketRegistryTicketCatalogConfiguration;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;

/**
 * Unit test for {@link EhCache3TicketRegistry}.
 *
 * @author Hal Deadman
 * @since 6.2.0
 */
@Import({
    Ehcache3TicketRegistryConfiguration.class,
    Ehcache3TicketRegistryTicketCatalogConfiguration.class
})
@Tag("Ehcache")
@Getter
public class EhCache3TicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;
}
