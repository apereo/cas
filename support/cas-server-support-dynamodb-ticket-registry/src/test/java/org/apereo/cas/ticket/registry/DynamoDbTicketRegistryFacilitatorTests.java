package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.junit.EnabledIfPortOpen;

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DynamoDbTicketRegistryFacilitatorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("DynamoDb")
public class DynamoDbTicketRegistryFacilitatorTests {

    @Nested
    @EnabledIfPortOpen(port = 8000)
    @SuppressWarnings("ClassCanBeStatic")
    public class OriginalDynamoDbTicketRegistryFacilitatorTests extends BaseDynamoDbTicketRegistryFacilitatorTests {
        @Test
        public void verifyBuildAttributeMap() {
            val ticket = new MockTicketGrantingTicket("casuser",
                    CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                    CollectionUtils.wrap("name", "CAS"));
            val map = dynamoDbTicketRegistryFacilitator.buildTableAttributeValuesMapFromTicket(ticket, ticket);
            assertFalse(map.isEmpty());
            Arrays.stream(DynamoDbTicketRegistryFacilitator.ColumnNames.values())
                    .forEach(c -> assertTrue(map.containsKey(c.getColumnName())));
        }

        @Test
        public void verifyTicketOperations() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val ticket = new MockTicketGrantingTicket("casuser",
                    CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword(),
                    CollectionUtils.wrap("name", "CAS"));
            dynamoDbTicketRegistryFacilitator.put(ticket, ticket);
            val col = dynamoDbTicketRegistryFacilitator.getAll();
            assertFalse(col.isEmpty());
            val ticketFetched = dynamoDbTicketRegistryFacilitator.get(ticket.getId(), ticket.getId());
            assertEquals(ticket, ticketFetched);
            assertFalse(dynamoDbTicketRegistryFacilitator.delete("badticket", "badticket"));
            assertTrue(dynamoDbTicketRegistryFacilitator.deleteAll() > 0);

        }
    }

    @Nested
    @EnabledIfPortOpen(port = 8000)
    @TestPropertySource(properties = "cas.ticket.registry.dynamo-db.billing-mode=PAY_PER_REQUEST")
    @SuppressWarnings("ClassCanBeStatic")
    public class DynamoDbTicketRegistryFacilitatorBillingModePayPerRequestTests
            extends BaseDynamoDbTicketRegistryFacilitatorTests {
        @Test
        public void verifyCreateTableWithOnDemandBilling() {
            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val client = dynamoDbTicketRegistryFacilitator.getAmazonDynamoDBClient();
            dynamoDbTicketRegistryFacilitator.getTicketCatalog().findAll().forEach(td -> {
                DescribeTableResponse resp = client.describeTable(DescribeTableRequest.builder()
                        .tableName(td.getProperties().getStorageName())
                        .build());
                val respTD = resp.table();
                assertEquals(BillingMode.PAY_PER_REQUEST, respTD.billingModeSummary().billingMode());

                val throughput = respTD.provisionedThroughput();
                assertEquals(0, throughput.readCapacityUnits());
                assertEquals(0, throughput.writeCapacityUnits());
            });
        }
    }

    @Nested
    @EnabledIfPortOpen(port = 8000)
    @TestPropertySource(properties = {"cas.ticket.registry.dynamo-db.billing-mode=PROVISIONED",
                                        "cas.ticket.registry.dynamo-db.read-capacity=7",
                                        "cas.ticket.registry.dynamo-db.write-capacity=9"})
    @SuppressWarnings("ClassCanBeStatic")
    public class DynamoDbTicketRegistryFacilitatorBillingModeProvisionedTests
            extends BaseDynamoDbTicketRegistryFacilitatorTests {
        @Test
        public void verifyCreateTableWithProvisionedBilling() {

            dynamoDbTicketRegistryFacilitator.createTicketTables(true);
            val client = dynamoDbTicketRegistryFacilitator.getAmazonDynamoDBClient();
            dynamoDbTicketRegistryFacilitator.getTicketCatalog().findAll().forEach(td -> {
                DescribeTableResponse resp = client.describeTable(DescribeTableRequest.builder()
                        .tableName(td.getProperties().getStorageName())
                        .build());
                val respTD = resp.table();
                assertNull(respTD.billingModeSummary());

                val throughput = respTD.provisionedThroughput();
                assertEquals(7, throughput.readCapacityUnits());
                assertEquals(9, throughput.writeCapacityUnits());
            });
        }
    }
}
