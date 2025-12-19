package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import org.apereo.cas.configuration.model.support.dynamodb.AuditDynamoDbProperties;
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
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

/**
 * This is {@link DynamoDbAuditTrailManagerFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
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
        values.put(ColumnNames.CLIENT_IP_ADDRESS.getColumnName(), AttributeValue.builder().s(record.getClientInfo().getClientIpAddress()).build());
        values.put(ColumnNames.SERVER_IP_ADDRESS.getColumnName(), AttributeValue.builder().s(record.getClientInfo().getServerIpAddress()).build());
        values.put(ColumnNames.RESOURCE_OPERATED_UPON.getColumnName(), AttributeValue.builder().s(record.getResourceOperatedUpon()).build());
        values.put(ColumnNames.APPLICATION_CODE.getColumnName(), AttributeValue.builder().s(record.getApplicationCode()).build());
        values.put(ColumnNames.ACTION_PERFORMED.getColumnName(), AttributeValue.builder().s(record.getActionPerformed()).build());
        values.put(ColumnNames.USER_AGENT.getColumnName(), AttributeValue.builder().s(StringUtils.defaultIfBlank(record.getClientInfo().getUserAgent(), "N/A")).build());
        values.put(ColumnNames.GEO_LOCATION.getColumnName(), AttributeValue.builder().s(StringUtils.defaultIfBlank(record.getClientInfo().getGeoLocation(), "N/A")).build());
        values.put(ColumnNames.TENANT.getColumnName(), AttributeValue.builder().s(StringUtils.defaultIfBlank(record.getClientInfo().getTenant(), "N/A")).build());

        val time = record.getWhenActionWasPerformed().toEpochSecond(ZoneOffset.UTC);
        values.put(ColumnNames.WHEN_ACTION_PERFORMED.getColumnName(), AttributeValue.builder().s(String.valueOf(time)).build());
        LOGGER.debug("Created attribute values [{}] based on [{}]", values, record);
        return values;
    }

    /**
     * Create tables.
     *
     * @param deleteTables the delete tables
     */
    public void createTable(final boolean deleteTables) {
        FunctionUtils.doUnchecked(param -> {
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
        });
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
     * @param whereClause the where clause
     * @return the audit records
     */
    public List<? extends AuditActionContext> getAuditRecords(final Map<AuditTrailManager.WhereClauseFields, Object> whereClause) {
        val localDate = (LocalDateTime) whereClause.get(AuditTrailManager.WhereClauseFields.DATE);
        val time = DateTimeUtils.dateOf(localDate).getTime();
        val queryKeys = CollectionUtils.<DynamoDbQueryBuilder>wrap(DynamoDbQueryBuilder.builder()
            .key(ColumnNames.WHEN_ACTION_PERFORMED.getColumnName())
            .attributeValue(List.of(AttributeValue.builder().s(String.valueOf(time)).build()))
            .operator(ComparisonOperator.GE)
            .build());
        if (whereClause.containsKey(AuditTrailManager.WhereClauseFields.PRINCIPAL)) {
            queryKeys.add(DynamoDbQueryBuilder.builder()
                .key(ColumnNames.PRINCIPAL.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().s(whereClause.get(AuditTrailManager.WhereClauseFields.PRINCIPAL).toString()).build()))
                .operator(ComparisonOperator.EQ)
                .build());
        }
        val count = whereClause.containsKey(AuditTrailManager.WhereClauseFields.COUNT)
            ? (long) whereClause.get(AuditTrailManager.WhereClauseFields.COUNT)
            : AbstractAuditTrailManager.DEFAULT_MAX_AUDIT_RECORDS_TO_FETCH;
        return DynamoDbTableUtils.getRecordsByKeys(amazonDynamoDBClient,
                dynamoDbProperties.getTableName(),
                count,
                queryKeys,
                item -> {
                    val principal = item.get(ColumnNames.PRINCIPAL.getColumnName()).s();
                    val actionPerformed = item.get(ColumnNames.ACTION_PERFORMED.getColumnName()).s();
                    val appCode = item.get(ColumnNames.APPLICATION_CODE.getColumnName()).s();
                    val clientIp = item.get(ColumnNames.CLIENT_IP_ADDRESS.getColumnName()).s();
                    val serverIp = item.get(ColumnNames.SERVER_IP_ADDRESS.getColumnName()).s();
                    val resource = item.get(ColumnNames.RESOURCE_OPERATED_UPON.getColumnName()).s();
                    val userAgent = item.get(ColumnNames.USER_AGENT.getColumnName()).s();
                    val geoLocation = item.get(ColumnNames.GEO_LOCATION.getColumnName()).s();
                    val tenant = item.get(ColumnNames.TENANT.getColumnName()).s();
                    val auditTime = Long.parseLong(item.get(ColumnNames.WHEN_ACTION_PERFORMED.getColumnName()).s());
                    val clientInfo = new ClientInfo(clientIp, serverIp, userAgent, geoLocation).setTenant(tenant);
                    return new AuditActionContext(principal, resource, actionPerformed, appCode,
                        DateTimeUtils.localDateTimeOf(auditTime), clientInfo);
                })
            .collect(Collectors.toList());
    }


    /**
     * Column names for tables holding records.
     */
    @Getter
    @RequiredArgsConstructor
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
         * userAgent column.
         */
        USER_AGENT("userAgent"),
        /**
         * Geolocation column.
         */
        GEO_LOCATION("geoLocation"),
        /**
         * Tenant column.
         */
        TENANT("tenant"),
        /**
         * applicationCode column.
         */
        APPLICATION_CODE("applicationCode");

        private final String columnName;
    }
}
