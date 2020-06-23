package org.apereo.cas.support.events;

import org.apereo.cas.configuration.model.core.events.DynamoDbEventsProperties;
import org.apereo.cas.support.events.dao.CasEvent;
import org.apereo.cas.util.LoggingUtils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

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

    private final AmazonDynamoDB amazonDynamoDBClient;

    /**
     * Create tables.
     *
     * @param deleteTables the delete tables
     */
    @SneakyThrows
    public void createTable(final boolean deleteTables) {
        val request = new CreateTableRequest()
            .withAttributeDefinitions(new AttributeDefinition(ColumnNames.ID.getColumnName(), ScalarAttributeType.N))
            .withKeySchema(new KeySchemaElement(ColumnNames.ID.getColumnName(), KeyType.HASH))
            .withProvisionedThroughput(new ProvisionedThroughput(dynamoDbProperties.getReadCapacity(),
                dynamoDbProperties.getWriteCapacity())).withTableName(dynamoDbProperties.getTableName());
        if (deleteTables) {
            val delete = new DeleteTableRequest(dynamoDbProperties.getTableName());
            LOGGER.debug("Sending delete request [{}] to remove table if necessary", delete);
            TableUtils.deleteTableIfExists(amazonDynamoDBClient, delete);
        }
        LOGGER.debug("Sending delete request [{}] to create table", request);
        TableUtils.createTableIfNotExists(amazonDynamoDBClient, request);
        LOGGER.debug("Waiting until table [{}] becomes active...", request.getTableName());
        TableUtils.waitUntilActive(amazonDynamoDBClient, request.getTableName());
        val describeTableRequest = new DescribeTableRequest().withTableName(request.getTableName());
        LOGGER.debug("Sending request [{}] to obtain table description...", describeTableRequest);
        val tableDescription = amazonDynamoDBClient.describeTable(describeTableRequest).getTable();
        LOGGER.debug("Located newly created table with description: [{}]", tableDescription);
    }

    /**
     * Save.
     *
     * @param record the record
     */
    public void save(final CasEvent record) {
        val values = buildTableAttributeValuesMap(record);
        val putItemRequest = new PutItemRequest(dynamoDbProperties.getTableName(), values);
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
        val query = DynamoDbQuery.builder()
            .key(ColumnNames.PRINCIPAL.getColumnName())
            .attributeValue(List.of(new AttributeValue(id)))
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
                DynamoDbQuery.builder()
                    .key(ColumnNames.PRINCIPAL.getColumnName())
                    .attributeValue(List.of(new AttributeValue(id)))
                    .operator(ComparisonOperator.EQ)
                    .build(),
                DynamoDbQuery.builder()
                    .key(ColumnNames.CREATION_TIME.getColumnName())
                    .attributeValue(List.of(new AttributeValue(dateTime.toString())))
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
            DynamoDbQuery.builder()
                .key(ColumnNames.TYPE.getColumnName())
                .attributeValue(List.of(new AttributeValue(type)))
                .operator(ComparisonOperator.EQ)
                .build(),
            DynamoDbQuery.builder()
                .key(ColumnNames.CREATION_TIME.getColumnName())
                .attributeValue(List.of(new AttributeValue(dateTime.toString())))
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
        val query = DynamoDbQuery.builder()
            .key(ColumnNames.TYPE.getColumnName())
            .attributeValue(List.of(new AttributeValue(type)))
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
            DynamoDbQuery.builder()
                .key(ColumnNames.TYPE.getColumnName())
                .attributeValue(List.of(new AttributeValue(type)))
                .operator(ComparisonOperator.EQ)
                .build(),
            DynamoDbQuery.builder()
                .key(ColumnNames.PRINCIPAL.getColumnName())
                .attributeValue(List.of(new AttributeValue(principal)))
                .operator(ComparisonOperator.EQ)
                .build(),
            DynamoDbQuery.builder()
                .key(ColumnNames.CREATION_TIME.getColumnName())
                .attributeValue(List.of(new AttributeValue(dateTime.toString())))
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
                DynamoDbQuery.builder()
                    .key(ColumnNames.PRINCIPAL.getColumnName())
                    .attributeValue(List.of(new AttributeValue(principal)))
                    .operator(ComparisonOperator.EQ)
                    .build(),
                DynamoDbQuery.builder()
                    .key(ColumnNames.TYPE.getColumnName())
                    .attributeValue(List.of(new AttributeValue(type)))
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
        values.put(ColumnNames.PRINCIPAL.getColumnName(), new AttributeValue(record.getPrincipalId()));

        val id = new AttributeValue();
        id.setN(String.valueOf(record.getId()));
        values.put(ColumnNames.ID.getColumnName(), id);

        values.put(ColumnNames.CREATION_TIME.getColumnName(), new AttributeValue(record.getCreationTime()));
        values.put(ColumnNames.TYPE.getColumnName(), new AttributeValue(record.getType()));

        val properties = MAPPER.writeValueAsString(record.getProperties());
        values.put(ColumnNames.PROPERTIES.getColumnName(), new AttributeValue(properties));
        LOGGER.debug("Created attribute values [{}] based on [{}]", values, record);
        return values;
    }

    @SneakyThrows
    private Set<CasEvent> getRecordsByKeys(final DynamoDbQuery... queries) {
        return getRecordsByKeys(Arrays.stream(queries).collect(Collectors.toList()));
    }

    @SneakyThrows
    private Set<CasEvent> getRecordsByKeys(final List<DynamoDbQuery> queries) {
        try {
            val scanRequest = new ScanRequest(dynamoDbProperties.getTableName());
            queries.forEach(query -> {
                val cond = new Condition();
                cond.setComparisonOperator(query.getOperator());
                cond.setAttributeValueList(query.getAttributeValue());
                scanRequest.addScanFilterEntry(query.getKey(), cond);
            });

            LOGGER.debug("Submitting request [{}] to get record with keys [{}]", scanRequest, queries);
            val items = amazonDynamoDBClient.scan(scanRequest).getItems();
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
        val principal = item.get(ColumnNames.PRINCIPAL.getColumnName()).getS();
        val id = Long.valueOf(item.get(ColumnNames.ID.getColumnName()).getN());
        val type = item.get(ColumnNames.TYPE.getColumnName()).getS();
        val creationTime = item.get(ColumnNames.CREATION_TIME.getColumnName()).getS();
        val properties = MAPPER.readValue(item.get(ColumnNames.PROPERTIES.getColumnName()).getS(),
            new TypeReference<Map<String, String>>() {
            });
        return new CasEvent(id, type, principal, creationTime, properties);
    }

    /**
     * Column names for tables holding records.
     */
    @Getter
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

        ColumnNames(final String columnName) {
            this.columnName = columnName;
        }
    }

    @Getter
    @Builder
    private static class DynamoDbQuery {
        private final String key;

        private final List<AttributeValue> attributeValue;

        private final ComparisonOperator operator;
    }
}
