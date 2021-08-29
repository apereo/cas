package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbTrustedDevicesMultifactorProperties;
import org.apereo.cas.dynamodb.DynamoDbQueryBuilder;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
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

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link DynamoDbMultifactorTrustEngineFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
@SuppressWarnings("JavaUtilDate")
public class DynamoDbMultifactorTrustEngineFacilitator {
    private final DynamoDbTrustedDevicesMultifactorProperties dynamoDbProperties;

    private final DynamoDbClient amazonDynamoDBClient;

    private static MultifactorAuthenticationTrustRecord extractAttributeValuesFrom(final Map<String, AttributeValue> item) {
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
        return record;
    }

    /**
     * Build table attribute values map map.
     *
     * @param record the record
     * @return the map
     */
    private static Map<String, AttributeValue> buildTableAttributeValuesMap(final MultifactorAuthenticationTrustRecord record) {
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
     * Create tables.
     *
     * @param deleteTables the delete tables
     */
    @SneakyThrows
    public void createTable(final boolean deleteTables) {
        DynamoDbTableUtils.createTable(amazonDynamoDBClient, dynamoDbProperties,
            dynamoDbProperties.getTableName(), deleteTables,
            List.of(AttributeDefinition.builder().attributeName(ColumnNames.ID.getColumnName()).attributeType(ScalarAttributeType.S).build()),
            List.of(KeySchemaElement.builder().attributeName(ColumnNames.ID.getColumnName()).keyType(KeyType.HASH).build()));
    }

    /**
     * Gets record for principal.
     *
     * @param principal the principal
     * @return the record for principal
     */
    public Set<MultifactorAuthenticationTrustRecord> getRecordForPrincipal(final String principal) {
        val queries = List.of(DynamoDbQueryBuilder.builder()
            .key(ColumnNames.PRINCIPAL.getColumnName())
            .operator(ComparisonOperator.EQ)
            .attributeValue(List.of(AttributeValue.builder().s(String.valueOf(principal)).build()))
            .build());
        return DynamoDbTableUtils.getRecordsByKeys(amazonDynamoDBClient, dynamoDbProperties.getTableName(),
                queries, DynamoDbMultifactorTrustEngineFacilitator::extractAttributeValuesFrom)
            .collect(Collectors.toSet());
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
     * Remove.
     *
     * @param key the key
     */
    public void remove(final String key) {
        val queries = List.of(DynamoDbQueryBuilder.builder()
            .key(ColumnNames.RECORD_KEY.getColumnName())
            .operator(ComparisonOperator.EQ)
            .attributeValue(List.of(AttributeValue.builder().s(String.valueOf(key)).build()))
            .build());
        val records = DynamoDbTableUtils.getRecordsByKeys(amazonDynamoDBClient, dynamoDbProperties.getTableName(),
            queries, DynamoDbMultifactorTrustEngineFacilitator::extractAttributeValuesFrom);

        deleteMultifactorTrustRecords(records);
    }

    /**
     * Remove expired records.
     *
     * @param expirationDate the exp date
     */
    public void remove(final ZonedDateTime expirationDate) {
        val time = DateTimeUtils.dateOf(expirationDate).getTime();
        val queries = List.of(DynamoDbQueryBuilder.builder()
            .key(ColumnNames.EXPIRATION_DATE.getColumnName())
            .operator(ComparisonOperator.LE)
            .attributeValue(List.of(AttributeValue.builder().s(String.valueOf(time)).build()))
            .build());
        val records = DynamoDbTableUtils.getRecordsByKeys(amazonDynamoDBClient, dynamoDbProperties.getTableName(),
            queries, DynamoDbMultifactorTrustEngineFacilitator::extractAttributeValuesFrom);
        deleteMultifactorTrustRecords(records);
    }

    /**
     * Gets record for date.
     *
     * @param onOrAfterDate the on or after date
     * @return the record for date
     */
    public Set<? extends MultifactorAuthenticationTrustRecord> getRecordForDate(final ZonedDateTime onOrAfterDate) {
        val time = DateTimeUtils.dateOf(onOrAfterDate).getTime();
        val queries = List.of(DynamoDbQueryBuilder.builder()
            .key(ColumnNames.RECORD_DATE.getColumnName())
            .operator(ComparisonOperator.GE)
            .attributeValue(List.of(AttributeValue.builder().s(String.valueOf(time)).build()))
            .build());
        return DynamoDbTableUtils.getRecordsByKeys(amazonDynamoDBClient, dynamoDbProperties.getTableName(),
                queries, DynamoDbMultifactorTrustEngineFacilitator::extractAttributeValuesFrom)
            .collect(Collectors.toSet());
    }

    /**
     * Gets record for id.
     *
     * @param id the id
     * @return the record for id
     */
    public MultifactorAuthenticationTrustRecord getRecordForId(final long id) {
        val queries = List.of(DynamoDbQueryBuilder.builder()
            .key(ColumnNames.ID.getColumnName())
            .operator(ComparisonOperator.EQ)
            .attributeValue(List.of(AttributeValue.builder().s(String.valueOf(id)).build()))
            .build());
        return DynamoDbTableUtils.getRecordsByKeys(amazonDynamoDBClient, dynamoDbProperties.getTableName(),
                queries, DynamoDbMultifactorTrustEngineFacilitator::extractAttributeValuesFrom)
            .findFirst()
            .orElse(null);
    }

    /**
     * Gets record for id.
     *
     * @return the record for id
     */
    public Set<MultifactorAuthenticationTrustRecord> getAll() {
        val queries = List.of(DynamoDbQueryBuilder.builder()
            .key(ColumnNames.RECORD_KEY.getColumnName())
            .operator(ComparisonOperator.NOT_NULL)
            .build());
        return DynamoDbTableUtils.getRecordsByKeys(amazonDynamoDBClient, dynamoDbProperties.getTableName(),
                queries, DynamoDbMultifactorTrustEngineFacilitator::extractAttributeValuesFrom)
            .collect(Collectors.toSet());
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


    private void deleteMultifactorTrustRecords(final Stream<MultifactorAuthenticationTrustRecord> records) {
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
}
