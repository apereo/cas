package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.MongoDbTicketRegistryConfiguration;
import org.apereo.cas.config.MongoDbTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.DefaultTicketDefinition;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;

import lombok.Getter;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link MongoDbTicketRegistryTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("MongoDb")
@Import({
    MongoDbTicketRegistryTicketCatalogConfiguration.class,
    MongoDbTicketRegistryConfiguration.class
})
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
public class MongoDbTicketRegistryTests extends BaseTicketRegistryTests {

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry newTicketRegistry;

    @Autowired
    @Qualifier("mongoDbTicketRegistryTemplate")
    private MongoOperations mongoDbTicketRegistryTemplate;

    @BeforeEach
    public void before() {
        newTicketRegistry.deleteAll();
    }

    @RepeatedTest(2)
    public void verifyUpdateFirstAndClean() throws Exception {
        val originalAuthn = CoreAuthenticationTestUtils.getAuthentication();
        val result = newTicketRegistry.updateTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
            originalAuthn, NeverExpiresExpirationPolicy.INSTANCE));
        assertNull(result);
    }

    @RepeatedTest(1)
    public void verifyBadTicketInCatalog() throws Exception {
        val ticket = new MockTicketGrantingTicket("casuser");
        val catalog = mock(TicketCatalog.class);
        val defn = new DefaultTicketDefinition(ticket.getClass(), TicketGrantingTicket.class, ticket.getPrefix(), 0);
        when(catalog.find(any(Ticket.class))).thenReturn(null);
        val mgr = mock(TicketSerializationManager.class);
        when(mgr.serializeTicket(any())).thenReturn("{}");
        val registry = new MongoDbTicketRegistry(CipherExecutor.noOp(), mgr, catalog, mongoDbTicketRegistryTemplate);
        registry.addTicket(ticket);
        assertNull(registry.updateTicket(ticket));

        when(catalog.find(any(Ticket.class))).thenReturn(defn);
        defn.getProperties().setStorageName(null);
        registry.addTicket(ticket);
        assertNull(registry.updateTicket(ticket));

        when(catalog.find(any(Ticket.class))).thenThrow(new RuntimeException());
        defn.getProperties().setStorageName(null);
        registry.addTicket(ticket);
        assertNull(registry.updateTicket(ticket));

        when(catalog.find(anyString())).thenThrow(new RuntimeException());
        assertNull(registry.getTicket(ticket.getId()));
    }
}
