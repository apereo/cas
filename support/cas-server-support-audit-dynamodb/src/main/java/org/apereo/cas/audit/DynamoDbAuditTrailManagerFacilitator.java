package org.apereo.cas.audit;

import org.apereo.cas.configuration.model.support.dynamodb.AuditDynamoDbProperties;
import org.apereo.cas.util.DateTimeUtils;

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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
public class DynamoDbAuditTrailManagerFacilitator {
    private final AuditDynamoDbProperties dynamoDbProperties;

    private final AmazonDynamoDB amazonDynamoDBClient;

    /**
     * Build table attribute values map map.
     *
     * @param record the record
     * @return the map
     */
    private static Map<String, AttributeValue> buildTableAttributeValuesMap(final AuditActionContext record) {
        val values = new HashMap<String, AttributeValue>();
        values.put(ColumnNames.PRINCIPAL.getColumnName(), new AttributeValue(record.getPrincipal()));
        values.put(ColumnNames.CLIENT_IP_ADDRESS.getColumnName(), new AttributeValue(record.getClientIpAddress()));
        values.put(ColumnNames.SERVER_IP_ADDRESS.getColumnName(), new AttributeValue(record.getServerIpAddress()));
        values.put(ColumnNames.RESOURCE_OPERATED_UPON.getColumnName(), new AttributeValue(record.getResourceOperatedUpon()));
        values.put(ColumnNames.APPLICATION_CODE.getColumnName(), new AttributeValue(record.getApplicationCode()));
        values.put(ColumnNames.ACTION_PERFORMED.getColumnName(), new AttributeValue(record.getActionPerformed()));
        val time = record.getWhenActionWasPerformed().getTime();
        values.put(ColumnNames.WHEN_ACTION_PERFORMED.getColumnName(), new AttributeValue(String.valueOf(time)));
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
        val request = new CreateTableRequest()
            .withAttributeDefinitions(new AttributeDefinition(ColumnNames.PRINCIPAL.getColumnName(), ScalarAttributeType.S))
            .withKeySchema(new KeySchemaElement(ColumnNames.PRINCIPAL.getColumnName(), KeyType.HASH))
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
    public void save(final AuditActionContext record) {
        val values = buildTableAttributeValuesMap(record);
        val putItemRequest = new PutItemRequest(dynamoDbProperties.getTableName(), values);
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
    public Set<? extends AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        val keys = new HashMap<String, AttributeValue>();
        val time = DateTimeUtils.dateOf(localDate).getTime();
        keys.put(ColumnNames.WHEN_ACTION_PERFORMED.getColumnName(), new AttributeValue(String.valueOf(time)));
        return getRecordsByKeys(keys, ComparisonOperator.GE);
    }

    private Set<AuditActionContext> getRecordsByKeys(final Map<String, AttributeValue> keys,
                                                     final ComparisonOperator operator) {

        try {
            val scanRequest = new ScanRequest(dynamoDbProperties.getTableName());
            keys.forEach((k, v) -> {
                val cond = new Condition();
                cond.setComparisonOperator(operator);
                cond.setAttributeValueList(List.of(v));
                scanRequest.addScanFilterEntry(k, cond);
            });

            LOGGER.debug("Submitting request [{}] to get record with keys [{}]", scanRequest, keys);
            val items = amazonDynamoDBClient.scan(scanRequest).getItems();
            return items
                .stream()
                .map(item -> {
                    val principal = item.get(ColumnNames.PRINCIPAL.getColumnName()).getS();
                    val actionPerformed = item.get(ColumnNames.ACTION_PERFORMED.getColumnName()).getS();
                    val appCode = item.get(ColumnNames.APPLICATION_CODE.getColumnName()).getS();
                    val clientIp = item.get(ColumnNames.CLIENT_IP_ADDRESS.getColumnName()).getS();
                    val serverIp = item.get(ColumnNames.SERVER_IP_ADDRESS.getColumnName()).getS();
                    val resource = item.get(ColumnNames.RESOURCE_OPERATED_UPON.getColumnName()).getS();
                    val time = Long.parseLong(item.get(ColumnNames.WHEN_ACTION_PERFORMED.getColumnName()).getS());
                    return new AuditActionContext(principal, resource, actionPerformed,
                        appCode, new Date(time), clientIp, serverIp);
                })
                .collect(Collectors.toCollection(LinkedHashSet::new));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
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
