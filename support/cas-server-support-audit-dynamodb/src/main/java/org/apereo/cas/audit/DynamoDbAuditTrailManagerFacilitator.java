package org.apereo.cas.audit;

import org.apereo.cas.configuration.model.support.dynamodb.AuditDynamoDbProperties;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.inspektr.audit.AuditActionContext;
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

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DynamoDbAuditTrailManagerFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
@SuppressWarnings("JdkObsolete")
public class DynamoDbAuditTrailManagerFacilitator {
    private final AuditDynamoDbProperties dynamoDbProperties;

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
            .attributeDefinitions(AttributeDefinition.builder().attributeName(ColumnNames.PRINCIPAL.getColumnName()).attributeType(ScalarAttributeType.S).build())
            .keySchema(KeySchemaElement.builder().attributeName(ColumnNames.PRINCIPAL.getColumnName()).keyType(KeyType.HASH).build())
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
    public void save(final AuditActionContext record) {
        val values = buildTableAttributeValuesMap(record);
        val putItemRequest = PutItemRequest.builder().tableName(dynamoDbProperties.getTableName()).item(values).build();
        LOGGER.debug("Submitting put request [{}] for record [{}]", putItemRequest, record);
        val putItemResult = amazonDynamoDBClient.putItem(putItemRequest);
        LOGGER.debug("Record added with result [{}]", putItemResult);
    }

    /**
     * Remove all.
     */
    public void removeAll() {
        createTable(true);
    }

    /**
     * Gets audit records.
     *
     * @param localDate the local date
     * @return the audit records since
     */
    @SuppressWarnings("JdkObsolete")
    public Set<? extends AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        val keys = new HashMap<String, AttributeValue>();
        val time = DateTimeUtils.dateOf(localDate).getTime();
        keys.put(ColumnNames.WHEN_ACTION_PERFORMED.getColumnName(), AttributeValue.builder().s(String.valueOf(time)).build());
        return getRecordsByKeys(keys, ComparisonOperator.GE);
    }

    /**
     * Build table attribute values map.
     *
     * @param record the record
     * @return the map
     */
    private static Map<String, AttributeValue> buildTableAttributeValuesMap(final AuditActionContext record) {
        val values = new HashMap<String, AttributeValue>();
        values.put(ColumnNames.PRINCIPAL.getColumnName(), AttributeValue.builder().s(record.getPrincipal()).build());
        values.put(ColumnNames.CLIENT_IP_ADDRESS.getColumnName(), AttributeValue.builder().s(record.getClientIpAddress()).build());
        values.put(ColumnNames.SERVER_IP_ADDRESS.getColumnName(), AttributeValue.builder().s(record.getServerIpAddress()).build());
        values.put(ColumnNames.RESOURCE_OPERATED_UPON.getColumnName(), AttributeValue.builder().s(record.getResourceOperatedUpon()).build());
        values.put(ColumnNames.APPLICATION_CODE.getColumnName(), AttributeValue.builder().s(record.getApplicationCode()).build());
        values.put(ColumnNames.ACTION_PERFORMED.getColumnName(), AttributeValue.builder().s(record.getActionPerformed()).build());
        val time = record.getWhenActionWasPerformed().getTime();
        values.put(ColumnNames.WHEN_ACTION_PERFORMED.getColumnName(), AttributeValue.builder().s(String.valueOf(time)).build());
        LOGGER.debug("Created attribute values [{}] based on [{}]", values, record);
        return values;
    }

    private Set<AuditActionContext> getRecordsByKeys(final Map<String, AttributeValue> keys,
                                                     final ComparisonOperator operator) {
        try {

            var scanRequest = ScanRequest.builder()
                .tableName(dynamoDbProperties.getTableName())
                .scanFilter(keys.entrySet().stream()
                    .map(query -> {
                        val cond = Condition.builder().comparisonOperator(operator).attributeValueList(query.getValue()).build();
                        return Pair.of(query.getKey(), cond);
                    })
                    .collect(Collectors.toMap(Pair::getKey, Pair::getValue)))
                .build();

            LOGGER.debug("Submitting request [{}] to get record with keys [{}]", scanRequest, keys);
            val items = amazonDynamoDBClient.scan(scanRequest).items();
            return items
                .stream()
                .map(item -> {
                    val principal = item.get(ColumnNames.PRINCIPAL.getColumnName()).s();
                    val actionPerformed = item.get(ColumnNames.ACTION_PERFORMED.getColumnName()).s();
                    val appCode = item.get(ColumnNames.APPLICATION_CODE.getColumnName()).s();
                    val clientIp = item.get(ColumnNames.CLIENT_IP_ADDRESS.getColumnName()).s();
                    val serverIp = item.get(ColumnNames.SERVER_IP_ADDRESS.getColumnName()).s();
                    val resource = item.get(ColumnNames.RESOURCE_OPERATED_UPON.getColumnName()).s();
                    val time = Long.parseLong(item.get(ColumnNames.WHEN_ACTION_PERFORMED.getColumnName()).s());
                    return new AuditActionContext(principal, resource, actionPerformed,
                        appCode, new Date(time), clientIp, serverIp);
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new HashSet<>(0);
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
         * whenActionWasPerformed column.
         */
        WHEN_ACTION_PERFORMED("whenActionWasPerformed"),
        /**
         * clientIpAddress column.
         */
        CLIENT_IP_ADDRESS("clientIpAddress"),
        /**
         * serverIpAddress column.
         */
        SERVER_IP_ADDRESS("serverIpAddress"),
        /**
         * resourceOperatedUpon column.
         */
        RESOURCE_OPERATED_UPON("resourceOperatedUpon"),
        /**
         * actionPerformed column.
         */
        ACTION_PERFORMED("actionPerformed"),
        /**
         * applicationCode column.
         */
        APPLICATION_CODE("applicationCode");

        private final String columnName;

        ColumnNames(final String columnName) {
            this.columnName = columnName;
        }
    }
}
