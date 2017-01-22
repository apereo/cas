package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.HazelcastTicketRegistryConfiguration;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Unit tests for {@link HazelcastTicketRegistry}.
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RefreshAutoConfiguration.class, HazelcastTicketRegistryConfiguration.class})
@TestPropertySource(properties = {"cas.ticket.registry.hazelcast.cluster.instanceName=testlocalhostinstance"})
public class HazelcastTicketRegistryTests extends AbstractTicketRegistryTests {
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Override
    public TicketRegistry getNewTicketRegistry() throws Exception {
        return ticketRegistry;
    }
}
