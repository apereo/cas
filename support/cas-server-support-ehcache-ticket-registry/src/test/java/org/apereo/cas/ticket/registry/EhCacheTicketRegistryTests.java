package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.EhcacheTicketRegistryConfiguration;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit test for {@link EhCacheTicketRegistry}.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EhcacheTicketRegistryConfiguration.class, RefreshAutoConfiguration.class})
@ContextConfiguration(locations = "classpath:ticketRegistry.xml")
public class EhCacheTicketRegistryTests extends AbstractTicketRegistryTests {

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;
    
    @Override
    public TicketRegistry getNewTicketRegistry() throws Exception {
        return ticketRegistry;
    }
}
