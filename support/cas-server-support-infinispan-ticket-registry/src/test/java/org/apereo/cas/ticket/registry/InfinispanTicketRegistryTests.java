package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.registry.config.InfinispanTicketRegistryConfiguration;

import lombok.Getter;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link InfinispanTicketRegistryTests}.
 *
 * @since 4.2.0
 */
@SpringBootTest(classes = {
    InfinispanTicketRegistryConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
})
@Tag("Infinispan")
@Getter
public class InfinispanTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;
}
