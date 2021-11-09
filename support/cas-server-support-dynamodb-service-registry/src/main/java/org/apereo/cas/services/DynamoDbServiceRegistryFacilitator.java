package org.apereo.cas.services;

import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbServiceRegistryProperties;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.StringSerializer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link DynamoDbServiceRegistryFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
public class DynamoDbServiceRegistryFacilitator {

    private final StringSerializer<RegisteredService> jsonSerializer = new RegisteredServiceJsonSerializer();

    private final DynamoDbServiceRegistryProperties dynamoDbProperties;

    private final DynamoDbClient amazonDynamoDBClient;

    public DynamoDbServiceRegistryFacilitator(final DynamoDbServiceRegistryProperties dynamoDbProperties,
                                              final DynamoDbClient amazonDynamoDBClient) {
        this.dynamoDbProperties = dynamoDbProperties;
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        if (!dynamoDbProperties.isPreventTableCreationOnStartup()) {
            createServicesTable(dynamoDbProperties.isDropTablesOnStartup());
        }
    }

    /**
     * Delete boolean.
     *
     * @param service the service
     * @return true/false
     */
    public boolean delete(final RegisteredService service) {
        val del = DeleteItemRequest.builder()
            .tableName(dynamoDbProperties.getTableName())
            .key(CollectionUtils.wrap(ColumnNames.ID.getColumnName(), AttributeValue.builder().s(String.valueOf(service.getId())).build()))
            .build();
        LOGGER.debug("Submitting delete request [{}] for service [{}]", del, service);
        val res = amazonDynamoDBClient.deleteItem(del);
        LOGGER.debug("Delete request came back with result [{}]", res);
        return res != null;
    }

    /**
     * Count long.
     *
     * @return the long
     */
    public long count() {
        val scan = ScanRequest.builder().tableName(dynamoDbProperties.getTableName()).build();
        LOGGER.debug("Scanning table with request [{}] to count items", scan);
        val result = this.amazonDynamoDBClient.scan(scan);
        LOGGER.debug("Scanned table with result [{}]", scan);
        return result.count();
    }

    /**
     * Gets all.
     *
     * @return the all
     */
    public List<RegisteredService> getAll() {
        val scan = ScanRequest.builder().tableName(dynamoDbProperties.getTableName()).build();
        LOGGER.debug("Scanning table with request [{}]", scan);
        val result = this.amazonDynamoDBClient.scan(scan);
        LOGGER.debug("Scanned table with result [{}]", scan);
        return result.items()
            .stream()
            .map(this::deserializeServiceFromBinaryBlob)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparingInt(RegisteredService::getEvaluationOrder))
            .collect(Collectors.toList());
    }

    /**
     * Get registered service.
     *
     * @param id the id
     * @return the registered service
     */
    public RegisteredService get(final long id) {
        val keys = new HashMap<String, AttributeValue>();
        keys.put(ColumnNames.ID.getColumnName(), AttributeValue.builder().s(String.valueOf(id)).build());
        return getRegisteredServiceByKeys(keys);
    }

    /**
     * Put.
     *
     * @param service the service
     */
    public void put(final RegisteredService service) {
        val values = buildTableAttributeValuesMapFromService(service);
        val putItemRequest = PutItemRequest.builder().tableName(dynamoDbProperties.getTableName()).item(values).build();
        LOGGER.debug("Submitting put request [{}] for service id [{}]", putItemRequest, service.getServiceId());
        val putItemResult = amazonDynamoDBClient.putItem(putItemRequest);
        LOGGER.debug("Service added with result [{}]", putItemResult);
    }

    /**
     * Create tables.
     *
     * @param deleteTables the delete tables
     */
    @SneakyThrows
    public void createServicesTable(final boolean deleteTables) {
        DynamoDbTableUtils.createTable(amazonDynamoDBClient, dynamoDbProperties,
            dynamoDbProperties.getTableName(), deleteTables,
            List.of(AttributeDefinition.builder()
                .attributeName(ColumnNames.ID.getColumnName())
                .attributeType(ScalarAttributeType.S)
                .build()),
            List.of(KeySchemaElement.builder()
                .attributeName(ColumnNames.ID.getColumnName())
                .keyType(KeyType.HASH)
                .build()));
    }

    /**
     * Build table attribute values from map.
     *
     * @param service the service
     * @return the map
     */
    public Map<String, AttributeValue> buildTableAttributeValuesMapFromService(final RegisteredService service) {
        val values = new HashMap<String, AttributeValue>();
        values.put(ColumnNames.ID.getColumnName(), AttributeValue.builder().s(String.valueOf(service.getId())).build());
        values.put(ColumnNames.NAME.getColumnName(), AttributeValue.builder().s(String.valueOf(service.getName())).build());
        values.put(ColumnNames.DESCRIPTION.getColumnName(), AttributeValue.builder().s(String.valueOf(service.getDescription())).build());
        values.put(ColumnNames.SERVICE_ID.getColumnName(), AttributeValue.builder().s(String.valueOf(service.getServiceId())).build());
        val out = new ByteArrayOutputStream();
        jsonSerializer.to(out, service);
        values.put(ColumnNames.ENCODED.getColumnName(), AttributeValue.builder().b(SdkBytes.fromByteArray(out.toByteArray())).build());
        LOGGER.debug("Created attribute values [{}] based on provided service [{}]", values, service);
        return values;
    }

    /**
     * Delete all.
     */
    public void deleteAll() {
        createServicesTable(true);
    }

    @Getter
    @RequiredArgsConstructor
    private enum ColumnNames {
        ID("id"),
        NAME("name"),
        DESCRIPTION("description"),
        SERVICE_ID("serviceId"),
        ENCODED("encoded");

        private final String columnName;
    }

    private RegisteredService deserializeServiceFromBinaryBlob(final Map<String, AttributeValue> returnItem) {
        val bb = returnItem.get(ColumnNames.ENCODED.getColumnName()).b();
        LOGGER.debug("Located binary encoding of service item [{}]. Transforming item into service object", returnItem);

        try (val is = bb.asInputStream()) {
            return this.jsonSerializer.from(is);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    private RegisteredService getRegisteredServiceByKeys(final Map<String, AttributeValue> keys) {
        try {
            val request = GetItemRequest.builder().key(keys).tableName(dynamoDbProperties.getTableName()).build();
            LOGGER.debug("Submitting request [{}] to get service with keys [{}]", request, keys);
            val returnItem = amazonDynamoDBClient.getItem(request).item();
            if (returnItem != null) {
                val service = deserializeServiceFromBinaryBlob(returnItem);
                LOGGER.debug("Located service [{}]", service);
                return service;
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }
}
