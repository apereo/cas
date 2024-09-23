package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasMongoDbTicketRegistryAutoConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.DefaultTicketDefinition;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import java.util.UUID;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MongoDbTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("MongoDb")
@ImportAutoConfiguration(CasMongoDbTicketRegistryAutoConfiguration.class)
@TestPropertySource(properties = {
    "cas.ticket.registry.mongo.database-name=ticket-registry",
    "cas.ticket.registry.mongo.authentication-database-name=admin",
    "cas.ticket.registry.mongo.host=localhost",
    "cas.ticket.registry.mongo.port=27017",
    "cas.ticket.registry.mongo.drop-collection=true",
    "cas.ticket.registry.mongo.update-indexes=true",
    "cas.ticket.registry.mongo.drop-indexes=true",
    "cas.ticket.registry.mongo.user-id=root",
    "cas.ticket.registry.mongo.password=secret"
})
@EnableScheduling
@EnabledIfListeningOnPort(port = 27017)
@Getter
class MongoDbTicketRegistryTests extends BaseTicketRegistryTests {

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier("mongoDbTicketRegistryTemplate")
    private MongoOperations mongoDbTicketRegistryTemplate;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @BeforeEach
    public void before() {
        newTicketRegistry.deleteAll();
    }

    @RepeatedTest(2)
    void verifyUpdateFirstAndClean() throws Throwable {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
        val ticketGrantingTicketId = TestTicketIdentifiers.generate().ticketGrantingTicketId();
        val result = newTicketRegistry.updateTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE));
        assertNull(result);
    }

    @RepeatedTest(2)
    void verifyQuery() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
        val ticketGrantingTicketToAdd = Stream.generate(() -> {
                val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                    .getNewTicketId(TicketGrantingTicket.PREFIX);
                return new TicketGrantingTicketImpl(tgtId, authentication, NeverExpiresExpirationPolicy.INSTANCE);
            })
            .limit(5);
        getNewTicketRegistry().addTicket(ticketGrantingTicketToAdd);

        val criteria1 = new TicketRegistryQueryCriteria()
            .setCount(5L)
            .setDecode(Boolean.FALSE)
            .setType(TicketGrantingTicket.PREFIX);
        val queryResults1 = getNewTicketRegistry().query(criteria1);
        assertEquals(criteria1.getCount(), queryResults1.size());

        val criteria2 = new TicketRegistryQueryCriteria()
            .setCount(5L)
            .setDecode(Boolean.TRUE)
            .setType(TicketGrantingTicket.PREFIX);
        val queryResults = getNewTicketRegistry().query(criteria2);
        assertEquals(criteria2.getCount(), queryResults.size());
    }

    @RepeatedTest(2)
    void verifyCount() throws Throwable {
        val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
        val ticketGrantingTicketToAdd = Stream.generate(() -> {
                val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                    .getNewTicketId(TicketGrantingTicket.PREFIX);
                return new TicketGrantingTicketImpl(tgtId, authentication, NeverExpiresExpirationPolicy.INSTANCE);
            })
            .limit(5);
        getNewTicketRegistry().addTicket(ticketGrantingTicketToAdd);
        val count = getNewTicketRegistry().countTickets();
        assertTrue(count > 0);
    }
    
    @RepeatedTest(1)
    void verifyBadTicketInCatalog() throws Throwable {
        val ticket = new MockTicketGrantingTicket("casuser");
        val catalog = mock(TicketCatalog.class);
        val ticketDefinition = new DefaultTicketDefinition(ticket.getClass(), TicketGrantingTicket.class, ticket.getPrefix(), 0);
        when(catalog.find(any(Ticket.class))).thenReturn(null);
        val mgr = mock(TicketSerializationManager.class);
        when(mgr.serializeTicket(any())).thenReturn("{}");
        val registry = new MongoDbTicketRegistry(CipherExecutor.noOp(), mgr, catalog, applicationContext, mongoDbTicketRegistryTemplate);
        registry.addTicket(ticket);
        assertNull(registry.updateTicket(ticket));

        when(catalog.find(any(Ticket.class))).thenReturn(ticketDefinition);
        ticketDefinition.getProperties().setStorageName(null);
        registry.addTicket(ticket);
        assertNull(registry.updateTicket(ticket));

        when(catalog.find(any(Ticket.class))).thenThrow(new RuntimeException());
        ticketDefinition.getProperties().setStorageName(null);
        registry.addTicket(ticket);
        assertNull(registry.updateTicket(ticket));

        when(catalog.find(anyString())).thenThrow(new RuntimeException());
        assertNull(registry.getTicket(ticket.getId()));
    }
}
