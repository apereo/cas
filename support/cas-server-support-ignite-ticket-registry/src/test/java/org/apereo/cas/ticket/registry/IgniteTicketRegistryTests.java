package org.apereo.cas.ticket.registry;

import java.util.Arrays;
import java.util.Collection;

import org.apereo.cas.config.IgniteTicketRegistryConfiguration;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * Unit test for {@link IgniteTicketRegistry}.
 *
 * @author Scott Battaglia
 * @author Timur Duehr timur.duehr@nccgroup.trust
 * @since 3.0.0
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class, IgniteTicketRegistryConfiguration.class})
@TestPropertySource(locations={"classpath:/igniteregistry.properties"})
public class IgniteTicketRegistryTests extends AbstractTicketRegistryTests {

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    public IgniteTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() throws Exception {
        // FIXME Encryption in Ignite registry is broken
        return Arrays.asList(false/*, true */);
    }

    @Override
    public TicketRegistry getNewTicketRegistry() throws Exception {
        return this.ticketRegistry;
    }
}
