package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CassandraTicketRegistryConfiguration;
import org.apereo.cas.config.CassandraTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This is {@link CassandraTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@SpringBootTest(classes = {
    CassandraTicketRegistryConfiguration.class,
    CassandraTicketRegistryTicketCatalogConfiguration.class,
    BaseTicketRegistryTests.SharedTestConfiguration.class
}, properties = {
    "cas.ticket.registry.cassandra.keyspace=cas",
    "cas.ticket.registry.cassandra.local-dc=datacenter1",
    "cas.ticket.registry.cassandra.drop-tables-on-startup=true"
})
@Tag("Cassandra")
@EnabledIfPortOpen(port = 9042)
public class CassandraTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Override
    public TicketRegistry getNewTicketRegistry() {
        return this.ticketRegistry;
    }
}
