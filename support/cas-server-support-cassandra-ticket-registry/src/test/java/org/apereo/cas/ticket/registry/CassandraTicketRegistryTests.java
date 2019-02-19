package org.apereo.cas.ticket.registry;

import org.apereo.cas.util.junit.EnabledIfContinuousIntegration;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link CassandraTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties
@TestPropertySource(properties = {
    "cas.ticket.registry.cassandra.keyspace=cas"
})
@Tag("Cassandra")
@EnabledIfContinuousIntegration
public class CassandraTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return this.ticketRegistry;
    }
}
