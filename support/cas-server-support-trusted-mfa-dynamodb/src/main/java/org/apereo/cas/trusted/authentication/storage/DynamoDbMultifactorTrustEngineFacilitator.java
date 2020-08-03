package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbMultifactorTrustProperties;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;

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
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DynamoDbMultifactorTrustEngineFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
@SuppressWarnings("JdkObsolete")
public class DynamoDbMultifactorTrustEngineFacilitator {
    private final DynamoDbMultifactorTrustProperties dynamoDbProperties;

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
            .attributeDefinitions(AttributeDefinition.builder().attributeName(ColumnNames.ID.getColumnName()).attributeType(ScalarAttributeType.S).build())
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
     * Gets record for principal.
     *
     * @param principal the principal
     * @return the record for principal
     */
    public Set<MultifactorAuthenticationTrustRecord> getRecordForPrincipal(final String principal) {
        val keys = new HashMap<String, AttributeValue>();
        keys.put(ColumnNames.PRINCIPAL.getColumnName(), AttributeValue.builder().s(String.valueOf(principal)).build());
        return getRecordsByKeys(keys, ComparisonOperator.EQ);
    }

    /**
     * Save.
     *
     * @param record the record
     */
    public void save(final MultifactorAuthenticationTrustRecord record) {
        val values = buildTableAttributeValuesMap(record);
        val putItemRequest = PutItemRequest.builder().tableName(dynamoDbProperties.getTableName()).item(values).build();
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
        values.put(ColumnNames.ID.getColumnName(), AttributeValue.builder().s(String.valueOf(record.getId())).build());
        values.put(ColumnNames.NAME.getColumnName(), AttributeValue.builder().s(record.getName()).build());
        values.put(ColumnNames.PRINCIPAL.getColumnName(), AttributeValue.builder().s(record.getPrincipal()).build());
        values.put(ColumnNames.DEVICE_FINGERPRINT.getColumnName(), AttributeValue.builder().s(record.getDeviceFingerprint()).build());
        values.put(ColumnNames.RECORD_KEY.getColumnName(), AttributeValue.builder().s(record.getRecordKey()).build());

        val recordDate = DateTimeUtils.dateOf(record.getRecordDate()).getTime();
        values.put(ColumnNames.RECORD_DATE.getColumnName(), AttributeValue.builder().s(String.valueOf(recordDate)).build());

        val expDate = record.getExpirationDate().getTime();
        values.put(ColumnNames.EXPIRATION_DATE.getColumnName(), AttributeValue.builder().s(String.valueOf(expDate)).build());

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
        keys.put(ColumnNames.RECORD_KEY.getColumnName(), AttributeValue.builder().s(String.valueOf(key)).build());
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
        keys.put(ColumnNames.EXPIRATION_DATE.getColumnName(), AttributeValue.builder().s(String.valueOf(time)).build());
        val records = getRecordsByKeys(keys, ComparisonOperator.LE);
        deleteMultifactorTrustRecords(records);
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
        keys.put(ColumnNames.RECORD_DATE.getColumnName(), AttributeValue.builder().s(String.valueOf(time)).build());
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
        keys.put(ColumnNames.ID.getColumnName(), AttributeValue.builder().s(String.valueOf(id)).build());
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

    private Set<MultifactorAuthenticationTrustRecord> getRecordsByKeys(final Map<String, AttributeValue> keys,
                                                                       final ComparisonOperator operator) {
        val results = new HashSet<MultifactorAuthenticationTrustRecord>();
        try {
            val scanRequest = ScanRequest.builder().tableName(dynamoDbProperties.getTableName());
            if (keys.isEmpty()) {
                val cond = Condition.builder().comparisonOperator(operator).build();
                scanRequest.scanFilter(Map.of(ColumnNames.RECORD_KEY.getColumnName(), cond));
            } else {
                scanRequest.scanFilter(
                    keys.entrySet()
                        .stream()
                        .map(query -> {
                            val cond = Condition.builder().comparisonOperator(operator).attributeValueList(List.of(query.getValue())).build();
                            return Pair.of(query.getKey(), cond);
                        })
                        .collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
            }

            LOGGER.debug("Submitting request [{}] to get record with keys [{}]", scanRequest, keys);
            val items = amazonDynamoDBClient.scan(scanRequest.build()).items();
            items.forEach(item -> {
                val record = new MultifactorAuthenticationTrustRecord();
                record.setId(Long.parseLong(item.get(ColumnNames.ID.getColumnName()).s()));
                record.setDeviceFingerprint(item.get(ColumnNames.DEVICE_FINGERPRINT.getColumnName()).s());
                record.setName(item.get(ColumnNames.NAME.getColumnName()).s());
                record.setPrincipal(item.get(ColumnNames.PRINCIPAL.getColumnName()).s());
                record.setRecordKey(item.get(ColumnNames.RECORD_KEY.getColumnName()).s());
                val time = Long.parseLong(item.get(ColumnNames.RECORD_DATE.getColumnName()).s());
                record.setRecordDate(DateTimeUtils.zonedDateTimeOf(Instant.ofEpochMilli(time)));
                val expTime = Long.parseLong(item.get(ColumnNames.EXPIRATION_DATE.getColumnName()).s());
                record.setExpirationDate(new Date(expTime));

                results.add(record);
            });
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return results;
    }

    private void deleteMultifactorTrustRecords(final Set<MultifactorAuthenticationTrustRecord> records) {
        records.forEach(record -> {
            val del = DeleteItemRequest.builder()
                .tableName(dynamoDbProperties.getTableName())
                .key(CollectionUtils.wrap(ColumnNames.ID.getColumnName(), AttributeValue.builder().s(String.valueOf(record.getId())).build()))
                .build();

            LOGGER.debug("Submitting delete request [{}] for record [{}]", del, record);
            val res = amazonDynamoDBClient.deleteItem(del);
            LOGGER.debug("Delete request came back with result [{}]", res);
        });
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
