package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.adaptors.yubikey.YubiKeyRegisteredDevice;
import org.apereo.cas.configuration.model.support.mfa.yubikey.YubiKeyDynamoDbMultifactorProperties;
import org.apereo.cas.dynamodb.DynamoDbQueryBuilder;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link DynamoDbYubiKeyFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class DynamoDbYubiKeyFacilitator {
    private final YubiKeyDynamoDbMultifactorProperties dynamoDbProperties;

    private final DynamoDbClient amazonDynamoDBClient;

    public DynamoDbYubiKeyFacilitator(final YubiKeyDynamoDbMultifactorProperties dynamoDbProperties,
                                      final DynamoDbClient amazonDynamoDBClient) {
        this.dynamoDbProperties = dynamoDbProperties;
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        if (!dynamoDbProperties.isPreventTableCreationOnStartup()) {
            createTable(dynamoDbProperties.isDropTablesOnStartup());
        }
    }

    private static AttributeValue toAttributeValue(final YubiKeyAccount account) {
        val devices = account.getDevices()
            .stream()
            .map(device -> {
                val attributes = CollectionUtils.<String, AttributeValue>wrap(
                    "id", AttributeValue.builder().n(String.valueOf(device.getId())).build(),
                    "name", AttributeValue.builder().s(device.getName()).build(),
                    "publicId", AttributeValue.builder().s(device.getPublicId()).build(),
                    "registrationDate", AttributeValue.builder().s(device.getRegistrationDate().toString()).build()
                );
                if (StringUtils.isNotBlank(device.getTenant())) {
                    attributes.put("tenant", AttributeValue.builder().s(device.getTenant()).build());
                }
                return AttributeValue.builder().m(attributes).build();
            })
            .collect(Collectors.toList());
        return AttributeValue.builder().l(devices).build();
    }

    private static YubiKeyRegisteredDevice toYubiKeyRegisteredDevice(final Map<String, AttributeValue> map) {
        return YubiKeyRegisteredDevice.builder()
            .id(Long.parseLong(map.get("id").n()))
            .name(map.get("name").s())
            .publicId(map.get("publicId").s())
            .registrationDate(DateTimeUtils.zonedDateTimeOf(map.get("registrationDate").s()))
            .tenant(map.containsKey("tenant") ? map.get("tenant").s() : null)
            .build();
    }

    /**
     * Build table attribute values map.
     *
     * @param record the record
     * @return the map
     */
    private static Map<String, AttributeValue> buildTableAttributeValuesMap(final YubiKeyAccount record) {
        val values = new HashMap<String, AttributeValue>();
        values.put(ColumnNames.ID.getColumnName(), AttributeValue.builder().n(String.valueOf(record.getId())).build());
        values.put(ColumnNames.USERNAME.getColumnName(), AttributeValue.builder().s(String.valueOf(record.getUsername())).build());
        values.put(ColumnNames.DEVICE_IDENTIFIERS.getColumnName(), toAttributeValue(record));
        if (StringUtils.isNotBlank(record.getTenant())) {
            values.put(ColumnNames.TENANT.getColumnName(), AttributeValue.builder().s(record.getTenant()).build());
        }
        LOGGER.debug("Created attribute values [{}] based on [{}]", values, record);
        return values;
    }

    /**
     * Create tables.
     *
     * @param deleteTables the delete tables
     */
    public void createTable(final boolean deleteTables) {
        FunctionUtils.doUnchecked(__ -> DynamoDbTableUtils.createTable(amazonDynamoDBClient, dynamoDbProperties,
            dynamoDbProperties.getTableName(), deleteTables,
            List.of(AttributeDefinition.builder()
                .attributeName(ColumnNames.USERNAME.getColumnName())
                .attributeType(ScalarAttributeType.S)
                .build()),
            List.of(KeySchemaElement.builder()
                .attributeName(ColumnNames.USERNAME.getColumnName())
                .keyType(KeyType.HASH)
                .build())));
    }

    /**
     * Remove devices.
     */
    public void removeDevices() {
        createTable(true);
    }

    /**
     * Gets accounts.
     *
     * @return the accounts
     */
    public List<? extends YubiKeyAccount> getAccounts() {
        return getRecordsByKeys();
    }

    /**
     * Gets accounts.
     *
     * @param uid the uid
     * @return the accounts
     */
    public List<YubiKeyAccount> getAccounts(final String uid) {
        return getRecordsByKeys(
            DynamoDbQueryBuilder.builder()
                .operator(ComparisonOperator.EQ)
                .attributeValue(List.of(AttributeValue.builder().s(uid).build()))
                .key(ColumnNames.USERNAME.getColumnName())
                .build());
    }

    /**
     * Delete.
     *
     * @param username the username
     * @param deviceId the device id
     */
    public void delete(final String username, final long deviceId) {
        val accounts = getAccounts(username);
        if (!accounts.isEmpty()) {
            val account = accounts.getFirst();
            if (account != null && account.getDevices().removeIf(device -> device.getId() == deviceId)) {
                update(account);
            }
        }
    }

    /**
     * Delete.
     *
     * @param uid the uid
     */
    public void delete(final String uid) {
        val del = DeleteItemRequest.builder().tableName(dynamoDbProperties.getTableName())
            .key(CollectionUtils.wrap(ColumnNames.USERNAME.getColumnName(), AttributeValue.builder().s(uid).build()))
            .build();
        amazonDynamoDBClient.deleteItem(del);
    }

    /**
     * Save.
     *
     * @param registration the registration
     * @return true/false
     */
    public boolean save(final YubiKeyAccount registration) {
        val values = buildTableAttributeValuesMap(registration);
        val putItemRequest = PutItemRequest.builder().tableName(dynamoDbProperties.getTableName()).item(values).build();
        LOGGER.debug("Submitting put request [{}] for record [{}]", putItemRequest, registration);
        val putItemResult = amazonDynamoDBClient.putItem(putItemRequest);
        LOGGER.debug("Record added with result [{}]", putItemResult);
        return true;
    }

    /**
     * Save.
     *
     * @param registration the registration
     * @return true/false
     */
    public boolean update(final YubiKeyAccount registration) {
        val updateRequest = UpdateItemRequest.builder()
            .tableName(dynamoDbProperties.getTableName())
            .key(Map.of(ColumnNames.USERNAME.getColumnName(),
                AttributeValue.builder().s(String.valueOf(registration.getUsername())).build()))
            .attributeUpdates(Map.of(ColumnNames.DEVICE_IDENTIFIERS.getColumnName(),
                AttributeValueUpdate.builder().value(toAttributeValue(registration)).action(AttributeAction.PUT).build()))
            .build();
        LOGGER.debug("Submitting put request [{}] for record [{}]", updateRequest, registration);
        val putItemResult = amazonDynamoDBClient.updateItem(updateRequest);
        LOGGER.debug("Record added with result [{}]", putItemResult);
        return true;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ColumnNames {
        /**
         * Id column name.
         */
        ID("id"),
        /**
         * Username column name.
         */
        USERNAME("username"),
        /**
         * Tenant column name.
         */
        TENANT("tenant"),
        /**
         * Device identifiers column name.
         */
        DEVICE_IDENTIFIERS("deviceIdentifiers");

        private final String columnName;
    }

    private List<YubiKeyAccount> getRecordsByKeys(final DynamoDbQueryBuilder... queries) {
        return DynamoDbTableUtils.getRecordsByKeys(amazonDynamoDBClient, dynamoDbProperties.getTableName(),
                Arrays.stream(queries).collect(Collectors.toList()),
                item -> {
                    val id = Long.parseLong(item.get(ColumnNames.ID.getColumnName()).n());
                    val username = item.get(ColumnNames.USERNAME.getColumnName()).s();
                    val details = item.get(ColumnNames.DEVICE_IDENTIFIERS.getColumnName()).l();
                    val tenant = item.containsKey(ColumnNames.TENANT.getColumnName())
                        ? item.get(ColumnNames.TENANT.getColumnName()).s()
                        : null;
                    val records = details.stream().map(value -> toYubiKeyRegisteredDevice(value.m())).collect(Collectors.toList());
                    return YubiKeyAccount.builder()
                        .id(id)
                        .username(username)
                        .devices(records)
                        .tenant(tenant)
                        .build();
                })
            .collect(Collectors.toList());
    }

}
