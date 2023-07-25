package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.authentication.RememberMeCredential;
import org.apereo.cas.config.MongoDbTicketRegistryConfiguration;
import org.apereo.cas.config.MongoDbTicketRegistryTicketCatalogConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.DefaultTicketDefinition;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.AlwaysExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.BaseDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.expiration.RememberMeDelegatingExpirationPolicy;
import org.apereo.cas.ticket.expiration.TimeoutExpirationPolicy;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.DateTimeUtils;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
        val registry = new MongoDbTicketRegistry(catalog, mongoDbTicketRegistryTemplate, mgr);
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

    @RepeatedTest(1)
    @SuppressWarnings("JavaUtilDate")
    public void verifyExpireAtTicketTimeoutExpirationPolicy() throws Exception {
        val ticket = new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                new TimeoutExpirationPolicy(60));
        newTicketRegistry.addTicket(ticket);

        val collectionName = "ticketGrantingTicketsCollection";

        val query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(ticket.getId()));
        val d = mongoDbTicketRegistryTemplate.findOne(query, TicketHolder.class, collectionName);
        val creationExpireAt = d.getExpireAt();

        Thread.sleep(5);

        newTicketRegistry.updateTicket(ticket);
        val dd = mongoDbTicketRegistryTemplate.findOne(query, TicketHolder.class, collectionName);
        val updateExpireAt = dd.getExpireAt();

        assertTrue(updateExpireAt.after(creationExpireAt));
    }

    @RepeatedTest(1)
    @SuppressWarnings("JavaUtilDate")
    public void verifyExpireAtTicketHardTimeoutExpirationPolicy() throws Exception {
        val ticket = new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                new HardTimeoutExpirationPolicy(60000));
        newTicketRegistry.addTicket(ticket);

        val collectionName = "ticketGrantingTicketsCollection";

        val query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(ticket.getId()));
        val d = mongoDbTicketRegistryTemplate.findOne(query, TicketHolder.class, collectionName);
        val creationExpireAt = d.getExpireAt();

        Thread.sleep(5);

        newTicketRegistry.updateTicket(ticket);
        val dd = mongoDbTicketRegistryTemplate.findOne(query, TicketHolder.class, collectionName);
        val updateExpireAt = dd.getExpireAt();

        assertEquals(updateExpireAt, creationExpireAt);

        val exp = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30000);
        val datePlus30000Secs = DateTimeUtils.dateOf(Instant.ofEpochMilli(exp));
        assertTrue(creationExpireAt.after(datePlus30000Secs));
    }

    @RepeatedTest(1)
    @SuppressWarnings("JavaUtilDate")
    public void verifyExpireAtTicketRememberMeDelegatingExpirationPolicy() throws Exception {

        val p = new RememberMeDelegatingExpirationPolicy();
        p.addPolicy(RememberMeDelegatingExpirationPolicy.POLICY_NAME_REMEMBER_ME, new HardTimeoutExpirationPolicy(60000));
        p.addPolicy(BaseDelegatingExpirationPolicy.POLICY_NAME_DEFAULT, new TimeoutExpirationPolicy(60));

        val ticket = new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                p);
        newTicketRegistry.addTicket(ticket);

        val collectionName = "ticketGrantingTicketsCollection";

        val query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(ticket.getId()));
        val d = mongoDbTicketRegistryTemplate.findOne(query, TicketHolder.class, collectionName);
        val creationExpireAt = d.getExpireAt();

        Thread.sleep(5);

        newTicketRegistry.updateTicket(ticket);
        val dd = mongoDbTicketRegistryTemplate.findOne(query, TicketHolder.class, collectionName);
        val updateExpireAt = dd.getExpireAt();

        assertTrue(updateExpireAt.after(creationExpireAt));

        val exp = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30000);
        val datePlus30000Secs = DateTimeUtils.dateOf(Instant.ofEpochMilli(exp));
        assertFalse(creationExpireAt.after(datePlus30000Secs));
    }

    @RepeatedTest(1)
    @SuppressWarnings("JavaUtilDate")
    public void verifyExpireAtRememberMeTicketRememberMeDelegatingExpirationPolicy() throws Exception {

        val p = new RememberMeDelegatingExpirationPolicy();
        p.addPolicy(RememberMeDelegatingExpirationPolicy.POLICY_NAME_REMEMBER_ME, new HardTimeoutExpirationPolicy(60000));
        p.addPolicy(BaseDelegatingExpirationPolicy.POLICY_NAME_DEFAULT, new TimeoutExpirationPolicy(60));

        val ticket = new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(CoreAuthenticationTestUtils.getPrincipal(),
                        Collections.singletonMap(RememberMeCredential.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, List.of(true)),
                        null),
                p);
        newTicketRegistry.addTicket(ticket);

        val collectionName = "ticketGrantingTicketsCollection";

        val query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(ticket.getId()));
        val d = mongoDbTicketRegistryTemplate.findOne(query, TicketHolder.class, collectionName);
        val creationExpireAt = d.getExpireAt();

        Thread.sleep(5);

        newTicketRegistry.updateTicket(ticket);
        val dd = mongoDbTicketRegistryTemplate.findOne(query, TicketHolder.class, collectionName);
        val updateExpireAt = dd.getExpireAt();

        assertEquals(updateExpireAt, creationExpireAt);

        val exp = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30000);
        val datePlus30000Secs = DateTimeUtils.dateOf(Instant.ofEpochMilli(exp));
        assertTrue(creationExpireAt.after(datePlus30000Secs));
    }

    @RepeatedTest(1)
    @SuppressWarnings("JavaUtilDate")
    public void verifyExpireAtTicketNeverExpiresExpirationPolicy() throws Exception {

        val ticket = new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        newTicketRegistry.addTicket(ticket);

        val collectionName = "ticketGrantingTicketsCollection";

        val query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(ticket.getId()));
        val d = mongoDbTicketRegistryTemplate.findOne(query, TicketHolder.class, collectionName);
        val creationExpireAt = d.getExpireAt();
        val exp = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(50*365);
        val datePlus50Years = DateTimeUtils.dateOf(Instant.ofEpochMilli(exp));

        assertTrue(creationExpireAt == null || creationExpireAt.after(datePlus50Years));
    }

    @RepeatedTest(1)
    @SuppressWarnings("JavaUtilDate")
    public void verifyExpireAtTicketAlwaysExpiresExpirationPolicy() throws Exception {

        val ticket = new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(),
                new AlwaysExpiresExpirationPolicy());
        newTicketRegistry.addTicket(ticket);

        val collectionName = "ticketGrantingTicketsCollection";

        val query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(ticket.getId()));
        val d = mongoDbTicketRegistryTemplate.findOne(query, TicketHolder.class, collectionName);

        val exp = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(1);
        val nowPlus1second = DateTimeUtils.dateOf(Instant.ofEpochMilli(exp));

        assertTrue(d == null || d.getExpireAt().before(nowPlus1second));
    }

}
