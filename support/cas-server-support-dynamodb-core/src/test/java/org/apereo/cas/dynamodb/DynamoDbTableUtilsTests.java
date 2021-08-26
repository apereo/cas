package org.apereo.cas.dynamodb;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
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
public class DynamoDbTableUtilsTests {
    @Test
    public void verifyCreateTable() {
        val client = mock(DynamoDbClient.class);
        when(client.createTable(any(CreateTableRequest.class)))
            .thenThrow(ResourceInUseException.create("error", new IllegalArgumentException()));
        assertFalse(DynamoDbTableUtils.createTableIfNotExists(client, CreateTableRequest.builder().build()));
    }

    @Test
    public void verifyWaitUntilTable() {
        val client = mock(DynamoDbClient.class);
        val description = TableDescription.builder().tableStatus(TableStatus.CREATING).build();
        val table = DescribeTableResponse.builder().table(description).build();
        when(client.describeTable(any(DescribeTableRequest.class))).thenReturn(table);
        assertThrows(DynamoDbTableUtils.TableNeverTransitionedToStateException.class,
            () -> DynamoDbTableUtils.waitUntilActive(client, "tableName", 1000, 1000));
    }

    @Test
    public void verifyWaitUntilTableNotFound() {
        val client = mock(DynamoDbClient.class);
        when(client.describeTable(any(DescribeTableRequest.class)))
            .thenThrow(ResourceNotFoundException.create("fail", new IllegalArgumentException()));
        assertThrows(SdkException.class,
            () -> DynamoDbTableUtils.waitUntilActive(client, "tableName", 1000, 1000));

    }

}
