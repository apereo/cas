package org.apereo.cas.audit;

import org.apereo.cas.configuration.model.support.dynamodb.AuditDynamoDbProperties;
import org.apereo.cas.dynamodb.DynamoDbQueryBuilder;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.util.DateTimeUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
@SuppressWarnings("JavaUtilDate")
public class DynamoDbAuditTrailManagerFacilitator {
    private final AuditDynamoDbProperties dynamoDbProperties;

    private final DynamoDbClient amazonDynamoDBClient;

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

    /**
     * Create tables.
     *
     * @param deleteTables the delete tables
     */
    @SneakyThrows
    public void createTable(final boolean deleteTables) {
        val attributes = List.of(AttributeDefinition.builder()
            .attributeName(ColumnNames.PRINCIPAL.getColumnName())
            .attributeType(ScalarAttributeType.S)
            .build());
        val schema = List.of(KeySchemaElement.builder()
            .attributeName(ColumnNames.PRINCIPAL.getColumnName())
            .keyType(KeyType.HASH)
            .build());
        DynamoDbTableUtils.createTable(amazonDynamoDBClient, dynamoDbProperties,
            dynamoDbProperties.getTableName(), deleteTables, attributes, schema);
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
    @SuppressWarnings("JavaUtilDate")
    public Set<? extends AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        val time = DateTimeUtils.dateOf(localDate).getTime();
        val keys = DynamoDbQueryBuilder.builder()
            .key(ColumnNames.WHEN_ACTION_PERFORMED.getColumnName())
            .attributeValue(List.of(AttributeValue.builder().s(String.valueOf(time)).build()))
            .operator(ComparisonOperator.GE)
            .build();
        return DynamoDbTableUtils.getRecordsByKeys(amazonDynamoDBClient, dynamoDbProperties.getTableName(),
                List.of(keys), item -> {
                    val principal = item.get(ColumnNames.PRINCIPAL.getColumnName()).s();
                    val actionPerformed = item.get(ColumnNames.ACTION_PERFORMED.getColumnName()).s();
                    val appCode = item.get(ColumnNames.APPLICATION_CODE.getColumnName()).s();
                    val clientIp = item.get(ColumnNames.CLIENT_IP_ADDRESS.getColumnName()).s();
                    val serverIp = item.get(ColumnNames.SERVER_IP_ADDRESS.getColumnName()).s();
                    val resource = item.get(ColumnNames.RESOURCE_OPERATED_UPON.getColumnName()).s();
                    val time1 = Long.parseLong(item.get(ColumnNames.WHEN_ACTION_PERFORMED.getColumnName()).s());
                    return new AuditActionContext(principal, resource, actionPerformed,
                        appCode, new Date(time1), clientIp, serverIp);
                })
            .collect(Collectors.toSet());
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
