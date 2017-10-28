package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CasCoreUtilSerializationConfiguration;
import org.apereo.cas.config.MemcachedTicketRegistryConfiguration;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.Collection;

/**
 * Unit test for MemcachedTicketRegistry class.
 *
 * @author Middleware Services
 * @since 3.0.0
 */
@RunWith(Parameterized.class)
@SpringBootTest(classes = {MemcachedTicketRegistryConfiguration.class, 
        RefreshAutoConfiguration.class,
        CasCoreUtilSerializationConfiguration.class})
@TestPropertySource(locations = {"classpath:/memcached.properties"})
public class MemcachedTicketRegistryTests extends AbstractTicketRegistryTests {
  
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry registry;

    public MemcachedTicketRegistryTests(final boolean useEncryption) {
        super(useEncryption);
    }

    @Parameterized.Parameters
    public static Collection<Object> getTestParameters() {
        return Arrays.asList(false, true);
    }

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return registry;
    }


    @Override
    protected boolean isIterableRegistry() {
        return false;
    }
}
