package org.apereo.cas.ticket.registry;

import java.util.Arrays;
import java.util.Collection;

import org.apereo.cas.ticket.registry.config.InfinispanTicketRegistryConfiguration;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;

/**
 * This is {@link InfinispanTicketRegistryTests}.
 *
 * @since 4.2.0
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class, InfinispanTicketRegistryConfiguration.class})
public class InfinispanTicketRegistryTests extends AbstractTicketRegistryTests {

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    public InfinispanTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(false, true);
    }

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return ticketRegistry;
    }
}
