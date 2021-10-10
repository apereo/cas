package org.apereo.cas.adaptors.u2f.storage;

import org.apereo.cas.configuration.model.support.mfa.u2f.U2FDynamoDbMultifactorAuthenticationProperties;
import org.apereo.cas.dynamodb.DynamoDbQueryBuilder;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
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
    private final U2FDynamoDbMultifactorAuthenticationProperties dynamoDbProperties;

    private final DynamoDbClient amazonDynamoDBClient;

    public U2FDynamoDbFacilitator(final U2FDynamoDbMultifactorAuthenticationProperties dynamoDbProperties,
                                  final DynamoDbClient amazonDynamoDBClient) {
        this.dynamoDbProperties = dynamoDbProperties;
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        if (!dynamoDbProperties.isPreventTableCreationOnStartup()) {
            createTable(dynamoDbProperties.isDropTablesOnStartup());
        }
    }

    /**
     * Build table attribute values map.
     *
     * @param record the record
     * @return the map
     */
    private static Map<String, AttributeValue> buildTableAttributeValuesMap(final U2FDeviceRegistration record) {
        val values = new HashMap<String, AttributeValue>();
        values.put(ColumnNames.ID.getColumnName(), AttributeValue.builder().n(String.valueOf(record.getId())).build());
        values.put(ColumnNames.USERNAME.getColumnName(), AttributeValue.builder().s(record.getUsername()).build());
        values.put(ColumnNames.RECORD.getColumnName(), AttributeValue.builder().s(record.getRecord()).build());
        val time = record.getCreatedDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        values.put(ColumnNames.CREATED_DATE.getColumnName(), AttributeValue.builder().n(String.valueOf(time)).build());
        LOGGER.debug("Created attribute values [{}] based on [{}]", values, record);
        return values;
    }

    /**
     * Create tables.
     *
     * @param deleteTables the delete tables
     */
    @SneakyThrows
    public void createTable(final boolean deleteTables) {
        DynamoDbTableUtils.createTable(amazonDynamoDBClient, dynamoDbProperties,
            dynamoDbProperties.getTableName(), deleteTables,
            List.of(AttributeDefinition.builder()
                .attributeName(ColumnNames.ID.getColumnName())
                .attributeType(ScalarAttributeType.N).build()),
            List.of(KeySchemaElement.builder()
                .attributeName(ColumnNames.ID.getColumnName())
                .keyType(KeyType.HASH)
                .build()));
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
            .attributeValue(List.of(AttributeValue.builder().n(String.valueOf(time)).build()))
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
                .attributeValue(List.of(AttributeValue.builder().n(String.valueOf(time)).build()))
                .key(ColumnNames.CREATED_DATE.getColumnName())
                .build(),
            DynamoDbQueryBuilder.builder()
                .operator(ComparisonOperator.EQ)
                .attributeValue(List.of(AttributeValue.builder().s(username).build()))
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
        val putItemRequest = PutItemRequest.builder().tableName(dynamoDbProperties.getTableName()).item(values).build();
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
            .attributeValue(List.of(AttributeValue.builder().n(String.valueOf(time)).build()))
            .key(ColumnNames.CREATED_DATE.getColumnName())
            .build());
        items.forEach(item -> {
            val del = DeleteItemRequest.builder()
                .tableName(dynamoDbProperties.getTableName())
                .key(CollectionUtils.wrap(ColumnNames.ID.getColumnName(),
                    AttributeValue.builder().n(String.valueOf(item.getId())).build()))
                .build();
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
                .attributeValue(List.of(AttributeValue.builder().n(String.valueOf(id)).build()))
                .key(ColumnNames.ID.getColumnName())
                .build(),
            DynamoDbQueryBuilder.builder()
                .operator(ComparisonOperator.EQ)
                .attributeValue(List.of(AttributeValue.builder().s(username).build()))
                .key(ColumnNames.USERNAME.getColumnName())
                .build());
        items.forEach(item -> {
            val del = DeleteItemRequest.builder()
                .tableName(dynamoDbProperties.getTableName())
                .key(CollectionUtils.wrap(ColumnNames.ID.getColumnName(), AttributeValue.builder().n(String.valueOf(item.getId())).build()))
                .build();
            amazonDynamoDBClient.deleteItem(del);
        });
    }

    @Getter
    @RequiredArgsConstructor
    private enum ColumnNames {
        ID("id"), USERNAME("username"), RECORD("record"), CREATED_DATE("createdDate");

        private final String columnName;
    }

    @SneakyThrows
    private Set<U2FDeviceRegistration> getRecordsByKeys(final DynamoDbQueryBuilder... queries) {
        return DynamoDbTableUtils.getRecordsByKeys(amazonDynamoDBClient, dynamoDbProperties.getTableName(),
                Arrays.stream(queries).collect(Collectors.toList()),
                item -> {
                    val id = Long.parseLong(item.get(ColumnNames.ID.getColumnName()).n());
                    val username = item.get(ColumnNames.USERNAME.getColumnName()).s();
                    val record = item.get(ColumnNames.RECORD.getColumnName()).s();
                    val time = Long.parseLong(item.get(ColumnNames.CREATED_DATE.getColumnName()).n());
                    return U2FDeviceRegistration.builder()
                        .id(id)
                        .username(username)
                        .record(record)
                        .createdDate(DateTimeUtils.localDateTime(time))
                        .build();
                })
            .sorted(Comparator.comparing(U2FDeviceRegistration::getCreatedDate))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
