package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CassandraTicketRegistryAutoConfiguration;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CassandraTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ImportAutoConfiguration(CassandraTicketRegistryAutoConfiguration.class)
@TestPropertySource(
    properties = {
        "cas.ticket.registry.cassandra.keyspace=cas",
        "cas.ticket.registry.cassandra.local-dc=datacenter1",
        "cas.ticket.registry.cassandra.drop-tables-on-startup=true",
        "cas.ticket.registry.cassandra.ssl-protocols=TLSv1.2",
        "cas.http-client.host-name-verifier=none"
    })
@Tag("Cassandra")
@Tag("TicketRegistryTestWithEncryption")
@Tag("SkipClearingTicketRegistry")
@EnabledIfListeningOnPort(port = 9042)
@Getter
class CassandraTicketRegistryTests extends BaseTicketRegistryTests {
    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Override
    protected boolean isCipherExecutorOwnedByContext() {
        return true;
    }

    @RepeatedTest(1)
    void verifyFails() {
        assertDoesNotThrow(() -> newTicketRegistry.addTicket((Ticket) null));
    }

    @RepeatedTest(1)
    void verifyTicketIsFoundByItsOwnIdentifier() throws Throwable {
        val first = new TicketGrantingTicketImpl(TestTicketIdentifiers.generate().ticketGrantingTicketId(),
            CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString()), NeverExpiresExpirationPolicy.INSTANCE);
        val second = new TicketGrantingTicketImpl(TestTicketIdentifiers.generate().ticketGrantingTicketId(),
            CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString()), NeverExpiresExpirationPolicy.INSTANCE);
        newTicketRegistry.addTicket(first);
        newTicketRegistry.addTicket(second);
        assertEquals(first.getId(), newTicketRegistry.getTicket(first.getId()).getId());
        assertEquals(second.getId(), newTicketRegistry.getTicket(second.getId()).getId());
        assertNull(newTicketRegistry.getTicket(TestTicketIdentifiers.generate().ticketGrantingTicketId()));
    }

}
