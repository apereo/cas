package org.apereo.cas.services;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbServiceRegistryProperties;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link DynamoDbServiceRegistryFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DynamoDbServiceRegistryFacilitator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbServiceRegistryFacilitator.class);

    private final StringSerializer<RegisteredService> jsonSerializer = new DefaultRegisteredServiceJsonSerializer();

    private enum ColumnNames {
        ID("id"),
        NAME("name"),
        DESCRIPTION("description"),
        SERVICE_ID("serviceId"),
        ENCODED("encoded");

        private final String name;

        ColumnNames(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final DynamoDbServiceRegistryProperties dynamoDbProperties;
    private final AmazonDynamoDBClient amazonDynamoDBClient;

    public DynamoDbServiceRegistryFacilitator(final DynamoDbServiceRegistryProperties dynamoDbProperties,
                                              final AmazonDynamoDBClient amazonDynamoDBClient) {
        this.dynamoDbProperties = dynamoDbProperties;
        this.amazonDynamoDBClient = amazonDynamoDBClient;
        if (!dynamoDbProperties.isPreventTableCreationOnStartup()){
            createServicesTable(dynamoDbProperties.isDropTablesOnStartup());
        }
    }

    /**
     * Delete boolean.
     *
     * @param service the service
     * @return the boolean
     */
    public boolean delete(final RegisteredService service) {
        final DeleteItemRequest del = new DeleteItemRequest()
            .withTableName(dynamoDbProperties.getTableName())
            .withKey(CollectionUtils.wrap(ColumnNames.ID.getName(),
                new AttributeValue(String.valueOf(service.getId()))));

        LOGGER.debug("Submitting delete request [{}] for service [{}]", del, service);
        final DeleteItemResult res = amazonDynamoDBClient.deleteItem(del);
        LOGGER.debug("Delete request came back with result [{}]", res);
        return res != null;

    }

    /**
     * Count long.
     *
     * @return the long
     */
    public long count() {
        final ScanRequest scan = new ScanRequest(dynamoDbProperties.getTableName());
        LOGGER.debug("Scanning table with request [{}] to count items", scan);
        final ScanResult result = this.amazonDynamoDBClient.scan(scan);
        LOGGER.debug("Scanned table with result [{}]", scan);
        return result.getCount();
    }

    /**
     * Gets all.
     *
     * @return the all
     */
    public List<RegisteredService> getAll() {
        final List<RegisteredService> services = new ArrayList<>();
        final ScanRequest scan = new ScanRequest(dynamoDbProperties.getTableName());
        LOGGER.debug("Scanning table with request [{}]", scan);
        final ScanResult result = this.amazonDynamoDBClient.scan(scan);
        LOGGER.debug("Scanned table with result [{}]", scan);

        services.addAll(result.getItems()
            .stream()
            .map(this::deserializeServiceFromBinaryBlob)
            .sorted((o1, o2) -> Integer.valueOf(o1.getEvaluationOrder()).compareTo(o2.getEvaluationOrder()))
            .collect(Collectors.toList()));
        return services;
    }

    /**
     * Get registered service.
     *
     * @param id the id
     * @return the registered service
     */
    public RegisteredService get(final String id) {
        final Map<String, AttributeValue> keys = new HashMap<>();
        keys.put(ColumnNames.SERVICE_ID.getName(), new AttributeValue(id));
        return getRegisteredServiceByKeys(keys);
    }

    /**
     * Get registered service.
     *
     * @param id the id
     * @return the registered service
     */
    public RegisteredService get(final long id) {
        final Map<String, AttributeValue> keys = new HashMap<>();
        keys.put(ColumnNames.ID.getName(), new AttributeValue(String.valueOf(id)));
        return getRegisteredServiceByKeys(keys);
    }

    private RegisteredService deserializeServiceFromBinaryBlob(final Map<String, AttributeValue> returnItem) {
        final ByteBuffer bb = returnItem.get(ColumnNames.ENCODED.getName()).getB();
        LOGGER.debug("Located binary encoding of service item [{}]. Transforming item into service object", returnItem);
        final ByteArrayInputStream is = new ByteArrayInputStream(bb.array());
        return this.jsonSerializer.from(is);
    }

    private RegisteredService getRegisteredServiceByKeys(final Map<String, AttributeValue> keys) {
        final GetItemRequest request = new GetItemRequest()
            .withKey(keys)
            .withTableName(dynamoDbProperties.getTableName());

        LOGGER.debug("Submitting request [{}] to get service with keys [{}]", request, keys);
        final Map<String, AttributeValue> returnItem = amazonDynamoDBClient.getItem(request).getItem();
        if (returnItem != null) {
            final RegisteredService service = deserializeServiceFromBinaryBlob(returnItem);
            LOGGER.debug("Located service [{}]", service);
            return service;
        }
        return null;
    }

    /**
     * Put.
     *
     * @param service the service
     */
    public void put(final RegisteredService service) {
        final Map<String, AttributeValue> values = buildTableAttributeValuesMapFromService(service);
        final PutItemRequest putItemRequest = new PutItemRequest(dynamoDbProperties.getTableName(), values);
        LOGGER.debug("Submitting put request [{}] for service id [{}]", putItemRequest, service.getServiceId());
        final PutItemResult putItemResult = amazonDynamoDBClient.putItem(putItemRequest);
        LOGGER.debug("Service added with result [{}]", putItemResult);
    }

    /**
     * Create tables.
     *
     * @param deleteTables the delete tables
     */
    public void createServicesTable(final boolean deleteTables) {
        try {
            final CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(new AttributeDefinition(ColumnNames.ID.getName(), ScalarAttributeType.S))
                .withKeySchema(new KeySchemaElement(ColumnNames.ID.getName(), KeyType.HASH))
                .withProvisionedThroughput(new ProvisionedThroughput(dynamoDbProperties.getReadCapacity(),
                    dynamoDbProperties.getWriteCapacity()))
                .withTableName(dynamoDbProperties.getTableName());

            if (deleteTables) {
                final DeleteTableRequest delete = new DeleteTableRequest(request.getTableName());
                LOGGER.debug("Sending delete request [{}] to remove table if necessary", delete);
                TableUtils.deleteTableIfExists(amazonDynamoDBClient, delete);
            }
            LOGGER.debug("Sending delete request [{}] to create table", request);
            TableUtils.createTableIfNotExists(amazonDynamoDBClient, request);

            LOGGER.debug("Waiting until table [{}] becomes active...", request.getTableName());
            TableUtils.waitUntilActive(amazonDynamoDBClient, request.getTableName());

            final DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(request.getTableName());
            LOGGER.debug("Sending request [{}] to obtain table description...", describeTableRequest);

            final TableDescription tableDescription = amazonDynamoDBClient.describeTable(describeTableRequest).getTable();
            LOGGER.debug("Located newly created table with description: [{}]", tableDescription);
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Build table attribute values from map.
     *
     * @param service the service
     * @return the map
     */
    public Map<String, AttributeValue> buildTableAttributeValuesMapFromService(final RegisteredService service) {
        final Map<String, AttributeValue> values = new HashMap<>();
        values.put(ColumnNames.ID.getName(), new AttributeValue(String.valueOf(service.getId())));
        values.put(ColumnNames.NAME.getName(), new AttributeValue(service.getName()));
        values.put(ColumnNames.DESCRIPTION.getName(), new AttributeValue(service.getDescription()));
        values.put(ColumnNames.SERVICE_ID.getName(), new AttributeValue(service.getServiceId()));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        jsonSerializer.to(out, service);
        values.put(ColumnNames.ENCODED.getName(), new AttributeValue().withB(ByteBuffer.wrap(out.toByteArray())));
        LOGGER.debug("Created attribute values [{}] based on provided service [{}]", values, service);
        return values;
    }

}
