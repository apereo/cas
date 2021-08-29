package org.apereo.cas.webauthn;

import org.apereo.cas.configuration.model.support.mfa.webauthn.WebAuthnDynamoDbMultifactorProperties;
import org.apereo.cas.dynamodb.DynamoDbQueryBuilder;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.util.CollectionUtils;

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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link DynamoDbWebAuthnFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class DynamoDbWebAuthnFacilitator {
    private final WebAuthnDynamoDbMultifactorProperties dynamoDbProperties;

    private final DynamoDbClient amazonDynamoDBClient;

    /**
     * Build table attribute values map.
     *
     * @param record the record
     * @return the map
     */
    private static Map<String, AttributeValue> buildTableAttributeValuesMap(final DynamoDbWebAuthnCredentialRegistration record) {
        val values = new HashMap<String, AttributeValue>();
        values.put(ColumnNames.PRINCIPAL.getColumnName(),
            AttributeValue.builder()
                .s(record.getUsername().trim().toLowerCase()).build());
        val records = record.getRecords()
            .stream()
            .map(value -> AttributeValue.builder().s(value).build())
            .collect(Collectors.toList());
        values.put(ColumnNames.RECORDS.getColumnName(), AttributeValue.builder().l(records).build());
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
            List.of(AttributeDefinition.builder()
                .attributeName(ColumnNames.PRINCIPAL.getColumnName())
                .attributeType(ScalarAttributeType.S)
                .build()),
            List.of(KeySchemaElement.builder()
                .attributeName(ColumnNames.PRINCIPAL.getColumnName())
                .keyType(KeyType.HASH)
                .build()));
    }

    /**
     * Gets accounts by user name.
     *
     * @param username the username
     * @return the accounts by
     */
    public Stream<DynamoDbWebAuthnCredentialRegistration> getAccountsBy(final String username) {
        return getRecordsByKeys(DynamoDbQueryBuilder.builder()
            .operator(ComparisonOperator.EQ)
            .attributeValue(List.of(AttributeValue.builder().s(username.trim().toLowerCase()).build()))
            .key(ColumnNames.PRINCIPAL.getColumnName())
            .build());
    }

    /**
     * Load all entries.
     *
     * @return the list
     */
    public Stream<DynamoDbWebAuthnCredentialRegistration> load() {
        return getRecordsByKeys();
    }

    /**
     * Remove.
     *
     * @param username the username
     */
    public void remove(final String username) {
        val del = DeleteItemRequest.builder().tableName(dynamoDbProperties.getTableName())
            .key(CollectionUtils.wrap(ColumnNames.PRINCIPAL.getColumnName(), AttributeValue.builder().s(username).build()))
            .build();
        amazonDynamoDBClient.deleteItem(del);
    }

    /**
     * Save.
     *
     * @param registration the records
     */
    public void save(final DynamoDbWebAuthnCredentialRegistration registration) {
        val values = buildTableAttributeValuesMap(registration);
        val putItemRequest = PutItemRequest.builder().tableName(dynamoDbProperties.getTableName()).item(values).build();
        LOGGER.debug("Submitting put request [{}] for record [{}]", putItemRequest, registration);
        val putItemResult = amazonDynamoDBClient.putItem(putItemRequest);
        LOGGER.debug("Record added with result [{}]", putItemResult);
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
         * records column.
         */
        RECORDS("records");

        private final String columnName;

        ColumnNames(final String columnName) {
            this.columnName = columnName;
        }
    }

    @SneakyThrows
    private Stream<DynamoDbWebAuthnCredentialRegistration> getRecordsByKeys(final DynamoDbQueryBuilder... queries) {
        return DynamoDbTableUtils.getRecordsByKeys(amazonDynamoDBClient, dynamoDbProperties.getTableName(),
            Arrays.stream(queries).collect(Collectors.toList()), item -> {
                val username = item.get(ColumnNames.PRINCIPAL.getColumnName()).s().trim().toLowerCase();
                val records = item.get(ColumnNames.RECORDS.getColumnName()).l();
                return DynamoDbWebAuthnCredentialRegistration.builder()
                    .username(username)
                    .records(records.stream().map(AttributeValue::s).collect(Collectors.toList()))
                    .build();
            });
    }
}
