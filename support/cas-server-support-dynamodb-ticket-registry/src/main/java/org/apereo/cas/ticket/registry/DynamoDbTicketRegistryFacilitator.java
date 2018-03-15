package org.apereo.cas.ticket.registry;

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
import org.apache.commons.lang3.SerializationUtils;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbTicketRegistryProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.util.CollectionUtils;
import org.jooq.lambda.Unchecked;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * This is {@link DynamoDbTicketRegistryFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DynamoDbTicketRegistryFacilitator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDbTicketRegistryFacilitator.class);

    private enum ColumnNames {
        ID("id"),
        PREFIX("prefix"),
        CREATION_TIME("creationTime"),
        COUNT_OF_USES("countOfUses"),
        TIME_TO_LIVE("timeToLive"),
        TIME_TO_IDLE("timeToIdle"),
        ENCODED("encoded");

        private final String name;

        ColumnNames(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final TicketCatalog ticketCatalog;
    private final DynamoDbTicketRegistryProperties dynamoDbProperties;
    private final AmazonDynamoDBClient amazonDynamoDBClient;

    public DynamoDbTicketRegistryFacilitator(final TicketCatalog ticketCatalog,
                                             final DynamoDbTicketRegistryProperties dynamoDbProperties,
                                             final AmazonDynamoDBClient amazonDynamoDBClient) {
        this.ticketCatalog = ticketCatalog;
        this.dynamoDbProperties = dynamoDbProperties;
        this.amazonDynamoDBClient = amazonDynamoDBClient;

        if (!dynamoDbProperties.isPreventTableCreationOnStartup()) {
            createTicketTables(dynamoDbProperties.isDropTablesOnStartup());
        }
    }

    /**
     * Delete.
     *
     * @param ticketId the ticket id
     * @return the boolean
     */
    public boolean delete(final String ticketId) {
        final TicketDefinition metadata = this.ticketCatalog.find(ticketId);
        if (metadata != null) {
            final DeleteItemRequest del = new DeleteItemRequest()
                    .withTableName(metadata.getProperties().getStorageName())
                    .withKey(CollectionUtils.wrap(ColumnNames.ID.getName(), new AttributeValue(ticketId)));
            LOGGER.debug("Submitting delete request [{}] for ticket [{}]", del, ticketId);
            final DeleteItemResult res = amazonDynamoDBClient.deleteItem(del);
            LOGGER.debug("Delete request came back with result [{}]", res);
            return res != null;
        }
        return false;
    }

    /**
     * Delete all.
     *
     * @return the int
     */
    public int deleteAll() {
        final AtomicInteger count = new AtomicInteger();
        final Collection<TicketDefinition> metadata = this.ticketCatalog.findAll();
        metadata.forEach(r -> {
            final ScanRequest scan = new ScanRequest(r.getProperties().getStorageName());
            LOGGER.debug("Submitting scan request [{}] to table [{}]", scan, r.getProperties().getStorageName());
            count.addAndGet(this.amazonDynamoDBClient.scan(scan).getCount());
        });
        createTicketTables(true);
        return count.get();
    }

    /**
     * Gets all.
     *
     * @return the all
     */
    public Collection<Ticket> getAll() {
        final Collection<Ticket> tickets = new ArrayList<>();
        final Collection<TicketDefinition> metadata = this.ticketCatalog.findAll();
        metadata.forEach(r -> {
            final ScanRequest scan = new ScanRequest(r.getProperties().getStorageName());
            LOGGER.debug("Scanning table with request [{}]", scan);
            final ScanResult result = this.amazonDynamoDBClient.scan(scan);
            LOGGER.debug("Scanned table with result [{}]", scan);

            tickets.addAll(result.getItems()
                    .stream()
                    .map(DynamoDbTicketRegistryFacilitator::deserializeTicket)
                    .collect(Collectors.toList()));
        });
        return tickets;
    }

    /**
     * Get ticket.
     *
     * @param ticketId the ticket id
     * @return the ticket
     */
    public Ticket get(final String ticketId) {
        final TicketDefinition metadata = this.ticketCatalog.find(ticketId);
        if (metadata != null) {
            final Map<String, AttributeValue> keys = new HashMap<>();

            keys.put(ColumnNames.ID.getName(), new AttributeValue(ticketId));
            final GetItemRequest request = new GetItemRequest()
                    .withKey(keys)
                    .withTableName(metadata.getProperties().getStorageName());

            LOGGER.debug("Submitting request [{}] to get ticket item [{}]", request, ticketId);

            final Map<String, AttributeValue> returnItem = amazonDynamoDBClient.getItem(request).getItem();
            if (returnItem != null) {
                final Ticket ticket = deserializeTicket(returnItem);
                LOGGER.debug("Located ticket [{}]", ticket);
                return ticket;
            }
        } else {
            LOGGER.warn("No ticket definition could be found in the catalog to match [{}]", ticketId);
        }
        return null;
    }

    private static Ticket deserializeTicket(final Map<String, AttributeValue> returnItem) {
        final ByteBuffer bb = returnItem.get(ColumnNames.ENCODED.getName()).getB();
        LOGGER.debug("Located binary encoding of ticket item [{}]. Transforming item into ticket object", returnItem);
        return SerializationUtils.deserialize(bb.array());
    }

    /**
     * Put ticket.
     *
     * @param ticket        the ticket
     * @param encodedTicket the encoded ticket
     */
    public void put(final Ticket ticket, final Ticket encodedTicket) {
        final TicketDefinition metadata = this.ticketCatalog.find(ticket);
        final Map<String, AttributeValue> values = buildTableAttributeValuesMapFromTicket(ticket, encodedTicket);
        LOGGER.debug("Adding ticket id [{}] with attribute values [{}]", encodedTicket.getId(), values);
        final PutItemRequest putItemRequest = new PutItemRequest(metadata.getProperties().getStorageName(), values);
        LOGGER.debug("Submitting put request [{}] for ticket id [{}]", putItemRequest, encodedTicket.getId());
        final PutItemResult putItemResult = amazonDynamoDBClient.putItem(putItemRequest);
        LOGGER.debug("Ticket added with result [{}]", putItemResult);
        getAll();
    }

    /**
     * Create ticket tables.
     *
     * @param deleteTables the delete tables
     */
    public void createTicketTables(final boolean deleteTables) {
        final Collection<TicketDefinition> metadata = this.ticketCatalog.findAll();
        metadata.forEach(Unchecked.consumer(r -> {
            final CreateTableRequest request = new CreateTableRequest()
                    .withAttributeDefinitions(new AttributeDefinition(ColumnNames.ID.getName(), ScalarAttributeType.S))
                    .withKeySchema(new KeySchemaElement(ColumnNames.ID.getName(), KeyType.HASH))
                    .withProvisionedThroughput(new ProvisionedThroughput(dynamoDbProperties.getReadCapacity(),
                            dynamoDbProperties.getWriteCapacity()))
                    .withTableName(r.getProperties().getStorageName());


            if (deleteTables) {
                final DeleteTableRequest delete = new DeleteTableRequest(r.getProperties().getStorageName());
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
        }));
    }

    /**
     * Build table attribute values from ticket map.
     *
     * @param ticket    the ticket
     * @param encTicket the encoded ticket
     * @return the map
     */
    public Map<String, AttributeValue> buildTableAttributeValuesMapFromTicket(final Ticket ticket, final Ticket encTicket) {
        final Map<String, AttributeValue> values = new HashMap<>();
        values.put(ColumnNames.ID.getName(), new AttributeValue(encTicket.getId()));
        values.put(ColumnNames.PREFIX.getName(), new AttributeValue(encTicket.getPrefix()));
        values.put(ColumnNames.CREATION_TIME.getName(), new AttributeValue(ticket.getCreationTime().toString()));
        values.put(ColumnNames.COUNT_OF_USES.getName(), new AttributeValue().withN(Integer.toString(ticket.getCountOfUses())));
        values.put(ColumnNames.TIME_TO_LIVE.getName(), new AttributeValue().withN(Long.toString(ticket.getExpirationPolicy().getTimeToLive())));
        values.put(ColumnNames.TIME_TO_IDLE.getName(), new AttributeValue().withN(Long.toString(ticket.getExpirationPolicy().getTimeToIdle())));
        values.put(ColumnNames.ENCODED.getName(), new AttributeValue().withB(ByteBuffer.wrap(SerializationUtils.serialize(encTicket))));

        LOGGER.debug("Created attribute values [{}] based on provided ticket [{}]", values, encTicket.getId());
        return values;
    }


}
