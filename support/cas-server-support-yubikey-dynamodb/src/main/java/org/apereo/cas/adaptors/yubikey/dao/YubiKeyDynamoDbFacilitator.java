package org.apereo.cas.adaptors.yubikey.dao;

import org.apereo.cas.adaptors.yubikey.YubiKeyAccount;
import org.apereo.cas.configuration.model.support.mfa.yubikey.YubiKeyDynamoDbMultifactorProperties;
import org.apereo.cas.dynamodb.DynamoDbQueryBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
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
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link YubiKeyDynamoDbFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class YubiKeyDynamoDbFacilitator {
    private final YubiKeyDynamoDbMultifactorProperties dynamoDbProperties;

    private final AmazonDynamoDB amazonDynamoDBClient;

    public YubiKeyDynamoDbFacilitator(final YubiKeyDynamoDbMultifactorProperties dynamoDbProperties,
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
            new AttributeDefinition(ColumnNames.USERNAME.getColumnName(), ScalarAttributeType.S))
            .withKeySchema(new KeySchemaElement(ColumnNames.USERNAME.getColumnName(), KeyType.HASH))
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
                .attributeValue(List.of(new AttributeValue(uid)))
                .key(ColumnNames.USERNAME.getColumnName())
                .build());
    }

    /**
     * Delete.
     *
     * @param uid the uid
     */
    public void delete(final String uid) {
        val del = new DeleteItemRequest().withTableName(dynamoDbProperties.getTableName())
            .withKey(CollectionUtils.wrap(ColumnNames.USERNAME.getColumnName(),
                new AttributeValue().withS(uid)));
        amazonDynamoDBClient.deleteItem(del);
    }

    /**
     * Save.
     *
     * @param registration the registration
     * @return the boolean
     */
    public boolean save(final YubiKeyAccount registration) {
        val values = buildTableAttributeValuesMap(registration);
        val putItemRequest = new PutItemRequest(dynamoDbProperties.getTableName(), values);
        LOGGER.debug("Submitting put request [{}] for record [{}]", putItemRequest, registration);
        val putItemResult = amazonDynamoDBClient.putItem(putItemRequest);
        LOGGER.debug("Record added with result [{}]", putItemResult);
        return true;
    }

    /**
     * Save.
     *
     * @param registration the registration
     * @return the boolean
     */
    public boolean update(final YubiKeyAccount registration) {
        val attributeValue = new AttributeValue().withSS(registration.getDeviceIdentifiers());
        val updateRequest = new UpdateItemRequest(dynamoDbProperties.getTableName(),
            Map.of(
//                ColumnNames.ID.getColumnName(), new AttributeValue().withN(String.valueOf(registration.getId()))//,
                ColumnNames.USERNAME.getColumnName(), new AttributeValue().withS(String.valueOf(registration.getUsername()))
            ),
            Map.of(ColumnNames.DEVICE_IDENTIFIERS.getColumnName(), new AttributeValueUpdate(attributeValue, AttributeAction.PUT)));
        LOGGER.debug("Submitting put request [{}] for record [{}]", updateRequest, registration);
        val putItemResult = amazonDynamoDBClient.updateItem(updateRequest);
        LOGGER.debug("Record added with result [{}]", putItemResult);
        return true;
    }

    /**
     * Build table attribute values map.
     *
     * @param record the record
     * @return the map
     */
    private static Map<String, AttributeValue> buildTableAttributeValuesMap(final YubiKeyAccount record) {
        val values = new HashMap<String, AttributeValue>();
        values.put(ColumnNames.ID.getColumnName(), new AttributeValue().withN(String.valueOf(record.getId())));
        values.put(ColumnNames.USERNAME.getColumnName(), new AttributeValue().withS(String.valueOf(record.getUsername())));
        values.put(ColumnNames.DEVICE_IDENTIFIERS.getColumnName(), new AttributeValue().withSS(record.getDeviceIdentifiers()));
        LOGGER.debug("Created attribute values [{}] based on [{}]", values, record);
        return values;
    }

    @SneakyThrows
    private List<YubiKeyAccount> getRecordsByKeys(final DynamoDbQueryBuilder... queries) {
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
                    val records = new ArrayList<>(item.get(ColumnNames.DEVICE_IDENTIFIERS.getColumnName()).getSS());
                    return YubiKeyAccount.builder()
                        .id(id)
                        .username(username)
                        .deviceIdentifiers(records)
                        .build();
                })
                .collect(Collectors.toCollection(ArrayList::new));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new ArrayList<>(0);
    }

    @Getter
    private enum ColumnNames {
        ID("id"), USERNAME("username"), DEVICE_IDENTIFIERS("deviceIdentifiers");

        private final String columnName;

        ColumnNames(final String columnName) {
            this.columnName = columnName;
        }
    }

}
