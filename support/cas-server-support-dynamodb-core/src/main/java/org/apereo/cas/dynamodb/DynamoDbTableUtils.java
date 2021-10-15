package org.apereo.cas.dynamodb;

import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.util.LoggingUtils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BillingMode;
import software.amazon.awssdk.services.dynamodb.model.Condition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link DynamoDbTableUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@UtilityClass
@Slf4j
public class DynamoDbTableUtils {
    private static final int DEFAULT_WAIT_TIMEOUT = 10 * 60 * 1000;

    private static final int DEFAULT_WAIT_INTERVAL = 10 * 1000;

    /**
     * Wait until active.
     *
     * @param dynamo    the dynamo
     * @param tableName the table name
     * @throws Exception the exception
     */
    public static void waitUntilActive(final DynamoDbClient dynamo, final String tableName) throws Exception {
        waitUntilActive(dynamo, tableName, DEFAULT_WAIT_TIMEOUT, DEFAULT_WAIT_INTERVAL);
    }

    /**
     * Wait until active.
     *
     * @param dynamo    the dynamo
     * @param tableName the table name
     * @param timeout   the timeout
     * @param interval  the interval
     * @throws Exception the exception
     */
    public static void waitUntilActive(final DynamoDbClient dynamo, final String tableName, final int timeout,
                                       final int interval) throws Exception {
        val table = waitForTableDescription(dynamo, tableName, TableStatus.ACTIVE, timeout, interval);

        if (table == null || !table.tableStatusAsString().equals(TableStatus.ACTIVE.toString())) {
            throw new TableNeverTransitionedToStateException(tableName, TableStatus.ACTIVE);
        }
    }

    /**
     * Creates the table and ignores any errors if it already exists.
     *
     * @param dynamo             The Dynamo client to use.
     * @param createTableRequest The create table request.
     * @return True if created, false otherwise.
     */
    public static boolean createTableIfNotExists(final DynamoDbClient dynamo, final CreateTableRequest createTableRequest) {
        try {
            dynamo.createTable(createTableRequest);
            return true;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return false;
    }

    /**
     * Deletes the table and ignores any errors if it doesn't exist.
     *
     * @param dynamo             The Dynamo client to use.
     * @param deleteTableRequest The delete table request.
     * @return True if deleted, false otherwise.
     */
    public static boolean deleteTableIfExists(final DynamoDbClient dynamo, final DeleteTableRequest deleteTableRequest) {
        try {
            dynamo.deleteTable(deleteTableRequest);
            return true;
        } catch (final ResourceNotFoundException e) {
            LOGGER.trace(e.getMessage());
        }
        return false;
    }

    /**
     * Create table.
     *
     * @param dynamoDbClient       the dynamo db client
     * @param dynamoDbProperties   the dynamo db properties
     * @param tableName            the table name
     * @param deleteTable          the delete tables
     * @param attributeDefinitions the attribute definitions
     * @param keySchemaElements    the key schema elements
     * @throws Exception the exception
     */
    public static void createTable(final DynamoDbClient dynamoDbClient,
                                   final AbstractDynamoDbProperties dynamoDbProperties,
                                   final String tableName,
                                   final boolean deleteTable,
                                   final List<AttributeDefinition> attributeDefinitions,
                                   final List<KeySchemaElement> keySchemaElements) throws Exception {
        val throughput = ProvisionedThroughput.builder()
            .readCapacityUnits(dynamoDbProperties.getReadCapacity())
            .writeCapacityUnits(dynamoDbProperties.getWriteCapacity())
            .build();
        val request = CreateTableRequest.builder()
            .attributeDefinitions(attributeDefinitions)
            .keySchema(keySchemaElements)
            .provisionedThroughput(throughput)
            .tableName(tableName)
            .billingMode(BillingMode.fromValue(dynamoDbProperties.getBillingMode().name()))
            .build();

        if (deleteTable) {
            val delete = DeleteTableRequest.builder().tableName(tableName).build();
            LOGGER.debug("Sending delete request [{}] to remove table if necessary", delete);
            deleteTableIfExists(dynamoDbClient, delete);
        }
        LOGGER.debug("Sending create request [{}] to create table", request);
        createTableIfNotExists(dynamoDbClient, request);
        LOGGER.debug("Waiting until table [{}] becomes active...", request.tableName());
        waitUntilActive(dynamoDbClient, request.tableName());
        val describeTableRequest = DescribeTableRequest.builder().tableName(request.tableName()).build();
        LOGGER.debug("Sending request [{}] to obtain table description...", describeTableRequest);
        val tableDescription = dynamoDbClient.describeTable(describeTableRequest).table();
        LOGGER.debug("Located newly created table with description: [{}]", tableDescription);
    }

    /**
     * Gets records by keys.
     *
     * @param <T>            the type parameter
     * @param dynamoDbClient the dynamo db client
     * @param tableName      the table name
     * @param queries        the queries
     * @param itemMapper     the item mapper
     * @return the records by keys
     */
    public static <T> Stream<T> getRecordsByKeys(final DynamoDbClient dynamoDbClient,
                                                 final String tableName,
                                                 final List<DynamoDbQueryBuilder> queries,
                                                 final Function<Map<String, AttributeValue>, T> itemMapper) {
        try {
            val scanFilter = queries.stream()
                .map(query -> {
                    val cond = Condition.builder()
                        .comparisonOperator(query.getOperator())
                        .attributeValueList(query.getAttributeValue())
                        .build();
                    return Pair.of(query.getKey(), cond);
                })
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
            val scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .scanFilter(scanFilter)
                .build();
            LOGGER.debug("Submitting request [{}] to get record with keys [{}]", scanRequest, queries);
            val items = dynamoDbClient.scan(scanRequest).items();
            return items
                .stream()
                .map(itemMapper);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return Stream.empty();
    }

    private static TableDescription waitForTableDescription(final DynamoDbClient dynamo,
                                                            final String tableName,
                                                            final TableStatus desiredStatus,
                                                            final int timeout,
                                                            final int interval)
        throws Exception {
        val startTime = System.currentTimeMillis();
        val endTime = startTime + timeout;

        val tableRequest = DescribeTableRequest.builder().tableName(tableName).build();
        TableDescription table = null;
        while (System.currentTimeMillis() < endTime) {
            try {
                table = dynamo.describeTable(tableRequest).table();
                if (desiredStatus == null || table.tableStatusAsString().equals(desiredStatus.toString())) {
                    return table;
                }
            } catch (final ResourceNotFoundException rnfe) {
                LOGGER.trace(rnfe.getMessage());
            }
            Thread.sleep(interval);
        }
        return table;
    }

    static class TableNeverTransitionedToStateException extends SdkClientException {

        private static final long serialVersionUID = 8920567021104846647L;

        /**
         * Instantiates a new Table never transitioned to state exception.
         *
         * @param tableName     the table name
         * @param desiredStatus the desired status
         */
        TableNeverTransitionedToStateException(final String tableName, final TableStatus desiredStatus) {
            super(TableNeverTransitionedToStateException
                .builder()
                .message("Table " + tableName + " never transitioned to desired state of " + desiredStatus.toString()));
        }

    }
}
