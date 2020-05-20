package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbMultifactorTrustProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;

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
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link DynamoDbMultifactorTrustEngineFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class DynamoDbMultifactorTrustEngineFacilitator {
    private final DynamoDbMultifactorTrustProperties dynamoDbProperties;

    private final AmazonDynamoDB amazonDynamoDBClient;

    /**
     * Create tables.
     *
     * @param deleteTables the delete tables
     */
    @SneakyThrows
    public void createTable(final boolean deleteTables) {
        val request = new CreateTableRequest()
            .withAttributeDefinitions(
                new AttributeDefinition(ColumnNames.ID.getColumnName(), ScalarAttributeType.S))
            .withKeySchema(
                new KeySchemaElement(ColumnNames.ID.getColumnName(), KeyType.HASH))
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
     * Gets record for principal.
     *
     * @param principal the principal
     * @return the record for principal
     */
    public Set<MultifactorAuthenticationTrustRecord> getRecordForPrincipal(final String principal) {
        val keys = new HashMap<String, AttributeValue>();
        keys.put(ColumnNames.PRINCIPAL.getColumnName(), new AttributeValue(String.valueOf(principal)));
        return getRecordsByKeys(keys, ComparisonOperator.EQ);
    }

    private Set<MultifactorAuthenticationTrustRecord> getRecordsByKeys(final Map<String, AttributeValue> keys,
                                                                       final ComparisonOperator operator) {
        val results = new HashSet<MultifactorAuthenticationTrustRecord>();
        try {
            val scanRequest = new ScanRequest(dynamoDbProperties.getTableName());
            if (keys.isEmpty()) {
                val cond = new Condition();
                cond.setComparisonOperator(operator);
                scanRequest.addScanFilterEntry(ColumnNames.RECORD_KEY.getColumnName(), cond);
            } else {
                keys.forEach((k, v) -> {
                    val cond = new Condition();
                    cond.setComparisonOperator(operator);
                    cond.setAttributeValueList(List.of(v));
                    scanRequest.addScanFilterEntry(k, cond);
                });
            }

            LOGGER.debug("Submitting request [{}] to get record with keys [{}]", scanRequest, keys);
            val items = amazonDynamoDBClient.scan(scanRequest).getItems();
            items.forEach(item -> {
                val record = new MultifactorAuthenticationTrustRecord();
                record.setId(Long.parseLong(item.get(ColumnNames.ID.getColumnName()).getS()));
                record.setDeviceFingerprint(item.get(ColumnNames.DEVICE_FINGERPRINT.getColumnName()).getS());
                record.setName(item.get(ColumnNames.NAME.getColumnName()).getS());
                record.setPrincipal(item.get(ColumnNames.PRINCIPAL.getColumnName()).getS());
                record.setRecordKey(item.get(ColumnNames.RECORD_KEY.getColumnName()).getS());
                val time = Long.parseLong(item.get(ColumnNames.RECORD_DATE.getColumnName()).getS());
                record.setRecordDate(DateTimeUtils.zonedDateTimeOf(new Date(time)));
                val expTime = Long.parseLong(item.get(ColumnNames.EXPIRATION_DATE.getColumnName()).getS());
                record.setExpirationDate(new Date(expTime));

                results.add(record);
            });
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return results;
    }

    /**
     * Save.
     *
     * @param record the record
     */
    public void save(final MultifactorAuthenticationTrustRecord record) {
        val values = buildTableAttributeValuesMap(record);
        val putItemRequest = new PutItemRequest(dynamoDbProperties.getTableName(), values);
        LOGGER.trace("Submitting put request [{}] for record id [{}]", putItemRequest, record.getId());
        val putItemResult = amazonDynamoDBClient.putItem(putItemRequest);
        LOGGER.debug("Record added with result [{}]", putItemResult);
    }

    /**
     * Build table attribute values map map.
     *
     * @param record the record
     * @return the map
     */
    public Map<String, AttributeValue> buildTableAttributeValuesMap(final MultifactorAuthenticationTrustRecord record) {
        val values = new HashMap<String, AttributeValue>();
        values.put(ColumnNames.ID.getColumnName(), new AttributeValue(String.valueOf(record.getId())));
        values.put(ColumnNames.NAME.getColumnName(), new AttributeValue(record.getName()));
        values.put(ColumnNames.PRINCIPAL.getColumnName(), new AttributeValue(record.getPrincipal()));
        values.put(ColumnNames.DEVICE_FINGERPRINT.getColumnName(), new AttributeValue(record.getDeviceFingerprint()));
        values.put(ColumnNames.RECORD_KEY.getColumnName(), new AttributeValue(record.getRecordKey()));

        val recordDate = DateTimeUtils.dateOf(record.getRecordDate()).getTime();
        values.put(ColumnNames.RECORD_DATE.getColumnName(), new AttributeValue(String.valueOf(recordDate)));

        val expDate = record.getExpirationDate().getTime();
        values.put(ColumnNames.EXPIRATION_DATE.getColumnName(), new AttributeValue(String.valueOf(expDate)));

        LOGGER.debug("Created attribute values [{}] based on [{}]", values, record);
        return values;
    }

    /**
     * Remove.
     *
     * @param key the key
     */
    public void remove(final String key) {
        val keys = new HashMap<String, AttributeValue>();
        keys.put(ColumnNames.RECORD_KEY.getColumnName(), new AttributeValue(String.valueOf(key)));
        val records = getRecordsByKeys(keys, ComparisonOperator.EQ);
        deleteMultifactorTrustRecords(records);
    }

    /**
     * Remove expired records.
     *
     * @param expirationDate the exp date
     */
    public void remove(final ZonedDateTime expirationDate) {
        val keys = new HashMap<String, AttributeValue>();
        val time = DateTimeUtils.dateOf(expirationDate).getTime();
        keys.put(ColumnNames.EXPIRATION_DATE.getColumnName(), new AttributeValue(String.valueOf(time)));
        val records = getRecordsByKeys(keys, ComparisonOperator.LE);
        deleteMultifactorTrustRecords(records);
    }

    private void deleteMultifactorTrustRecords(final Set<MultifactorAuthenticationTrustRecord> records) {
        records.forEach(record -> {
            val del = new DeleteItemRequest()
                .withTableName(dynamoDbProperties.getTableName())
                .withKey(CollectionUtils.wrap(ColumnNames.ID.getColumnName(),
                    new AttributeValue(String.valueOf(record.getId()))));
            LOGGER.debug("Submitting delete request [{}] for record [{}]", del, record);
            val res = amazonDynamoDBClient.deleteItem(del);
            LOGGER.debug("Delete request came back with result [{}]", res);
        });
    }

    /**
     * Gets record for date.
     *
     * @param onOrAfterDate the on or after date
     * @return the record for date
     */
    public Set<? extends MultifactorAuthenticationTrustRecord> getRecordForDate(final ZonedDateTime onOrAfterDate) {
        val keys = new HashMap<String, AttributeValue>();
        val time = DateTimeUtils.dateOf(onOrAfterDate).getTime();
        keys.put(ColumnNames.RECORD_DATE.getColumnName(), new AttributeValue(String.valueOf(time)));
        return getRecordsByKeys(keys, ComparisonOperator.GE);
    }

    /**
     * Gets record for id.
     *
     * @param id the id
     * @return the record for id
     */
    public MultifactorAuthenticationTrustRecord getRecordForId(final long id) {
        val keys = new HashMap<String, AttributeValue>();
        keys.put(ColumnNames.ID.getColumnName(), new AttributeValue(String.valueOf(id)));
        val records = getRecordsByKeys(keys, ComparisonOperator.EQ);
        return records.stream()
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets record for id.
     *
     * @return the record for id
     */
    public Set<MultifactorAuthenticationTrustRecord> getAll() {
        return getRecordsByKeys(Map.of(), ComparisonOperator.NOT_NULL);
    }

    /**
     * Column names for tables holding records.
     */
    @Getter
    public enum ColumnNames {

        /**
         * id column.
         */
        ID("id"),
        /**
         * principal column.
         */
        PRINCIPAL("principal"),
        /**
         * deviceFingerprint column.
         */
        DEVICE_FINGERPRINT("deviceFingerprint"),
        /**
         * recordDate column.
         */
        RECORD_DATE("recordDate"),
        /**
         * expirationDate column.
         */
        EXPIRATION_DATE("expirationDate"),
        /**
         * recordKey column.
         */
        RECORD_KEY("recordKey"),
        /**
         * name column.
         */
        NAME("name");

        private final String columnName;

        ColumnNames(final String columnName) {
            this.columnName = columnName;
        }
    }
}
