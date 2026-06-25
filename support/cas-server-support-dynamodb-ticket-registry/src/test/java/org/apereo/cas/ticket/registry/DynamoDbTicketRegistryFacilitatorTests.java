package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfListeningOnPort;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndexDescription;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamoDbTicketRegistryFacilitatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("DynamoDb")
class DynamoDbTicketRegistryFacilitatorTests {

    @Nested
    @EnabledIfListeningOnPort(port = 8000)
    class OriginalDynamoDbTicketRegistryFacilitatorTests extends BaseDynamoDbTicketRegistryFacilitatorTests {
        @Test
        void verifyBuildAttributeMap() {
            val ticket = new MockTicketGrantingTicket("casuser",
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                CollectionUtils.wrap("name", "CAS"));
            val map = dynamoDbTicketRegistryFacilitator.buildTableAttributeValuesMapFromTicket(
                DynamoDbTicketRegistryFacilitator.TicketPayload.builder()
                    .originalTicket(ticket)
                    .encodedTicket(ticket)
                    .principal("casuser")
                    .service(RegisteredServiceTestUtils.CONST_TEST_URL)
                    .build());
            assertFalse(map.isEmpty());
            Arrays.stream(DynamoDbTicketRegistryFacilitator.ColumnNames.values())
                .forEach(defn -> assertTrue(map.containsKey(defn.getColumnName())));
        }

        @Test
        void verifyBuildAttributeMapWithoutPrincipalOrService() {
            val ticket = new MockTicketGrantingTicket("anonymous");
            val map = dynamoDbTicketRegistryFacilitator.buildTableAttributeValuesMapFromTicket(
                DynamoDbTicketRegistryFacilitator.TicketPayload.builder()
                    .originalTicket(ticket)
                    .encodedTicket(ticket)
                    .build());
            assertFalse(map.isEmpty());
            assertFalse(map.containsKey(DynamoDbTicketRegistryFacilitator.ColumnNames.PRINCIPAL.getColumnName()));
            assertFalse(map.containsKey(DynamoDbTicketRegistryFacilitator.ColumnNames.SERVICE.getColumnName()));
        }

        @Test
        void verifyTicketOperations() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val ticket = new MockTicketGrantingTicket("casuser",
                CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                CollectionUtils.wrap("name", "CAS"));
            dynamoDbTicketRegistryFacilitator.put(
                DynamoDbTicketRegistryFacilitator.TicketPayload.builder()
                    .originalTicket(ticket)
                    .encodedTicket(ticket)
                    .principal("casuser")
                    .service(RegisteredServiceTestUtils.CONST_TEST_URL)
                    .build());
            val col = dynamoDbTicketRegistryFacilitator.getAll();
            assertFalse(col.isEmpty());
            val ticketFetched = dynamoDbTicketRegistryFacilitator.get(ticket.getId(), ticket.getId());
            assertEquals(ticket, ticketFetched);
            assertFalse(dynamoDbTicketRegistryFacilitator.delete("badticket", "badticket"));
            assertTrue(dynamoDbTicketRegistryFacilitator.deleteAll() > 0);
        }

        @Test
        void verifyGetReturnsNullForUnknownTicketId() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val result = dynamoDbTicketRegistryFacilitator.get("unknown-ticket-xyz", "unknown-ticket-xyz");
            assertNull(result);
        }

        @Test
        void verifyGetSessionsFor() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val principal = UUID.randomUUID().toString();
            val ticket = new MockTicketGrantingTicket(principal);
            dynamoDbTicketRegistryFacilitator.put(
                DynamoDbTicketRegistryFacilitator.TicketPayload.builder()
                    .originalTicket(ticket)
                    .encodedTicket(ticket)
                    .principal(principal)
                    .service(RegisteredServiceTestUtils.CONST_TEST_URL)
                    .build());
            val sessions = dynamoDbTicketRegistryFacilitator.getSessionsFor(principal).toList();
            assertFalse(sessions.isEmpty());
            assertTrue(sessions.stream().anyMatch(t -> t.getId().equals(ticket.getId())));
        }

        @Test
        void verifyGetSessionsForEmptyPrincipal() {
            val sessions = dynamoDbTicketRegistryFacilitator.getSessionsFor(StringUtils.EMPTY).toList();
            assertTrue(sessions.isEmpty());
        }

        @Test
        void verifyGetTicketsFor() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val serviceId = RegisteredServiceTestUtils.CONST_TEST_URL;
            val ticket = new MockTicketGrantingTicket("casuser");
            dynamoDbTicketRegistryFacilitator.put(
                DynamoDbTicketRegistryFacilitator.TicketPayload.builder()
                    .originalTicket(ticket)
                    .encodedTicket(ticket)
                    .principal("casuser")
                    .service(serviceId)
                    .build());
            val service = RegisteredServiceTestUtils.getService(serviceId);
            val tickets = dynamoDbTicketRegistryFacilitator.getTicketsFor(service).toList();
            assertFalse(tickets.isEmpty());
        }

        @Test
        void verifyCountTicketsFor() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val serviceId = RegisteredServiceTestUtils.CONST_TEST_URL;
            val ticket = new MockTicketGrantingTicket("casuser");
            dynamoDbTicketRegistryFacilitator.put(
                DynamoDbTicketRegistryFacilitator.TicketPayload.builder()
                    .originalTicket(ticket)
                    .encodedTicket(ticket)
                    .principal("casuser")
                    .service(serviceId)
                    .build());
            val service = RegisteredServiceTestUtils.getService(serviceId);
            val tgtStorageName = dynamoDbTicketRegistryFacilitator.getTicketCatalog()
                .findTicketDefinition(TicketGrantingTicket.class)
                .orElseThrow()
                .getProperties()
                .getStorageName();
            val count = dynamoDbTicketRegistryFacilitator.countTicketsFor(tgtStorageName, service);
            assertTrue(count >= 1);
        }

        @Test
        void verifyCountTickets() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val ticket = new MockTicketGrantingTicket("casuser");
            dynamoDbTicketRegistryFacilitator.put(
                DynamoDbTicketRegistryFacilitator.TicketPayload.builder()
                    .originalTicket(ticket)
                    .encodedTicket(ticket)
                    .principal("casuser")
                    .service(RegisteredServiceTestUtils.CONST_TEST_URL)
                    .build());
            val count = dynamoDbTicketRegistryFacilitator.countTickets(TicketGrantingTicket.class, TicketGrantingTicket.PREFIX);
            assertTrue(count >= 1);
        }

        @Test
        void verifyCountTicketsForUnknownPrefix() {
            val count = dynamoDbTicketRegistryFacilitator.countTickets(ServiceTicket.class, "UNKNOWN-PREFIX");
            assertEquals(0, count);
        }

        @Test
        void verifyStream() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val ticket = new MockTicketGrantingTicket("casuser");
            dynamoDbTicketRegistryFacilitator.put(
                DynamoDbTicketRegistryFacilitator.TicketPayload.builder()
                    .originalTicket(ticket)
                    .encodedTicket(ticket)
                    .principal("casuser")
                    .service(RegisteredServiceTestUtils.CONST_TEST_URL)
                    .build());
            val tickets = dynamoDbTicketRegistryFacilitator.stream().toList();
            assertFalse(tickets.isEmpty());
        }

        @Test
        void verifyDeleteTicketsFor() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val principal = UUID.randomUUID().toString();
            val ticket = new MockTicketGrantingTicket(principal);
            dynamoDbTicketRegistryFacilitator.put(
                DynamoDbTicketRegistryFacilitator.TicketPayload.builder()
                    .originalTicket(ticket)
                    .encodedTicket(ticket)
                    .principal(principal)
                    .service(RegisteredServiceTestUtils.CONST_TEST_URL)
                    .build());
            val deleted = dynamoDbTicketRegistryFacilitator.deleteTicketsFor(principal);
            assertTrue(deleted >= 1);
            val sessions = dynamoDbTicketRegistryFacilitator.getSessionsFor(principal).toList();
            assertTrue(sessions.isEmpty());
        }

        @Test
        void verifyQueryByPrincipal() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val principal = UUID.randomUUID().toString();
            val ticket = new MockTicketGrantingTicket(principal);
            dynamoDbTicketRegistryFacilitator.put(
                DynamoDbTicketRegistryFacilitator.TicketPayload.builder()
                    .originalTicket(ticket)
                    .encodedTicket(ticket)
                    .principal(principal)
                    .service(RegisteredServiceTestUtils.CONST_TEST_URL)
                    .build());
            val results = dynamoDbTicketRegistryFacilitator.query(
                TicketRegistryQueryCriteria.builder()
                    .principal(principal)
                    .count(10L)
                    .build()).toList();
            assertFalse(results.isEmpty());
        }

        @Test
        void verifyQueryByType() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val ticket = new MockTicketGrantingTicket("casuser");
            dynamoDbTicketRegistryFacilitator.put(
                DynamoDbTicketRegistryFacilitator.TicketPayload.builder()
                    .originalTicket(ticket)
                    .encodedTicket(ticket)
                    .principal("casuser")
                    .service(RegisteredServiceTestUtils.CONST_TEST_URL)
                    .build());
            val results = dynamoDbTicketRegistryFacilitator.query(
                TicketRegistryQueryCriteria.builder()
                    .type(TicketGrantingTicket.PREFIX)
                    .count(10L)
                    .build()).toList();
            assertFalse(results.isEmpty());
        }

        @Test
        void verifyQueryByUnknownType() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val results = dynamoDbTicketRegistryFacilitator.query(
                TicketRegistryQueryCriteria.builder()
                    .type("UNKNOWN-TICKET-TYPE")
                    .count(10L)
                    .build()).toList();
            assertTrue(results.isEmpty());
        }

        @Test
        void verifyQueryWithCountLimit() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val principal = UUID.randomUUID().toString();
            for (var i = 0; i < 3; i++) {
                val t = new MockTicketGrantingTicket(principal);
                dynamoDbTicketRegistryFacilitator.put(
                    DynamoDbTicketRegistryFacilitator.TicketPayload.builder()
                        .originalTicket(t)
                        .encodedTicket(t)
                        .principal(principal)
                        .service(RegisteredServiceTestUtils.CONST_TEST_URL)
                        .build());
            }
            val results = dynamoDbTicketRegistryFacilitator.query(
                TicketRegistryQueryCriteria.builder()
                    .principal(principal)
                    .count(2L)
                    .build()).toList();
            assertTrue(results.size() <= 2);
        }

        @Test
        void verifyBatchPutViaStream() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val payloads = new ArrayList<DynamoDbTicketRegistryFacilitator.TicketPayload>();
            for (var i = 0; i < 5; i++) {
                val t = new MockTicketGrantingTicket("batchuser-" + i);
                payloads.add(DynamoDbTicketRegistryFacilitator.TicketPayload.builder()
                    .originalTicket(t)
                    .encodedTicket(t)
                    .principal("batchuser-" + i)
                    .service(RegisteredServiceTestUtils.CONST_TEST_URL)
                    .build());
            }
            assertDoesNotThrow(() -> dynamoDbTicketRegistryFacilitator.put(payloads.stream()));
            assertTrue(dynamoDbTicketRegistryFacilitator.getAll().size() >= 5);
        }

        @Test
        void verifyGetSessionsWithAttributes() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val principal = UUID.randomUUID().toString();
            val ticket = new MockTicketGrantingTicket(principal);
            dynamoDbTicketRegistryFacilitator.put(
                DynamoDbTicketRegistryFacilitator.TicketPayload.builder()
                    .originalTicket(ticket)
                    .encodedTicket(ticket)
                    .principal(principal)
                    .service(RegisteredServiceTestUtils.CONST_TEST_URL)
                    .build());
            val tgtPrefix = dynamoDbTicketRegistryFacilitator.getTicketCatalog()
                .findTicketDefinition(TicketGrantingTicket.class)
                .orElseThrow()
                .getPrefix();
            val expressionValues = new HashMap<String, AttributeValue>();
            expressionValues.put(":key", AttributeValue.builder().s(tgtPrefix).build());
            val results = dynamoDbTicketRegistryFacilitator.getSessionsWithAttributes(
                StringUtils.EMPTY, new HashMap<>(), expressionValues).toList();
            assertNotNull(results);
            assertFalse(results.isEmpty());
        }

        @Test
        void verifyColumnNamesEnum() {
            for (val column : DynamoDbTicketRegistryFacilitator.ColumnNames.values()) {
                assertNotNull(column.getColumnName());
                assertFalse(column.getColumnName().isBlank());
            }
        }
    }

    @Nested
    @EnabledIfListeningOnPort(port = 8000)
    @TestPropertySource(properties = "cas.ticket.registry.dynamo-db.billing-mode=PAY_PER_REQUEST")
    class DynamoDbTicketRegistryFacilitatorBillingModePayPerRequestTests
        extends BaseDynamoDbTicketRegistryFacilitatorTests {
        @Test
        void verifyCreateTableWithOnDemandBilling() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val client = dynamoDbTicketRegistryFacilitator.getAmazonDynamoDBClient();
            dynamoDbTicketRegistryFacilitator.getTicketCatalog().findAll().forEach(td -> {
                val resp = client.describeTable(DescribeTableRequest.builder()
                    .tableName(td.getProperties().getStorageName())
                    .build());
                assertEquals(BillingMode.PAY_PER_REQUEST, resp.table().billingModeSummary().billingMode());
                val indexNames = resp.table().globalSecondaryIndexes()
                    .stream()
                    .map(GlobalSecondaryIndexDescription::indexName)
                    .toList();
                assertTrue(indexNames.containsAll(List.of("principalExpirationIndex", "serviceExpirationIndex", "prefixExpirationIndex")));
            });
        }
    }
}
