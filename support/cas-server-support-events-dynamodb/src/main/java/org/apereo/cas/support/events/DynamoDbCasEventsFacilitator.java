package org.apereo.cas.support.events;

import org.apereo.cas.configuration.model.core.events.DynamoDbEventsProperties;
import org.apereo.cas.dynamodb.DynamoDbQueryBuilder;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.util.LoggingUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.Condition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DynamoDbCasEventsFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class DynamoDbCasEventsFacilitator {
    private static final ObjectMapper MAPPER = new ObjectMapper()
        .setDefaultPrettyPrinter(new MinimalPrettyPrinter())
        .findAndRegisterModules();

    private final DynamoDbEventsProperties dynamoDbProperties;

    private final DynamoDbClient amazonDynamoDBClient;

    /**
     * Create tables.
     *
     * @param deleteTables the delete tables
     */
    @SneakyThrows
    public void createTable(final boolean deleteTables) {
        val throughput = ProvisionedThroughput.builder()
            .readCapacityUnits(dynamoDbProperties.getReadCapacity())
            .writeCapacityUnits(dynamoDbProperties.getWriteCapacity())
            .build();

        val request = CreateTableRequest.builder()
            .attributeDefinitions(AttributeDefinition.builder().attributeName(ColumnNames.ID.getColumnName()).attributeType(ScalarAttributeType.N).build())
            .keySchema(KeySchemaElement.builder().attributeName(ColumnNames.ID.getColumnName()).keyType(KeyType.HASH).build())
            .provisionedThroughput(throughput)
            .tableName(dynamoDbProperties.getTableName())
            .build();
        if (deleteTables) {
            val delete = DeleteTableRequest.builder().tableName(dynamoDbProperties.getTableName()).build();
            LOGGER.debug("Sending delete request [{}] to remove table if necessary", delete);
            DynamoDbTableUtils.deleteTableIfExists(amazonDynamoDBClient, delete);
        }
        LOGGER.debug("Sending create request [{}] to create table", request);
        DynamoDbTableUtils.createTableIfNotExists(amazonDynamoDBClient, request);
        LOGGER.debug("Waiting until table [{}] becomes active...", request.tableName());
        DynamoDbTableUtils.waitUntilActive(amazonDynamoDBClient, request.tableName());
        val describeTableRequest = DescribeTableRequest.builder().tableName(request.tableName()).build();
        LOGGER.debug("Sending request [{}] to obtain table description...", describeTableRequest);
        val tableDescription = amazonDynamoDBClient.describeTable(describeTableRequest).table();
        LOGGER.debug("Located newly created table with description: [{}]", tableDescription);
    }

    /**
     * Save.
     *
     * @param record the record
     */
    public void save(final CasEvent record) {
        val values = buildTableAttributeValuesMap(record);
        val putItemRequest = PutItemRequest.builder().tableName(dynamoDbProperties.getTableName()).item(values).build();
        LOGGER.debug("Submitting put request [{}] for record [{}]", putItemRequest, record);
        val putItemResult = amazonDynamoDBClient.putItem(putItemRequest);
        LOGGER.debug("Record added with result [{}]", putItemResult);
    }

    public Set<CasEvent> getAll() {
        return getRecordsByKeys(List.of());
    }

    /**
     * Delete all.
     */
    public void deleteAll() {
        createTable(true);
    }

    /**
     * Gets events for principal.
     *
     * @param id the id
     * @return the events for principal
     */
    public Collection<? extends CasEvent> getEventsForPrincipal(final String id) {
        val query = DynamoDbQueryBuilder.builder()
            .key(ColumnNames.PRINCIPAL.getColumnName())
            .attributeValue(List.of(AttributeValue.builder().s(id).build()))
            .operator(ComparisonOperator.EQ)
            .build();
        return getRecordsByKeys(query);
    }
    
    /**
     * Gets events for principal.
     *
     * @param id       the id
     * @param dateTime the date time
     * @return the events for principal
     */
    public Collection<? extends CasEvent> getEventsForPrincipal(final String id, final ZonedDateTime dateTime) {
        val query =
            List.of(
                DynamoDbQueryBuilder.builder()
                    .key(ColumnNames.PRINCIPAL.getColumnName())
                    .attributeValue(List.of(AttributeValue.builder().s(id).build()))
                    .operator(ComparisonOperator.EQ)
                    .build(),
                DynamoDbQueryBuilder.builder()
                    .key(ColumnNames.CREATION_TIME.getColumnName())
                    .attributeValue(List.of(AttributeValue.builder().s(dateTime.toString()).build()))
                    .operator(ComparisonOperator.GE)
                    .build());
        return getRecordsByKeys(query);
    }

    /**
     * Gets events of type.
     *
     * @param type     the type
     * @param dateTime the date time
     * @return the events of type
     */
    public Collection<? extends CasEvent> getEventsOfType(final String type, final ZonedDateTime dateTime) {
        val query = List.of(
            DynamoDbQueryBuilder.builder()
                .key(ColumnNames.TYPE.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().s(type).build()))
                .operator(ComparisonOperator.EQ)
                .build(),
            DynamoDbQueryBuilder.builder()
                .key(ColumnNames.CREATION_TIME.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().s(dateTime.toString()).build()))
                .operator(ComparisonOperator.GE)
                .build());
        return getRecordsByKeys(query);
    }

    /**
     * Gets events of type.
     *
     * @param type the type
     * @return the events of type
     */
    public Collection<? extends CasEvent> getEventsOfType(final String type) {
        val query = DynamoDbQueryBuilder.builder()
            .key(ColumnNames.TYPE.getColumnName())
            .attributeValue(List.of(AttributeValue.builder().s(type).build()))
            .operator(ComparisonOperator.EQ)
            .build();
        return getRecordsByKeys(query);
    }

    /**
     * Gets events of type for principal.
     *
     * @param type      the type
     * @param principal the principal
     * @param dateTime  the date time
     * @return the events of type for principal
     */
    public Collection<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal,
                                                                      final ZonedDateTime dateTime) {
        val query = List.of(
            DynamoDbQueryBuilder.builder()
                .key(ColumnNames.TYPE.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().s(type).build()))
                .operator(ComparisonOperator.EQ)
                .build(),
            DynamoDbQueryBuilder.builder()
                .key(ColumnNames.PRINCIPAL.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().s(principal).build()))
                .operator(ComparisonOperator.EQ)
                .build(),
            DynamoDbQueryBuilder.builder()
                .key(ColumnNames.CREATION_TIME.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().s(dateTime.toString()).build()))
                .operator(ComparisonOperator.GE)
                .build());
        return getRecordsByKeys(query);
    }

    /**
     * Gets events of type for principal.
     *
     * @param type      the type
     * @param principal the principal
     * @return the events of type for principal
     */
    public Collection<? extends CasEvent> getEventsOfTypeForPrincipal(final String type, final String principal) {
        val query =
            List.of(
                DynamoDbQueryBuilder.builder()
                    .key(ColumnNames.PRINCIPAL.getColumnName())
                    .attributeValue(List.of(AttributeValue.builder().s(principal).build()))
                    .operator(ComparisonOperator.EQ)
                    .build(),
                DynamoDbQueryBuilder.builder()
                    .key(ColumnNames.TYPE.getColumnName())
                    .attributeValue(List.of(AttributeValue.builder().s(type).build()))
                    .operator(ComparisonOperator.EQ)
                    .build());
        return getRecordsByKeys(query);
    }

    /**
     * Build table attribute values map.
     *
     * @param record the record
     * @return the map
     */
    @SneakyThrows
    private static Map<String, AttributeValue> buildTableAttributeValuesMap(final CasEvent record) {
        val values = new HashMap<String, AttributeValue>();
        values.put(ColumnNames.PRINCIPAL.getColumnName(), AttributeValue.builder().s(record.getPrincipalId()).build());
        values.put(ColumnNames.ID.getColumnName(), AttributeValue.builder().n(String.valueOf(record.getId())).build());
        values.put(ColumnNames.CREATION_TIME.getColumnName(), AttributeValue.builder().s(record.getCreationTime()).build());
        values.put(ColumnNames.TYPE.getColumnName(), AttributeValue.builder().s(record.getType()).build());
        val properties = MAPPER.writeValueAsString(record.getProperties());
        values.put(ColumnNames.PROPERTIES.getColumnName(), AttributeValue.builder().s(properties).build());
        LOGGER.debug("Created attribute values [{}] based on [{}]", values, record);
        return values;
    }

    @SneakyThrows
    private Set<CasEvent> getRecordsByKeys(final DynamoDbQueryBuilder... queries) {
        return getRecordsByKeys(Arrays.stream(queries).collect(Collectors.toList()));
    }

    @SneakyThrows
    private Set<CasEvent> getRecordsByKeys(final List<DynamoDbQueryBuilder> queries) {
        try {
            var scanRequest = ScanRequest.builder()
                .tableName(dynamoDbProperties.getTableName())
                .scanFilter(queries.stream()
                    .map(query -> {
                        val cond = Condition.builder().comparisonOperator(query.getOperator()).attributeValueList(query.getAttributeValue()).build();
                        return Pair.of(query.getKey(), cond);
                    })
                    .collect(Collectors.toMap(Pair::getKey, Pair::getValue)))
                .build();
            
            LOGGER.debug("Submitting request [{}] to get record with keys [{}]", scanRequest, queries);
            val items = amazonDynamoDBClient.scan(scanRequest).items();
            return items
                .stream()
                .map(DynamoDbCasEventsFacilitator::extractAttributeValuesFrom)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new HashSet<>(0);
    }

    @SneakyThrows
    private static CasEvent extractAttributeValuesFrom(final Map<String, AttributeValue> item) {
        val principal = item.get(ColumnNames.PRINCIPAL.getColumnName()).s();
        val id = Long.valueOf(item.get(ColumnNames.ID.getColumnName()).n());
        val type = item.get(ColumnNames.TYPE.getColumnName()).s();
        val creationTime = item.get(ColumnNames.CREATION_TIME.getColumnName()).s();
        val properties = MAPPER.readValue(item.get(ColumnNames.PROPERTIES.getColumnName()).s(),
            new TypeReference<Map<String, String>>() {
            });
        return new CasEvent(id, type, principal, creationTime, properties);
    }

    /**
     * Column names for tables holding records.
     */
    @Getter
    @RequiredArgsConstructor
    public enum ColumnNames {
        /**
         * principal column.
         */
        PRINCIPAL("principal"),
        /**
         * type column.
         */
        TYPE("type"),
        /**
         * id column.
         */
        ID("id"),
        /**
         * properties column.
         */
        PROPERTIES("properties"),
        /**
         * properties column.
         */
        CREATION_TIME("creationTime");

        private final String columnName;
    }
}
