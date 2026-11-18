package org.apereo.cas.dynamodb;

import module java.base;
import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndexDescription;
import software.amazon.awssdk.services.dynamodb.model.IndexStatus;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;
import software.amazon.awssdk.services.dynamodb.model.UpdateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateTableResponse;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link DynamoDbTableUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("DynamoDb")
class DynamoDbTableUtilsTests {
    @Test
    void verifyCreateTable() {
        val client = mock(DynamoDbClient.class);
        when(client.createTable(any(CreateTableRequest.class)))
            .thenThrow(SdkException.create("error", new IllegalArgumentException()));
        assertFalse(DynamoDbTableUtils.createTableIfNotExists(client, CreateTableRequest.builder().build()));
    }

    @Test
    void verifyWaitUntilTable() {
        val client = mock(DynamoDbClient.class);
        val description = TableDescription.builder().tableStatus(TableStatus.CREATING).build();
        val table = DescribeTableResponse.builder().table(description).build();
        when(client.describeTable(any(DescribeTableRequest.class))).thenReturn(table);
        assertThrows(DynamoDbTableUtils.TableNeverTransitionedToStateException.class,
            () -> DynamoDbTableUtils.waitUntilActive(client, "tableName", 1000, 1000));
    }

    @Test
    void verifyWaitUntilTableNotFound() {
        val client = mock(DynamoDbClient.class);
        when(client.describeTable(any(DescribeTableRequest.class)))
            .thenThrow(SdkException.create("fail", new IllegalArgumentException()));
        assertThrows(SdkException.class,
            () -> DynamoDbTableUtils.waitUntilActive(client, "tableName", 1000, 1000));

    }

    @Test
    void verifyWaitUntilIndexActive() {
        val client = mock(DynamoDbClient.class);
        when(client.describeTable(any(DescribeTableRequest.class))).thenReturn(
            describeTable(TableStatus.ACTIVE, indexDescription(IndexStatus.CREATING, true)),
            describeTable(TableStatus.ACTIVE, indexDescription(IndexStatus.ACTIVE, true)),
            describeTable(TableStatus.ACTIVE, indexDescription(IndexStatus.ACTIVE, false)));
        val table = DynamoDbTableUtils.waitUntilIndexActive(client, "tableName", "useridIndex", 5000, 10);
        assertEquals(IndexStatus.ACTIVE, table.globalSecondaryIndexes().getFirst().indexStatus());
        verify(client, times(3)).describeTable(any(DescribeTableRequest.class));
    }

    @Test
    void verifyWaitUntilIndexNeverActive() {
        val client = mock(DynamoDbClient.class);
        when(client.describeTable(any(DescribeTableRequest.class)))
            .thenReturn(describeTable(TableStatus.ACTIVE, indexDescription(IndexStatus.CREATING, true)));
        assertThrows(DynamoDbTableUtils.TableNeverTransitionedToStateException.class,
            () -> DynamoDbTableUtils.waitUntilIndexActive(client, "tableName", "useridIndex", 50, 10));
    }

    @Test
    void verifyCreateTableAddsMissingIndexAndWaitsForIt() throws Throwable {
        val client = mock(DynamoDbClient.class);
        when(client.createTable(any(CreateTableRequest.class)))
            .thenThrow(SdkException.create("Table already exists", new IllegalArgumentException()));
        when(client.updateTable(any(UpdateTableRequest.class))).thenReturn(UpdateTableResponse.builder().build());
        when(client.describeTable(any(DescribeTableRequest.class))).thenReturn(
            describeTable(TableStatus.ACTIVE, null),
            describeTable(TableStatus.ACTIVE, null),
            describeTable(TableStatus.ACTIVE, indexDescription(IndexStatus.ACTIVE, false)));

        val index = GlobalSecondaryIndex.builder()
            .indexName("useridIndex")
            .keySchema(KeySchemaElement.builder().attributeName("userid").keyType(KeyType.HASH).build())
            .projection(Projection.builder().projectionType(ProjectionType.ALL).build())
            .build();
        val table = DynamoDbTableUtils.createTable(client,
            new MinimalTestDynamoDbProperties().setBillingMode(AbstractDynamoDbProperties.BillingMode.PAY_PER_REQUEST),
            "tableName", false,
            List.of(
                AttributeDefinition.builder().attributeName("id").attributeType(ScalarAttributeType.N).build(),
                AttributeDefinition.builder().attributeName("userid").attributeType(ScalarAttributeType.S).build()),
            List.of(KeySchemaElement.builder().attributeName("id").keyType(KeyType.HASH).build()),
            List.of(index));

        verify(client).updateTable(argThat((UpdateTableRequest request) ->
            request.globalSecondaryIndexUpdates().getFirst().create().indexName().equals("useridIndex")
                && request.attributeDefinitions().size() == 1
                && request.attributeDefinitions().getFirst().attributeName().equals("userid")));
        assertEquals(IndexStatus.ACTIVE, table.globalSecondaryIndexes().getFirst().indexStatus());
    }

    @Test
    void verifyCreateTableWithBillingModeProvisioned() {
        val client = mock(DynamoDbClient.class);
        val readCapacity = 7L;
        val writeCapacity = 9L;

        val createTableArgMatcher = new CreateTableRequestArgumentMatcher(readCapacity, writeCapacity);
        expectCreateTable(client, createTableArgMatcher);

        val attributeName = "attr1";
        val props = new MinimalTestDynamoDbProperties()
            .setBillingMode(AbstractDynamoDbProperties.BillingMode.PROVISIONED)
            .setReadCapacity(readCapacity)
            .setWriteCapacity(writeCapacity);
        val attributeDefinitions = List.of(
            AttributeDefinition.builder()
                .attributeName(attributeName)
                .attributeType(ScalarAttributeType.S).build());
        val keySchema = List.of(KeySchemaElement.builder()
            .attributeName(attributeName)
            .keyType(KeyType.HASH).build());
        try {
            DynamoDbTableUtils.createTable(client, props, "test-table",
                false, attributeDefinitions, keySchema);
        } catch (final Exception ex) {
            fail("Failed to create table");
        }

        verify(client).createTable(argThat(createTableArgMatcher));
    }

    @Test
    void verifyCreateTableWithBillingModePayPerRequest() {
        val client = mock(DynamoDbClient.class);

        val createTableArgMatcher = new CreateTableRequestArgumentMatcher();

        expectCreateTable(client, createTableArgMatcher);

        val attributeName = "attr1";
        val props = new MinimalTestDynamoDbProperties()
            .setBillingMode(AbstractDynamoDbProperties.BillingMode.PAY_PER_REQUEST);
        val attributeDefinitions = List.of(
            AttributeDefinition.builder().attributeName(attributeName)
                .attributeType(ScalarAttributeType.S).build());
        val keySchema = List.of(KeySchemaElement.builder().attributeName(attributeName)
            .keyType(KeyType.HASH).build());

        try {
            DynamoDbTableUtils.createTable(client, props, "test-table",
                false, attributeDefinitions, keySchema);
        } catch (final Exception ex) {
            fail("Failed to create table");
        }

        verify(client).createTable(argThat(createTableArgMatcher));
    }

    @SuppressWarnings("serial")
    static class MinimalTestDynamoDbProperties extends AbstractDynamoDbProperties {

    }

    static class CreateTableRequestArgumentMatcher implements ArgumentMatcher<CreateTableRequest> {

        private long readCapacity;

        private long writeCapacity;

        CreateTableRequestArgumentMatcher() {
        }

        CreateTableRequestArgumentMatcher(final long readCapacity, final long writeCapacity) {
            this.readCapacity = readCapacity;
            this.writeCapacity = writeCapacity;
        }

        @Override
        public boolean matches(final CreateTableRequest createTableRequest) {
            if (BillingMode.PAY_PER_REQUEST == createTableRequest.billingMode()) {
                return createTableRequest.provisionedThroughput() == null;
            }
            val provisionedThroughput = createTableRequest.provisionedThroughput();
            return provisionedThroughput.readCapacityUnits() == readCapacity
                   && provisionedThroughput.writeCapacityUnits() == writeCapacity;
        }
    }

    private static DescribeTableResponse describeTable(final TableStatus status, final @Nullable GlobalSecondaryIndexDescription index) {
        val description = TableDescription.builder().tableName("tableName").tableStatus(status);
        if (index != null) {
            description.globalSecondaryIndexes(index);
        }
        return DescribeTableResponse.builder().table(description.build()).build();
    }

    private static GlobalSecondaryIndexDescription indexDescription(final IndexStatus status, final boolean backfilling) {
        return GlobalSecondaryIndexDescription.builder()
            .indexName("useridIndex")
            .indexStatus(status)
            .backfilling(backfilling)
            .build();
    }

    private static void expectCreateTable(final DynamoDbClient client, final CreateTableRequestArgumentMatcher matcher) {
        when(client.createTable(argThat(matcher)))
            .thenReturn(CreateTableResponse.builder().build());
        val description = TableDescription.builder().tableStatus(TableStatus.ACTIVE).build();
        val table = DescribeTableResponse.builder().table(description).build();
        when(client.describeTable(any(DescribeTableRequest.class))).thenReturn(table);
    }


}
