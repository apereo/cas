package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.StatelessTicketRegistryConfiguration;
import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;

/**
 * This is {@link StatelessTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Tickets")
@Import(StatelessTicketRegistryConfiguration.class)
@Getter
public class StatelessTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

}
