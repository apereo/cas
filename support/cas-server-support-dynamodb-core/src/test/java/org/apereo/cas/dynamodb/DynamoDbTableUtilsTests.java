package org.apereo.cas.dynamodb;

import module java.base;
import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import lombok.val;
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
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;
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

    private static void expectCreateTable(final DynamoDbClient client, final CreateTableRequestArgumentMatcher matcher) {
        when(client.createTable(argThat(matcher)))
            .thenReturn(CreateTableResponse.builder().build());
        val description = TableDescription.builder().tableStatus(TableStatus.ACTIVE).build();
        val table = DescribeTableResponse.builder().table(description).build();
        when(client.describeTable(any(DescribeTableRequest.class))).thenReturn(table);
    }


}
