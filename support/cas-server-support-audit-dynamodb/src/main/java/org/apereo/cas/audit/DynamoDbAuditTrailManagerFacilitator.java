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
import org.apereo.inspektr.audit.AuditActionCount;
import org.apereo.inspektr.audit.AuditSummaryQuery;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.apereo.inspektr.common.web.ClientInfo;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchGetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.KeysAndAttributes;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

/**
 * This is {@link DynamoDbAuditTrailManagerFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class DynamoDbAuditTrailManagerFacilitator {
    /**
     * Prefix of the items that count records per hour, one attribute per action.
     */
    public static final String SUMMARY_ID_PREFIX = "CasAuditSummary:";

    /**
     * How many items share the counts of one hour, so a busy hour does not write to a single item.
     */
    public static final int SUMMARY_SHARDS = 8;

    private static final int BATCH_GET_LIMIT = 100;

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
        values.put(ColumnNames.ID.getColumnName(), AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        values.put(ColumnNames.PRINCIPAL.getColumnName(), AttributeValue.builder().s(record.getPrincipal()).build());
        values.put(ColumnNames.CLIENT_IP_ADDRESS.getColumnName(), AttributeValue.builder().s(record.getClientInfo().getClientIpAddress()).build());
        values.put(ColumnNames.SERVER_IP_ADDRESS.getColumnName(), AttributeValue.builder().s(record.getClientInfo().getServerIpAddress()).build());
        values.put(ColumnNames.RESOURCE_OPERATED_UPON.getColumnName(), AttributeValue.builder().s(record.getResourceOperatedUpon()).build());
        values.put(ColumnNames.APPLICATION_CODE.getColumnName(), AttributeValue.builder().s(record.getApplicationCode()).build());
        values.put(ColumnNames.ACTION_PERFORMED.getColumnName(), AttributeValue.builder().s(record.getActionPerformed()).build());
        values.put(ColumnNames.USER_AGENT.getColumnName(), AttributeValue.builder().s(StringUtils.defaultIfBlank(record.getClientInfo().getUserAgent(), "N/A")).build());
        values.put(ColumnNames.GEO_LOCATION.getColumnName(), AttributeValue.builder().s(StringUtils.defaultIfBlank(record.getClientInfo().getGeoLocation(), "N/A")).build());
        values.put(ColumnNames.TENANT.getColumnName(), AttributeValue.builder().s(StringUtils.defaultIfBlank(record.getClientInfo().getTenant(), "N/A")).build());

        val time = record.getWhenActionWasPerformed().toInstant(ZoneOffset.UTC).toEpochMilli();
        values.put(ColumnNames.WHEN_ACTION_PERFORMED.getColumnName(), AttributeValue.builder().s(String.valueOf(time)).build());
        LOGGER.debug("Created attribute values [{}] based on [{}]", values, record);
        return values;
    }

    /**
     * Create tables. Each record gets its own random identifier as the hash key, so that the records of a principal
     * accumulate instead of replacing each other.
     *
     * @param deleteTables the delete tables
     */
    public void createTable(final boolean deleteTables) {
        FunctionUtils.doUnchecked(param -> {
            val attributes = List.of(AttributeDefinition.builder()
                .attributeName(ColumnNames.ID.getColumnName())
                .attributeType(ScalarAttributeType.S)
                .build());
            val schema = List.of(KeySchemaElement.builder()
                .attributeName(ColumnNames.ID.getColumnName())
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
     * Count the record in the item kept for its hour; the item is picked among a few for that hour, at random.
     *
     * @param record the record
     */
    public void count(final AuditActionContext record) {
        val shard = ThreadLocalRandom.current().nextInt(SUMMARY_SHARDS);
        val request = UpdateItemRequest.builder()
            .tableName(dynamoDbProperties.getTableName())
            .key(Map.of(ColumnNames.ID.getColumnName(), summaryId(record.getWhenActionWasPerformed(), shard)))
            .updateExpression("ADD #action :one")
            .expressionAttributeNames(Map.of("#action", record.getActionPerformed()))
            .expressionAttributeValues(Map.of(":one", AttributeValue.builder().n("1").build()))
            .build();
        amazonDynamoDBClient.updateItem(request);
    }

    /**
     * Read the counts kept for each hour of the period.
     *
     * @param query the period and actions to count
     * @return the counts
     */
    public List<AuditActionCount> summarize(final AuditSummaryQuery query) {
        val hourOfId = new HashMap<String, LocalDateTime>();
        query.hours().forEach(hour -> IntStream.range(0, SUMMARY_SHARDS)
            .forEach(shard -> hourOfId.put(summaryId(hour, shard).s(), hour)));
        val totals = new LinkedHashMap<LocalDateTime, Map<String, Long>>();
        val ids = List.copyOf(hourOfId.keySet());
        for (var start = 0; start < ids.size(); start += BATCH_GET_LIMIT) {
            var keys = ids.subList(start, Math.min(ids.size(), start + BATCH_GET_LIMIT))
                .stream()
                .map(id -> Map.of(ColumnNames.ID.getColumnName(), AttributeValue.builder().s(id).build()))
                .toList();
            while (!keys.isEmpty()) {
                val request = BatchGetItemRequest.builder()
                    .requestItems(Map.of(dynamoDbProperties.getTableName(), KeysAndAttributes.builder().keys(keys).build()))
                    .build();
                val response = amazonDynamoDBClient.batchGetItem(request);
                response.responses().getOrDefault(dynamoDbProperties.getTableName(), List.of()).forEach(item -> {
                    val hour = Objects.requireNonNull(hourOfId.get(Objects.requireNonNull(item.get(ColumnNames.ID.getColumnName())).s()));
                    item.forEach((attribute, value) -> {
                        if (query.actions().contains(attribute) && value.n() != null) {
                            totals.computeIfAbsent(hour, _ -> new HashMap<>()).merge(attribute, Long.parseLong(value.n()), Long::sum);
                        }
                    });
                });
                val unprocessed = response.unprocessedKeys().get(dynamoDbProperties.getTableName());
                keys = unprocessed != null && unprocessed.hasKeys() ? unprocessed.keys() : List.of();
            }
        }
        return totals.entrySet()
            .stream()
            .flatMap(entry -> entry.getValue().entrySet().stream()
                .map(count -> new AuditActionCount(entry.getKey(), count.getKey(), count.getValue())))
            .sorted(Comparator.comparing(AuditActionCount::hour))
            .toList();
    }

    private static AttributeValue summaryId(final LocalDateTime time, final int shard) {
        val hour = AuditSummaryQuery.hourOf(time).toEpochSecond(ZoneOffset.UTC);
        return AttributeValue.builder().s(SUMMARY_ID_PREFIX + hour + ':' + shard).build();
    }

    /**
     * Remove all.
     */
    public void removeAll() {
        createTable(true);
    }


    /**
     * Gets audit records. The time of each record is kept in epoch milliseconds, so it is compared with, and read back
     * as, milliseconds.
     *
     * @param whereClause the where clause
     * @return the audit records
     */
    public List<? extends AuditActionContext> getAuditRecords(final Map<AuditTrailManager.WhereClauseFields, Object> whereClause) {
        val localDate = (LocalDateTime) whereClause.get(AuditTrailManager.WhereClauseFields.DATE);
        val time = Objects.requireNonNull(localDate).toInstant(ZoneOffset.UTC).toEpochMilli();
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
                    val principal = Objects.requireNonNull(item.get(ColumnNames.PRINCIPAL.getColumnName())).s();
                    val actionPerformed = Objects.requireNonNull(item.get(ColumnNames.ACTION_PERFORMED.getColumnName())).s();
                    val appCode = Objects.requireNonNull(item.get(ColumnNames.APPLICATION_CODE.getColumnName())).s();
                    val clientIp = Objects.requireNonNull(item.get(ColumnNames.CLIENT_IP_ADDRESS.getColumnName())).s();
                    val serverIp = Objects.requireNonNull(item.get(ColumnNames.SERVER_IP_ADDRESS.getColumnName())).s();
                    val resource = Objects.requireNonNull(item.get(ColumnNames.RESOURCE_OPERATED_UPON.getColumnName())).s();
                    val userAgent = Objects.requireNonNull(item.get(ColumnNames.USER_AGENT.getColumnName())).s();
                    val geoLocation = Objects.requireNonNull(item.get(ColumnNames.GEO_LOCATION.getColumnName())).s();
                    val tenant = Objects.requireNonNull(item.get(ColumnNames.TENANT.getColumnName())).s();
                    val auditTime = Long.parseLong(Objects.requireNonNull(item.get(ColumnNames.WHEN_ACTION_PERFORMED.getColumnName())).s());
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
         * id column.
         */
        ID("id"),
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
