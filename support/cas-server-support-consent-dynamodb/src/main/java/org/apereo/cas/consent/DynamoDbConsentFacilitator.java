package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.consent.DynamoDbConsentProperties;
import org.apereo.cas.dynamodb.DynamoDbQueryBuilder;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link DynamoDbConsentFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class DynamoDbConsentFacilitator {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).build().toObjectMapper();

    private final DynamoDbConsentProperties dynamoDbProperties;
    private final DynamoDbClient amazonDynamoDBClient;


    private static Map<String, AttributeValue> buildTableAttributeValuesMap(final ConsentDecision record) {
        val values = new HashMap<String, AttributeValue>();
        values.put(ColumnNames.PRINCIPAL.getColumnName(), AttributeValue.builder().s(record.getPrincipal()).build());
        values.put(ColumnNames.SERVICE.getColumnName(), AttributeValue.builder().s(record.getService()).build());
        values.put(ColumnNames.ID.getColumnName(), AttributeValue.builder().n(String.valueOf(record.getId())).build());

        val body = MAPPER.writeValueAsString(record);
        values.put(ColumnNames.BODY.getColumnName(), AttributeValue.builder().s(body).build());

        val time = DateTimeUtils.dateOf(record.getCreatedDate());
        values.put(ColumnNames.CREATED_DATE.getColumnName(), AttributeValue.builder().s(String.valueOf(time)).build());

        LOGGER.debug("Created attribute values [{}] based on [{}]", values, record);
        return values;
    }

    private static ConsentDecision extractAttributeValuesFrom(final Map<String, AttributeValue> item) {
        val principal = item.get(ColumnNames.PRINCIPAL.getColumnName()).s();
        val id = Long.valueOf(item.get(ColumnNames.ID.getColumnName()).n());
        val body = item.get(ColumnNames.BODY.getColumnName()).s();
        LOGGER.debug("Extracting consent decision id [{}] for [{}]", id, principal);
        return FunctionUtils.doUnchecked(() -> MAPPER.readValue(body, ConsentDecision.class));
    }

    /**
     * Create tables.
     *
     * @param deleteTables the delete tables
     */
    public void createTable(final boolean deleteTables) {
        val attributes = List.of(AttributeDefinition.builder()
            .attributeName(ColumnNames.ID.getColumnName())
            .attributeType(ScalarAttributeType.N)
            .build());
        val schema = List.of(KeySchemaElement.builder()
            .attributeName(ColumnNames.ID.getColumnName())
            .keyType(KeyType.HASH)
            .build());
        FunctionUtils.doUnchecked(_ -> DynamoDbTableUtils.createTable(amazonDynamoDBClient, dynamoDbProperties,
            dynamoDbProperties.getTableName(), deleteTables, attributes, schema));
    }

    /**
     * Save.
     *
     * @param record the record
     */
    public void save(final ConsentDecision record) {
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
     * Load collection.
     *
     * @return the collection
     */
    public Collection<? extends ConsentDecision> load() {
        return getRecordsByKeys(List.of()).collect(Collectors.toList());
    }

    /**
     * Find collection.
     *
     * @param principal the principal
     * @return the collection
     */
    public Collection<? extends ConsentDecision> find(final String principal) {
        val query = DynamoDbQueryBuilder.builder()
            .key(ColumnNames.PRINCIPAL.getColumnName())
            .attributeValue(List.of(AttributeValue.builder().s(principal).build()))
            .operator(ComparisonOperator.EQ)
            .build();
        return getRecordsByKeys(List.of(query)).collect(Collectors.toList());
    }

    /**
     * Find consent decision.
     *
     * @param service   the service
     * @param principal the principal
     * @return the consent decision
     */
    public ConsentDecision find(final Service service, final Principal principal) {
        val query = List.of(
            DynamoDbQueryBuilder.builder()
                .key(ColumnNames.PRINCIPAL.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().s(principal.getId()).build()))
                .operator(ComparisonOperator.EQ)
                .build(),
            DynamoDbQueryBuilder.builder()
                .key(ColumnNames.SERVICE.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().s(service.getId()).build()))
                .operator(ComparisonOperator.GE)
                .build());
        return getRecordsByKeys(query).findFirst().orElse(null);
    }

    /**
     * Delete.
     *
     * @param id        the id
     * @param principal the principal
     * @return true/false
     */
    public boolean delete(final long id, final String principal) {
        val keys = List.of(
            DynamoDbQueryBuilder.builder()
                .key(ColumnNames.PRINCIPAL.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().s(principal).build()))
                .operator(ComparisonOperator.EQ)
                .build(),
            DynamoDbQueryBuilder.builder()
                .key(ColumnNames.ID.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().n(String.valueOf(id)).build()))
                .operator(ComparisonOperator.GE)
                .build());

        val results = getRecordsByKeys(keys);
        val deleteCount = results
            .map(decision -> {
                val del = DeleteItemRequest.builder()
                    .tableName(dynamoDbProperties.getTableName())
                    .key(CollectionUtils.wrap(ColumnNames.ID.getColumnName(),
                        AttributeValue.builder().n(String.valueOf(decision.getId())).build()))
                    .build();
                LOGGER.debug("Submitting delete request [{}] for decision id [{}] and principal [{}]", del, id, principal);
                val res = amazonDynamoDBClient.deleteItem(del);
                LOGGER.debug("Delete request came back with result [{}]", res);
                return res;
            })
            .filter(Objects::nonNull)
            .count();
        return deleteCount > 0;
    }

    /**
     * Delete.
     *
     * @param principal the principal
     * @return true/false
     */
    public boolean delete(final String principal) {
        val keys = List.of(
            DynamoDbQueryBuilder.builder()
                .key(ColumnNames.PRINCIPAL.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().s(principal).build()))
                .operator(ComparisonOperator.EQ)
                .build());
        val results = getRecordsByKeys(keys);
        results.forEach(decision -> {
            val del = DeleteItemRequest.builder()
                .tableName(dynamoDbProperties.getTableName())
                .key(CollectionUtils.wrap(ColumnNames.ID.getColumnName(),
                    AttributeValue.builder().n(String.valueOf(decision.getId())).build()))
                .build();
            LOGGER.debug("Submitting delete request [{}] for decision id [{}] and principal [{}]",
                del, decision.getId(), principal);
            val res = amazonDynamoDBClient.deleteItem(del);
            LOGGER.debug("Delete request came back with result [{}]", res);
        });
        return true;
    }

    /**
     * Column names for tables holding records.
     */
    @Getter
    @RequiredArgsConstructor
    public enum ColumnNames {
        /**
         * Principal column.
         */
        PRINCIPAL("principal"),
        /**
         * ID column.
         */
        ID("id"),
        /**
         * Service column.
         */
        SERVICE("service"),
        /**
         * Created-Date column.
         */
        CREATED_DATE("createdDate"),
        /**
         * Body column.
         */
        BODY("body");

        private final String columnName;
    }

    private Stream<ConsentDecision> getRecordsByKeys(final List<? extends DynamoDbQueryBuilder> queries) {
        return DynamoDbTableUtils.getRecordsByKeys(amazonDynamoDBClient,
            dynamoDbProperties.getTableName(),
            queries,
            DynamoDbConsentFacilitator::extractAttributeValuesFrom);
    }
}
