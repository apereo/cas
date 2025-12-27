package org.apereo.cas.dynamodb;

import module java.base;
import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
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
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import software.amazon.awssdk.services.dynamodb.model.TableStatus;
import software.amazon.awssdk.services.dynamodb.model.TimeToLiveSpecification;
import software.amazon.awssdk.services.dynamodb.model.UpdateTimeToLiveRequest;

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
     * @return the table description
     */
    public static TableDescription waitUntilActive(final DynamoDbClient dynamo, final String tableName) {
        return waitUntilActive(dynamo, tableName, DEFAULT_WAIT_TIMEOUT, DEFAULT_WAIT_INTERVAL);
    }

    /**
     * Wait until active.
     *
     * @param dynamo    the dynamo
     * @param tableName the table name
     * @param timeout   the timeout
     * @param interval  the interval
     * @return the table description
     */
    public static TableDescription waitUntilActive(final DynamoDbClient dynamo, final String tableName,
                                                   final int timeout,
                                                   final int interval) {
        val table = waitForTableDescription(dynamo, tableName, TableStatus.ACTIVE, timeout, interval);

        if (table == null || !table.tableStatusAsString().equals(TableStatus.ACTIVE.toString())) {
            throw new TableNeverTransitionedToStateException(tableName, TableStatus.ACTIVE);
        }
        return table;
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
     * @return the table description
     * @throws Exception the exception
     */
    public static TableDescription createTable(final DynamoDbClient dynamoDbClient,
                                               final AbstractDynamoDbProperties dynamoDbProperties,
                                               final String tableName,
                                               final boolean deleteTable,
                                               final List<AttributeDefinition> attributeDefinitions,
                                               final List<KeySchemaElement> keySchemaElements) throws Exception {

        val provisionedThroughput = getProvisionedThroughput(dynamoDbProperties);
        val request = CreateTableRequest.builder()
            .attributeDefinitions(attributeDefinitions)
            .keySchema(keySchemaElements)
            .provisionedThroughput(provisionedThroughput)
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
        return tableDescription;
    }

    private static ProvisionedThroughput getProvisionedThroughput(final AbstractDynamoDbProperties dynamoDbProperties) {
        val billingMode = BillingMode.fromValue(dynamoDbProperties.getBillingMode().name());
        return billingMode == BillingMode.PROVISIONED
            ? ProvisionedThroughput.builder()
            .readCapacityUnits(dynamoDbProperties.getReadCapacity())
            .writeCapacityUnits(dynamoDbProperties.getWriteCapacity())
            .build()
            : null;
    }

    /**
     * Enable time to live on table.
     *
     * @param dynamoDbClient   the dynamo db client
     * @param tableName        the table name
     * @param ttlAttributeName the ttl attribute name
     */
    public static void enableTimeToLiveOnTable(final DynamoDbClient dynamoDbClient,
                                               final String tableName,
                                               final String ttlAttributeName) {
        FunctionUtils.doAndHandle(_ -> {
            val ttlSpec = TimeToLiveSpecification.builder()
                .attributeName(ttlAttributeName)
                .enabled(true)
                .build();
            val request = UpdateTimeToLiveRequest.builder()
                .tableName(tableName)
                .timeToLiveSpecification(ttlSpec)
                .build();
            dynamoDbClient.updateTimeToLive(request);
        });
    }

    /**
     * Scan via filter expressions and respond.
     *
     * @param dynamoDbClient          the dynamo db client
     * @param tableName               the table name
     * @param filterExpression        the filter expression
     * @param expressionAttributeName the expression attribute name
     * @param expressionValues        the expression values
     * @return the scan response
     */
    public static ScanResponse scan(final DynamoDbClient dynamoDbClient,
                                    final String tableName,
                                    final String filterExpression,
                                    final Map<String, String> expressionAttributeName,
                                    final Map<String, AttributeValue> expressionValues) {
        return FunctionUtils.doAndHandle(() -> {
            val scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .filterExpression(filterExpression)
                .expressionAttributeValues(expressionValues)
                .expressionAttributeNames(expressionAttributeName)
                .build();
            LOGGER.debug("Submitting request [{}] to get record with expression filters [{}]", scanRequest, filterExpression);
            return dynamoDbClient.scan(scanRequest);
        }, e -> ScanResponse.builder().items(Map.of()).build()).get();
    }

    /**
     * Scan and build response.
     *
     * @param dynamoDbClient the dynamo db client
     * @param tableName      the table name
     * @param queries        the queries
     * @return the scan response
     */
    public static ScanResponse scan(final DynamoDbClient dynamoDbClient,
                                    final String tableName,
                                    final List<? extends DynamoDbQueryBuilder> queries) {
        return scan(dynamoDbClient, tableName, -1, queries);
    }

    /**
     * Scan scan response.
     *
     * @param dynamoDbClient the dynamo db client
     * @param tableName      the table name
     * @param count          the count
     * @param queries        the queries
     * @return the scan response
     */
    public static ScanResponse scan(final DynamoDbClient dynamoDbClient,
                                    final String tableName,
                                    final long count,
                                    final List<? extends DynamoDbQueryBuilder> queries) {
        try {
            val scanFilter = buildRequestQueryFilter(queries);
            val scanRequestBuilder = ScanRequest.builder().tableName(tableName).scanFilter(scanFilter);
            if (count > 0) {
                scanRequestBuilder.limit((int) count);
            }
            val scanRequest = scanRequestBuilder.build();
            LOGGER.debug("Submitting request [{}] to get record with keys [{}]", scanRequest, queries);
            return dynamoDbClient.scan(scanRequest);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return ScanResponse.builder().items(Map.of()).build();
    }

    /**
     * Build request query filter map.
     *
     * @param queries the queries
     * @return the map
     */
    public static Map<String, Condition> buildRequestQueryFilter(final List<? extends DynamoDbQueryBuilder> queries) {
        return queries
            .stream()
            .map(query -> {
                val cond = Condition.builder()
                    .comparisonOperator(query.getOperator())
                    .attributeValueList(query.getAttributeValue())
                    .build();
                return Pair.of(query.getKey(), cond);
            })
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
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
                                                 final List<? extends DynamoDbQueryBuilder> queries,
                                                 final Function<Map<String, AttributeValue>, T> itemMapper) {
        return getRecordsByKeys(dynamoDbClient, tableName, -1, queries, itemMapper);
    }

    /**
     * Gets records by keys.
     *
     * @param <T>            the type parameter
     * @param dynamoDbClient the dynamo db client
     * @param tableName      the table name
     * @param count          the count
     * @param queries        the queries
     * @param itemMapper     the item mapper
     * @return the records by keys
     */
    public static <T> Stream<T> getRecordsByKeys(final DynamoDbClient dynamoDbClient,
                                                 final String tableName,
                                                 final long count,
                                                 final List<? extends DynamoDbQueryBuilder> queries,
                                                 final Function<Map<String, AttributeValue>, T> itemMapper) {
        val scanResponse = scan(dynamoDbClient, tableName, count, queries);
        val items = scanResponse.items();
        return items.stream().map(itemMapper);
    }

    private static TableDescription waitForTableDescription(final DynamoDbClient dynamo,
                                                            final String tableName,
                                                            final TableStatus desiredStatus,
                                                            final int timeout,
                                                            final int interval) {
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
            FunctionUtils.doUnchecked(_ -> Thread.sleep(interval));
        }
        return table;
    }

    /**
     * Stream and scan using pagination.
     *
     * @param <T>                  the type parameter
     * @param amazonDynamoDBClient the amazon dynamo db client
     * @param tableName            the table name
     * @param keys                 the keys
     * @param itemMapper           the item mapper
     * @return the stream
     */
    public static <T> Stream<T> scanPaginator(final DynamoDbClient amazonDynamoDBClient,
                                              final String tableName,
                                              final List<DynamoDbQueryBuilder> keys,
                                              final Function<Map<String, AttributeValue>, T> itemMapper) {
        return scanPaginator(amazonDynamoDBClient, tableName, 0L, keys, itemMapper);
    }

    /**
     * Scan paginator and return stream.
     *
     * @param <T>                  the type parameter
     * @param amazonDynamoDBClient the amazon dynamo db client
     * @param tableName            the table name
     * @param limit                the limit
     * @param keys                 the keys
     * @param itemMapper           the item mapper
     * @return the stream
     */
    public static <T> Stream<T> scanPaginator(final DynamoDbClient amazonDynamoDBClient,
                                              final String tableName,
                                              final Long limit,
                                              final List<DynamoDbQueryBuilder> keys,
                                              final Function<Map<String, AttributeValue>, T> itemMapper) {
        val scanRequest = ScanRequest.builder()
            .tableName(tableName)
            .limit(limit > 0 ? limit.intValue() : Integer.MAX_VALUE)
            .scanFilter(DynamoDbTableUtils.buildRequestQueryFilter(keys))
            .build();
        LOGGER.debug("Scanning table with scan request [{}]", scanRequest);
        return amazonDynamoDBClient.scanPaginator(scanRequest)
            .stream()
            .flatMap(results -> results.items().stream())
            .map(itemMapper)
            .filter(Objects::nonNull);
    }

    public static class TableNeverTransitionedToStateException extends RuntimeException {

        @Serial
        private static final long serialVersionUID = 8920567021104846647L;

        TableNeverTransitionedToStateException(final String tableName, final TableStatus desiredStatus) {
            super("Table " + tableName + " never transitioned to desired state of " + desiredStatus.toString());
        }

    }
}
