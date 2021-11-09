package org.apereo.cas.ticket.registry;

import org.apereo.cas.config.CassandraTicketRegistryConfiguration;
import org.apereo.cas.config.CassandraTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.Getter;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

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
@Getter
public class CassandraTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @RepeatedTest(1)
    public void verifyFails() {
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                newTicketRegistry.addTicket((Ticket) null);
            }
        });
    }

}
