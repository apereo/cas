package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.configuration.model.support.mfa.u2f.U2FDynamoDbMultifactorProperties;
import org.apereo.cas.dynamodb.DynamoDbQueryBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link U2FDynamoDbFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class U2FDynamoDbFacilitator {
    private final U2FDynamoDbMultifactorProperties dynamoDbProperties;

    private final AmazonDynamoDB amazonDynamoDBClient;

    public U2FDynamoDbFacilitator(final U2FDynamoDbMultifactorProperties dynamoDbProperties,
                                  final AmazonDynamoDB amazonDynamoDBClient) {
        this.dynamoDbProperties = dynamoDbProperties;
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        if (!dynamoDbProperties.isPreventTableCreationOnStartup()) {
            createTable(dynamoDbProperties.isDropTablesOnStartup());
        }
    }

    /**
     * Create tables.
     *
     * @param deleteTables the delete tables
     */
    @SneakyThrows
    public void createTable(final boolean deleteTables) {
        LOGGER.debug("Attempting to create DynamoDb table");
        val request = new CreateTableRequest().withAttributeDefinitions(
            new AttributeDefinition(ColumnNames.ID.getColumnName(), ScalarAttributeType.N))
            .withKeySchema(new KeySchemaElement(ColumnNames.ID.getColumnName(), KeyType.HASH))
            .withProvisionedThroughput(new ProvisionedThroughput(dynamoDbProperties.getReadCapacity(),
                dynamoDbProperties.getWriteCapacity())).withTableName(dynamoDbProperties.getTableName());
        if (deleteTables) {
            val delete = new DeleteTableRequest(request.getTableName());
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
     * Fetch devices greater than or equal to date.
     *
     * @param expirationDate the expiration date
     * @return the collection
     */
    public Collection<? extends U2FDeviceRegistration> fetchDevicesFrom(final LocalDate expirationDate) {
        val time = expirationDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        return getRecordsByKeys(DynamoDbQueryBuilder.builder()
            .operator(ComparisonOperator.GE)
            .attributeValue(List.of(new AttributeValue().withN(String.valueOf(time))))
            .key(ColumnNames.CREATED_DATE.getColumnName())
            .build());
    }

    /**
     * Fetch devices.
     *
     * @param expirationDate the expiration date
     * @param username       the username
     * @return the collection
     */
    public Collection<? extends U2FDeviceRegistration> fetchDevicesFrom(final LocalDate expirationDate, final String username) {
        val time = expirationDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        return getRecordsByKeys(
            DynamoDbQueryBuilder.builder()
                .operator(ComparisonOperator.GE)
                .attributeValue(List.of(new AttributeValue().withN(String.valueOf(time))))
                .key(ColumnNames.CREATED_DATE.getColumnName())
                .build(),
            DynamoDbQueryBuilder.builder()
                .operator(ComparisonOperator.EQ)
                .attributeValue(List.of(new AttributeValue(username)))
                .key(ColumnNames.USERNAME.getColumnName())
                .build());
    }

    /**
     * Save u2f device registration.
     *
     * @param registration the registration
     * @return the u2f device registration
     */
    public U2FDeviceRegistration save(final U2FDeviceRegistration registration) {
        val values = buildTableAttributeValuesMap(registration);
        val putItemRequest = new PutItemRequest(dynamoDbProperties.getTableName(), values);
        LOGGER.debug("Submitting put request [{}] for record [{}]", putItemRequest, registration);
        val putItemResult = amazonDynamoDBClient.putItem(putItemRequest);
        LOGGER.debug("Record added with result [{}]", putItemResult);
        return registration;
    }

    /**
     * Remove devices before or equal to date.
     *
     * @param expirationDate the expiration date
     */
    public void removeDevicesBefore(final LocalDate expirationDate) {
        val time = expirationDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        val items = getRecordsByKeys(DynamoDbQueryBuilder.builder()
            .operator(ComparisonOperator.LE)
            .attributeValue(List.of(new AttributeValue().withN(String.valueOf(time))))
            .key(ColumnNames.CREATED_DATE.getColumnName())
            .build());
        items.forEach(item -> {
            val del = new DeleteItemRequest().withTableName(dynamoDbProperties.getTableName())
                .withKey(CollectionUtils.wrap(ColumnNames.ID.getColumnName(), new AttributeValue().withN(String.valueOf(item.getId()))));
            amazonDynamoDBClient.deleteItem(del);
        });
    }

    /**
     * Remove devices.
     */
    public void removeDevices() {
        createTable(true);
    }

    /**
     * Remove device.
     *
     * @param username the username
     * @param id       the id
     */
    public void removeDevice(final String username, final long id) {
        val items = getRecordsByKeys(
            DynamoDbQueryBuilder.builder()
                .operator(ComparisonOperator.EQ)
                .attributeValue(List.of(new AttributeValue().withN(String.valueOf(id))))
                .key(ColumnNames.ID.getColumnName())
                .build(),
            DynamoDbQueryBuilder.builder()
                .operator(ComparisonOperator.EQ)
                .attributeValue(List.of(new AttributeValue(username)))
                .key(ColumnNames.USERNAME.getColumnName())
                .build());
        items.forEach(item -> {
            val del = new DeleteItemRequest().withTableName(dynamoDbProperties.getTableName())
                .withKey(CollectionUtils.wrap(ColumnNames.ID.getColumnName(), new AttributeValue().withN(String.valueOf(item.getId()))));
            amazonDynamoDBClient.deleteItem(del);
        });
    }

    /**
     * Build table attribute values map.
     *
     * @param record the record
     * @return the map
     */
    private static Map<String, AttributeValue> buildTableAttributeValuesMap(final U2FDeviceRegistration record) {
        val values = new HashMap<String, AttributeValue>();
        values.put(ColumnNames.ID.getColumnName(), new AttributeValue().withN(String.valueOf(record.getId())));
        values.put(ColumnNames.USERNAME.getColumnName(), new AttributeValue(record.getUsername()));
        values.put(ColumnNames.RECORD.getColumnName(), new AttributeValue(record.getRecord()));
        val time = record.getCreatedDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        values.put(ColumnNames.CREATED_DATE.getColumnName(), new AttributeValue().withN(String.valueOf(time)));
        LOGGER.debug("Created attribute values [{}] based on [{}]", values, record);
        return values;
    }

    @SneakyThrows
    private Set<U2FDeviceRegistration> getRecordsByKeys(final DynamoDbQueryBuilder... queries) {
        try {
            val scanRequest = new ScanRequest(dynamoDbProperties.getTableName());
            Arrays.stream(queries).forEach(query -> {
                val cond = new Condition();
                cond.setComparisonOperator(query.getOperator());
                cond.setAttributeValueList(query.getAttributeValue());
                scanRequest.addScanFilterEntry(query.getKey(), cond);
            });
            LOGGER.debug("Submitting request [{}] to get record with keys [{}]", scanRequest, queries);
            val items = amazonDynamoDBClient.scan(scanRequest).getItems();
            return items
                .stream()
                .map(item -> {
                    val id = Long.parseLong(item.get(ColumnNames.ID.getColumnName()).getN());
                    val username = item.get(ColumnNames.USERNAME.getColumnName()).getS();
                    val record = item.get(ColumnNames.RECORD.getColumnName()).getS();
                    val time = Long.parseLong(item.get(ColumnNames.CREATED_DATE.getColumnName()).getN());
                    return U2FDeviceRegistration.builder()
                        .id(id)
                        .username(username)
                        .record(record)
                        .createdDate(DateTimeUtils.localDateTime(time))
                        .build();
                })
                .sorted(Comparator.comparing(U2FDeviceRegistration::getCreatedDate))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new HashSet<>(0);
    }

    @Getter
    private enum ColumnNames {
        ID("id"), USERNAME("username"), RECORD("record"), CREATED_DATE("createdDate");

        private final String columnName;

        ColumnNames(final String columnName) {
            this.columnName = columnName;
        }
    }

}
