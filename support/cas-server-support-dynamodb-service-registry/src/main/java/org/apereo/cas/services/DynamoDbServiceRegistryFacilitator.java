package org.apereo.cas.services;

import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbServiceRegistryProperties;
import org.apereo.cas.services.util.RegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.StringSerializer;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
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
    private final AmazonDynamoDB amazonDynamoDBClient;

    public DynamoDbServiceRegistryFacilitator(final DynamoDbServiceRegistryProperties dynamoDbProperties,
                                              final AmazonDynamoDB amazonDynamoDBClient) {
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
        val del = new DeleteItemRequest().withTableName(dynamoDbProperties.getTableName())
            .withKey(CollectionUtils.wrap(ColumnNames.ID.getColumnName(), new AttributeValue(String.valueOf(service.getId()))));
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
        val scan = new ScanRequest(dynamoDbProperties.getTableName());
        LOGGER.debug("Scanning table with request [{}] to count items", scan);
        val result = this.amazonDynamoDBClient.scan(scan);
        LOGGER.debug("Scanned table with result [{}]", scan);
        return result.getCount();
    }

    /**
     * Gets all.
     *
     * @return the all
     */
    public List<RegisteredService> getAll() {
        val scan = new ScanRequest(dynamoDbProperties.getTableName());
        LOGGER.debug("Scanning table with request [{}]", scan);
        val result = this.amazonDynamoDBClient.scan(scan);
        LOGGER.debug("Scanned table with result [{}]", scan);
        return result.getItems()
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
    public RegisteredService get(final String id) {
        if (NumberUtils.isCreatable(id)) {
            return get(Long.parseLong(id));
        }

        val scanRequest = new ScanRequest(dynamoDbProperties.getTableName());
        val cond = new Condition();
        cond.setComparisonOperator(ComparisonOperator.EQ);
        cond.setAttributeValueList(List.of(new AttributeValue(id)));
        scanRequest.addScanFilterEntry(ColumnNames.SERVICE_ID.getColumnName(), cond);
        LOGGER.debug("Submitting request [{}] to get service for id [{}]", scanRequest, id);
        val items = amazonDynamoDBClient.scan(scanRequest).getItems();
        if (items.isEmpty()) {
            LOGGER.debug("No service definition could be found for [{}]", id);
            return null;
        }
        val service = deserializeServiceFromBinaryBlob(items.get(0));
        LOGGER.debug("Located service [{}]", service);
        return service;
    }

    /**
     * Get registered service.
     *
     * @param id the id
     * @return the registered service
     */
    public RegisteredService get(final long id) {
        val keys = new HashMap<String, AttributeValue>();
        keys.put(ColumnNames.ID.getColumnName(), new AttributeValue(String.valueOf(id)));
        return getRegisteredServiceByKeys(keys);
    }

    private RegisteredService deserializeServiceFromBinaryBlob(final Map<String, AttributeValue> returnItem) {
        val bb = returnItem.get(ColumnNames.ENCODED.getColumnName()).getB();
        LOGGER.debug("Located binary encoding of service item [{}]. Transforming item into service object", returnItem);

        try (val is = new ByteArrayInputStream(bb.array(), bb.arrayOffset() + bb.position(), bb.remaining())) {
            return this.jsonSerializer.from(is);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private RegisteredService getRegisteredServiceByKeys(final Map<String, AttributeValue> keys) {
        try {
            val request = new GetItemRequest().withKey(keys).withTableName(dynamoDbProperties.getTableName());
            LOGGER.debug("Submitting request [{}] to get service with keys [{}]", request, keys);
            val returnItem = amazonDynamoDBClient.getItem(request).getItem();
            if (returnItem != null) {
                val service = deserializeServiceFromBinaryBlob(returnItem);
                LOGGER.debug("Located service [{}]", service);
                return service;
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Put.
     *
     * @param service the service
     */
    public void put(final RegisteredService service) {
        val values = buildTableAttributeValuesMapFromService(service);
        val putItemRequest = new PutItemRequest(dynamoDbProperties.getTableName(), values);
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
        LOGGER.debug("Attempting to create DynamoDb services table");
        val request = new CreateTableRequest().withAttributeDefinitions(
            new AttributeDefinition(ColumnNames.ID.getColumnName(), ScalarAttributeType.S))
            .withKeySchema(new KeySchemaElement(ColumnNames.ID.getColumnName(), KeyType.HASH))
            .withProvisionedThroughput(new ProvisionedThroughput(dynamoDbProperties.getReadCapacity(),
                dynamoDbProperties.getWriteCapacity())).withTableName(dynamoDbProperties.getTableName());
        if (deleteTables) {
            val delete = new DeleteTableRequest(request.getTableName());
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
     * Build table attribute values from map.
     *
     * @param service the service
     * @return the map
     */
    public Map<String, AttributeValue> buildTableAttributeValuesMapFromService(final RegisteredService service) {
        val values = new HashMap<String, AttributeValue>();
        values.put(ColumnNames.ID.getColumnName(), new AttributeValue(String.valueOf(service.getId())));
        values.put(ColumnNames.NAME.getColumnName(), new AttributeValue(service.getName()));
        values.put(ColumnNames.DESCRIPTION.getColumnName(), new AttributeValue(service.getDescription()));
        values.put(ColumnNames.SERVICE_ID.getColumnName(), new AttributeValue(service.getServiceId()));
        val out = new ByteArrayOutputStream();
        jsonSerializer.to(out, service);
        values.put(ColumnNames.ENCODED.getColumnName(), new AttributeValue().withB(ByteBuffer.wrap(out.toByteArray())));
        LOGGER.debug("Created attribute values [{}] based on provided service [{}]", values, service);
        return values;
    }

    @Getter
    private enum ColumnNames {

        ID("id"), NAME("name"), DESCRIPTION("description"), SERVICE_ID("serviceId"), ENCODED("encoded");

        private final String columnName;

        ColumnNames(final String columnName) {
            this.columnName = columnName;
        }
    }
}
