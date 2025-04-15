package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasGeodeTicketRegistryAutoConfiguration;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link GeodeTicketRegistry}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Tag("Tickets")
@ImportAutoConfiguration(CasGeodeTicketRegistryAutoConfiguration.class)
@TestPropertySource(properties = "cas.ticket.registry.geode.locators=none")
@Getter
class GeodeTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;
}
