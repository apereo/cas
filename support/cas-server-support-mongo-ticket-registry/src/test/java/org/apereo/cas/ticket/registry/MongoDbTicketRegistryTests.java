package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.CasMongoDbTicketRegistryAutoConfiguration;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.DefaultTicketDefinition;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.expiration.HardTimeoutExpirationPolicy;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.TicketGrantingTicketIdGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.Getter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.jooq.lambda.Unchecked;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
/**
 * This is {@link MongoDbTicketRegistryTests}.
 * <p>
 * The two nested classes are the encrypted and unencrypted runs. Each has a database of its own and
 * settles its cipher through its own configuration, so that whether tickets are encrypted is a
 * property of the context rather than something set on the shared registry before every test.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Tag("MongoDb")
@EnabledIfListeningOnPort(port = 27017)
class MongoDbTicketRegistryTests {

    @Nested
    @Tag("TicketRegistryTestWithoutEncryption")
    @TestPropertySource(properties = "cas.ticket.registry.mongo.database-name=ticket-registry-plain")
    class WithoutEncryption extends BaseMongoDbTicketRegistryTests {
    }

    @Nested
    @Tag("TicketRegistryTestWithEncryption")
    @TestPropertySource(properties = {
        "cas.ticket.registry.mongo.database-name=ticket-registry-crypto",
        "cas.ticket.registry.mongo.crypto.enabled=true"
    })
    class WithEncryption extends BaseMongoDbTicketRegistryTests {
    }

    @ImportAutoConfiguration(CasMongoDbTicketRegistryAutoConfiguration.class)
    @TestPropertySource(properties = {
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
    @Getter
    @Tag("SkipClearingTicketRegistry")
    abstract static class BaseMongoDbTicketRegistryTests extends BaseTicketRegistryTests {

        @Autowired
        @Qualifier(TicketRegistry.BEAN_NAME)
        private TicketRegistry newTicketRegistry;

        @Autowired
        @Qualifier("mongoDbTicketRegistryTemplate")
        private MongoOperations mongoDbTicketRegistryTemplate;

        @Override
        protected boolean isCipherExecutorOwnedByContext() {
            return true;
        }

        /**
         * Removing every session held for one principal must leave none behind for that principal.
         * The inherited version empties the registry and asserts it reports nothing at all, which
         * can only be true of a registry no other test is using.
         */
        @Override
        @RepeatedTest(2)
        void verifyGetTicketsIsZero() throws Throwable {
            val principal = UUID.randomUUID().toString();
            addSessionFor(principal);
            getNewTicketRegistry().deleteTicketsFor(principal);
            assertEquals(0, getNewTicketRegistry().countSessionsFor(principal));
        }

        /**
         * Removal reports how many it removed. Asked for one principal rather than for the whole
         * registry, which the inherited version empties and counts.
         *
         * @throws Throwable in case of failure
         */
        @Override
        @RepeatedTest(2)
        void verifyDeleteAllExistingTickets() throws Throwable {
            val principal = UUID.randomUUID().toString();
            addSessionFor(principal);
            assertEquals(1, getNewTicketRegistry().deleteTicketsFor(principal));
            assertEquals(0, getNewTicketRegistry().countSessionsFor(principal));
        }

        private void addSessionFor(final String principal) throws Throwable {
            val ticketGrantingTicketId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                .getNewTicketId(TicketGrantingTicket.PREFIX);
            getNewTicketRegistry().addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(principal), NeverExpiresExpirationPolicy.INSTANCE));
        }

        /**
         * The document is looked up by the identifier the registry stores it under rather than the
         * one the ticket was created with. Where the registry encrypts, those differ: the stored
         * identifier is the digest of the original, so querying the collection directly with the
         * original finds nothing.
         *
         * @throws Throwable in case of failure
         */
        @RepeatedTest(1)
        void verifyIndexesAndTtlConfiguration() throws Throwable {
            val ticketDefinition = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).orElseThrow();
            val collectionName = ticketDefinition.getProperties().getStorageName();
            val indexes = mongoDbTicketRegistryTemplate.getCollection(collectionName).listIndexes().into(new ArrayList<>());

            val expirationIndex = getIndexByName(indexes, "IDX_EXPIRATION");
            assertEquals(new Document(MongoDbTicketDocument.FIELD_NAME_EXPIRE_AT, 1), expirationIndex.get("key"));
            assertEquals(0, ((Number) expirationIndex.get("expireAfterSeconds")).longValue());

            val serviceIndex = getIndexByName(indexes, "IDX_SERVICE");
            assertEquals(new Document(MongoDbTicketDocument.FIELD_NAME_SERVICE, 1), serviceIndex.get("key"));

            val attributesIndex = getIndexByName(indexes, "IDX_ATTRIBUTES");
            assertEquals(new Document(MongoDbTicketDocument.FIELD_NAME_ATTRIBUTES + ".$**", 1), attributesIndex.get("key"));

            val timeout = 30;
            val creationWindow = Instant.now();
            val authentication = CoreAuthenticationTestUtils.getAuthentication(UUID.randomUUID().toString());
            val ticketGrantingTicketId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                .getNewTicketId(TicketGrantingTicket.PREFIX);
            getNewTicketRegistry().addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
                authentication, new HardTimeoutExpirationPolicy(timeout)));

            val storedId = getNewTicketRegistry().digestIdentifier(ticketGrantingTicketId);
            val query = new Query(Criteria.where(MongoDbTicketDocument.FIELD_NAME_ID).is(storedId));
            val document = mongoDbTicketRegistryTemplate.findOne(query, MongoDbTicketDocument.class, collectionName);
            assertNotNull(document);
            assertNotNull(document.getExpireAt());
            val expireAt = document.getExpireAt().toInstant();
            assertFalse(expireAt.isBefore(creationWindow.plusSeconds(timeout - 1)));
            assertTrue(expireAt.isBefore(creationWindow.plusSeconds(timeout + 10)));
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
        void verifyQuery() {
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
        void verifyCount() {
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
        void verifyBadTicketInCatalog() {
            val ticket = new MockTicketGrantingTicket("casuser");
            val catalog = mock(TicketCatalog.class);
            val ticketDefinition = new DefaultTicketDefinition(ticket.getClass(), TicketGrantingTicket.class, ticket.getPrefix(), 0);
            val mgr = mock(TicketSerializationManager.class);
            when(mgr.serializeTicket(any())).thenReturn("{}");
            val registry = new MongoDbTicketRegistry(CipherExecutor.noOp(), mgr, catalog, applicationContext, mongoDbTicketRegistryTemplate);

            when(catalog.find(any(Ticket.class))).thenReturn(null);
            assertThrows(IllegalArgumentException.class, () -> registry.addTicket(ticket));
            assertThrows(IllegalArgumentException.class, () -> registry.updateTicket(ticket));

            when(catalog.find(any(Ticket.class))).thenReturn(ticketDefinition);
            ticketDefinition.getProperties().setStorageName(null);
            assertThrows(IllegalArgumentException.class, () -> registry.addTicket(ticket));
            assertThrows(IllegalArgumentException.class, () -> registry.updateTicket(ticket));

            when(catalog.find(any(Ticket.class))).thenThrow(new RuntimeException());
            assertThrows(RuntimeException.class, () -> registry.addTicket(ticket));
            assertThrows(RuntimeException.class, () -> registry.updateTicket(ticket));

            when(catalog.find(anyString())).thenThrow(new RuntimeException());
            assertThrows(RuntimeException.class, () -> registry.getTicket(ticket.getId()));
        }

        @RepeatedTest(1)
        void verifySessionsWithDottedAttributeKeys() throws Throwable {
            val attributeKey = "custom.attribute.name";
            val attributeValue = UUID.randomUUID().toString();
            val principal = CoreAuthenticationTestUtils.getPrincipal(UUID.randomUUID().toString(),
                Map.of(attributeKey, List.<Object>of(attributeValue)));
            val ticketGrantingTicketId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                .getNewTicketId(TicketGrantingTicket.PREFIX);
            getNewTicketRegistry().addTicket(new TicketGrantingTicketImpl(ticketGrantingTicketId,
                CoreAuthenticationTestUtils.getAuthentication(principal), NeverExpiresExpirationPolicy.INSTANCE));

            try (val results = getNewTicketRegistry().getSessionsWithAttributes(Map.of(attributeKey, List.<Object>of(attributeValue)))) {
                assertEquals(1, results.count());
            }
        }

        @RepeatedTest(2)
        void verifyGetSessionsFor() {
            val principalId = UUID.randomUUID().toString();
            val authentication = CoreAuthenticationTestUtils.getAuthentication(principalId);
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

            assertEquals(5, getNewTicketRegistry().getSessionsFor(principalId).count());
        }

        @RepeatedTest(2)
        void verifyDeleteSessionsFor() {
            val principalId = UUID.randomUUID().toString();
            val authentication = CoreAuthenticationTestUtils.getAuthentication(principalId);
            val ticketGrantingTicketToAdd = Stream.generate(() -> {
                    val tgtId = new TicketGrantingTicketIdGenerator(10, StringUtils.EMPTY)
                        .getNewTicketId(TicketGrantingTicket.PREFIX);
                    return new TicketGrantingTicketImpl(tgtId, authentication, NeverExpiresExpirationPolicy.INSTANCE);
                })
                .limit(5);
            getNewTicketRegistry().addTicket(ticketGrantingTicketToAdd);

            val tickets = getNewTicketRegistry().getSessionsFor(principalId).toList();
            assertEquals(5, tickets.size());

            tickets.forEach(Unchecked.consumer(ticket -> getNewTicketRegistry().deleteTicket(ticket)));
            assertEquals(0, getNewTicketRegistry().getSessionsFor(principalId).count());
        }

        private static Document getIndexByName(final Collection<Document> indexes, final String name) {
            return indexes
                .stream()
                .filter(index -> name.equals(index.getString("name")))
                .findFirst()
                .orElseThrow();
        }
    }
}
