package org.apereo.cas.ticket.registry;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteTableRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import com.amazonaws.services.dynamodbv2.util.TableUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbProperties;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
    private final DynamoDbProperties dynamoDbProperties;
    private final AmazonDynamoDBClient amazonDynamoDBClient;

    public DynamoDbTicketRegistryFacilitator(final TicketCatalog ticketCatalog,
                                             final DynamoDbProperties dynamoDbProperties,
                                             final AmazonDynamoDBClient amazonDynamoDBClient) {
        this.ticketCatalog = ticketCatalog;
        this.dynamoDbProperties = dynamoDbProperties;
        this.amazonDynamoDBClient = amazonDynamoDBClient;
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

            final Map<String, AttributeValue> returnItem = amazonDynamoDBClient.getItem(request).getItem();
            if (returnItem != null) {
                final ByteBuffer bb = returnItem.get(ColumnNames.ENCODED.getName()).getB();
                final Ticket ticket = SerializationUtils.deserialize(bb.array());
                LOGGER.debug("Located ticket [{}]", ticket);
            }
        } else {
            LOGGER.warn("No ticket definition could be found in the catalog to match [{}]", ticketId);
        }
        return null;
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
        amazonDynamoDBClient.putItem(metadata.getProperties().getStorageName(), values);
    }

    /**
     * Create ticket tables.
     */
    public void createTicketTables() {
        final Collection<TicketDefinition> metadata = this.ticketCatalog.findAll();
        metadata.forEach(r -> {
            final CreateTableRequest request = new CreateTableRequest()
                    .withAttributeDefinitions(new AttributeDefinition(ColumnNames.ID.getName(), ScalarAttributeType.S))
                    .withKeySchema(new KeySchemaElement(ColumnNames.ID.getName(), KeyType.HASH))
                    .withProvisionedThroughput(new ProvisionedThroughput(dynamoDbProperties.getReadCapacity(),
                            dynamoDbProperties.getWriteCapacity()))
                    .withTableName(r.getProperties().getStorageName());

            if (dynamoDbProperties.isDropTablesOnStartup()) {
                final DeleteTableRequest delete = new DeleteTableRequest(r.getProperties().getStorageName());
                TableUtils.deleteTableIfExists(amazonDynamoDBClient, delete);
            }
            TableUtils.createTableIfNotExists(amazonDynamoDBClient, request);
        });
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

        final AttributeValue vCount = new AttributeValue();
        vCount.setN(String.valueOf(ticket.getCountOfUses()));
        values.put(ColumnNames.COUNT_OF_USES.getName(), vCount);

        final AttributeValue vLive = new AttributeValue();
        vLive.setN(String.valueOf(ticket.getExpirationPolicy().getTimeToLive()));
        values.put(ColumnNames.TIME_TO_LIVE.getName(), vLive);

        final AttributeValue vIdle = new AttributeValue();
        vIdle.setN(String.valueOf(ticket.getExpirationPolicy().getTimeToIdle()));
        values.put(ColumnNames.TIME_TO_IDLE.getName(), vIdle);

        final AttributeValue vBin = new AttributeValue();
        vBin.setB(ByteBuffer.wrap(SerializationUtils.serialize(encTicket)));
        values.put(ColumnNames.ENCODED.getName(), vBin);
        return values;
    }
}
