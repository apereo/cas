package org.apereo.cas.ticket.registry;

import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbTicketRegistryProperties;
import org.apereo.cas.dynamodb.DynamoDbQueryBuilder;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;

import com.google.common.collect.Streams;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.jooq.lambda.Unchecked;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ComparisonOperator;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

import java.nio.ByteBuffer;
import java.time.chrono.ChronoZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * This is {@link DynamoDbTicketRegistryFacilitator}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class DynamoDbTicketRegistryFacilitator {

    private static final int BATCH_PUT_REQUEST_LIMIT = 25;

    private final TicketCatalog ticketCatalog;

    private final DynamoDbTicketRegistryProperties dynamoDbProperties;

    private final DynamoDbClient amazonDynamoDBClient;

    private static Ticket deserializeTicket(final Map<String, AttributeValue> returnItem) {
        val encoded = returnItem.get(ColumnNames.ENCODED.getColumnName()).b();
        LOGGER.debug("Located binary encoding of ticket item [{}]. Transforming item into ticket object", returnItem);
        try (val is = encoded.asInputStream()) {
            return SerializationUtils.deserialize(is);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    /**
     * Delete.
     *
     * @param ticketId        the ticket id
     * @param encodedTicketId the encoded ticket id
     * @return true/false
     */
    public boolean delete(final String ticketId, final String encodedTicketId) {
        val metadata = this.ticketCatalog.find(ticketId);
        if (metadata != null) {
            val del = DeleteItemRequest.builder().tableName(metadata.getProperties().getStorageName())
                .key(CollectionUtils.wrap(ColumnNames.ID.getColumnName(), AttributeValue.builder().s(encodedTicketId).build())).build();
            LOGGER.debug("Submitting delete request [{}] for ticket [{}]", del, ticketId);
            val res = amazonDynamoDBClient.deleteItem(del);
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
        val count = new AtomicInteger();
        val metadata = this.ticketCatalog.findAll();
        metadata.forEach(r -> {
            val scan = ScanRequest.builder().tableName(r.getProperties().getStorageName()).build();
            LOGGER.debug("Submitting scan request [{}] to table [{}]", scan, r.getProperties().getStorageName());
            count.addAndGet(this.amazonDynamoDBClient.scan(scan).count());
        });
        createTicketTables(true);
        return count.get();
    }

    /**
     * Scan and paginate.
     *
     * @return the stream
     */
    public Stream<Ticket> stream() {
        val metadata = ticketCatalog.findAll();
        val resultStreams = metadata
            .stream()
            .map(defn -> {
                val keys = List.<DynamoDbQueryBuilder>of(
                    DynamoDbQueryBuilder.builder()
                        .key(ColumnNames.PREFIX.getColumnName())
                        .attributeValue(List.of(AttributeValue.builder().s(defn.getPrefix()).build()))
                        .operator(ComparisonOperator.EQ)
                        .build());
                return DynamoDbTableUtils.scanPaginator(amazonDynamoDBClient, defn.getProperties().getStorageName(),
                    keys, DynamoDbTicketRegistryFacilitator::deserializeTicket);
            })
            .toList();
        return Streams.concat(resultStreams.toArray(new Stream[]{}));
    }

    /**
     * Gets all.
     *
     * @return the all
     */
    public Collection<Ticket> getAll() {
        val metadata = this.ticketCatalog.findAll();
        val tickets = new ArrayList<Ticket>(metadata.size());
        metadata.forEach(r -> {
            val scan = ScanRequest.builder().tableName(r.getProperties().getStorageName()).build();
            LOGGER.debug("Scanning table with request [{}]", scan);
            val result = this.amazonDynamoDBClient.scan(scan);
            LOGGER.debug("Scanned table with result [{}]", scan);
            tickets.addAll(result.items()
                .stream()
                .map(DynamoDbTicketRegistryFacilitator::deserializeTicket)
                .filter(Objects::nonNull)
                .toList());
        });
        return tickets;
    }

    /**
     * Get ticket.
     *
     * @param ticketId        the ticket id
     * @param encodedTicketId the encoded ticket id
     * @return the ticket
     */
    public Ticket get(final String ticketId, final String encodedTicketId) {
        val metadata = this.ticketCatalog.find(ticketId);
        if (metadata != null) {
            val keys = new HashMap<String, AttributeValue>();
            keys.put(ColumnNames.ID.getColumnName(), AttributeValue.builder().s(encodedTicketId).build());
            val request = GetItemRequest.builder().key(keys).tableName(metadata.getProperties().getStorageName()).build();
            LOGGER.debug("Submitting request [{}] to get ticket item [{}]", request, ticketId);
            val returnItem = amazonDynamoDBClient.getItem(request).item();
            if (returnItem != null && !returnItem.isEmpty()) {
                val ticket = deserializeTicket(returnItem);
                LOGGER.debug("Located ticket [{}]", ticket);
                if (ticket == null || ticket.isExpired()) {
                    LOGGER.warn("The expiration policy for ticket id [{}] has expired the ticket", ticketId);
                    return null;
                }
                return ticket;
            }
        } else {
            LOGGER.warn("No ticket definition could be found in the catalog to match [{}]", ticketId);
        }
        return null;
    }

    /**
     * Put.
     *
     * @param toSave the to save
     */
    public void put(final Stream<Triple<Ticket, Ticket, String>> toSave) {
        val queue = new HashMap<String, Collection<WriteRequest>>();
        val count = new AtomicLong(0);
        toSave.forEach(entry -> {
            val encodedTicket = entry.getMiddle();
            val ticket = entry.getLeft();
            val principal = entry.getRight();
            val metadata = ticketCatalog.find(ticket);
            val entries = queue.getOrDefault(metadata.getProperties().getStorageName(), new ArrayList<>());
            entries.add(WriteRequest.builder().putRequest(buildPutRequest(ticket, encodedTicket, principal)).build());
            count.getAndIncrement();

            queue.put(metadata.getProperties().getStorageName(), entries);
            if (count.get() >= BATCH_PUT_REQUEST_LIMIT) {
                val batchRequest = BatchWriteItemRequest.builder().requestItems(queue).build();
                amazonDynamoDBClient.batchWriteItem(batchRequest);
                queue.clear();
                count.set(0);
            }
        });
        if (!queue.isEmpty()) {
            val batchRequest = BatchWriteItemRequest.builder().requestItems(queue).build();
            amazonDynamoDBClient.batchWriteItem(batchRequest);
        }
    }

    /**
     * Put ticket.
     *
     * @param ticket        the ticket
     * @param encodedTicket the encoded ticket
     * @param principal     the principal
     */
    public void put(final Ticket ticket, final Ticket encodedTicket,
                    final String principal) {
        val putItemRequest = buildPutItemRequest(ticket, encodedTicket, principal);
        LOGGER.debug("Submitting put request [{}] for ticket id [{}]", putItemRequest, encodedTicket.getId());
        val putItemResult = amazonDynamoDBClient.putItem(putItemRequest);
        LOGGER.debug("Ticket added with result [{}]", putItemResult);
    }

    private PutRequest buildPutRequest(final Ticket ticket, final Ticket encodedTicket, final String principal) {
        val values = buildTableAttributeValuesMapFromTicket(ticket, encodedTicket, principal);
        LOGGER.debug("Adding ticket id [{}] with attribute values [{}]", encodedTicket.getId(), values);
        return PutRequest.builder().item(values).build();
    }

    private PutItemRequest buildPutItemRequest(final Ticket ticket, final Ticket encodedTicket, final String principal) {
        val metadata = this.ticketCatalog.find(ticket);
        val values = buildTableAttributeValuesMapFromTicket(ticket, encodedTicket, principal);
        LOGGER.debug("Adding ticket id [{}] with attribute values [{}]", encodedTicket.getId(), values);
        return PutItemRequest.builder().tableName(metadata.getProperties().getStorageName()).item(values).build();
    }

    /**
     * Create ticket tables.
     *
     * @param deleteTables the delete tables
     */
    public void createTicketTables(final boolean deleteTables) {
        val metadata = this.ticketCatalog.findAll();
        metadata.forEach(Unchecked.consumer(r -> {
            val attributeDefns = List.of(
                AttributeDefinition.builder()
                    .attributeName(ColumnNames.ID.getColumnName())
                    .attributeType(ScalarAttributeType.S)
                    .build());
            val keySchemaElements = List.of(KeySchemaElement.builder()
                .attributeName(ColumnNames.ID.getColumnName())
                .keyType(KeyType.HASH)
                .build());
            val tableDesc = DynamoDbTableUtils.createTable(amazonDynamoDBClient, dynamoDbProperties,
                r.getProperties().getStorageName(),
                deleteTables,
                attributeDefns,
                keySchemaElements);
            DynamoDbTableUtils.enableTimeToLiveOnTable(amazonDynamoDBClient,
                tableDesc.tableName(), ColumnNames.EXPIRATION.getColumnName());
        }));
    }

    /**
     * Build table attribute values from ticket map.
     *
     * @param ticket    the ticket
     * @param encTicket the encoded ticket
     * @param principal the principal
     * @return the map
     */
    public Map<String, AttributeValue> buildTableAttributeValuesMapFromTicket(
        final Ticket ticket, final Ticket encTicket, final String principal) {
        val values = new HashMap<String, AttributeValue>();
        val ttl = Optional.ofNullable(ticket.getExpirationPolicy().getMaximumExpirationTime(ticket))
            .or(() -> Optional.ofNullable(NeverExpiresExpirationPolicy.INSTANCE.getMaximumExpirationTime(encTicket)))
            .map(ChronoZonedDateTime::toEpochSecond)
            .orElseGet(() -> -1L);
        values.put(ColumnNames.EXPIRATION.getColumnName(),
            AttributeValue.builder().n(String.valueOf(ttl)).build());
        values.put(ColumnNames.ID.getColumnName(),
            AttributeValue.builder().s(encTicket.getId()).build());
        values.put(ColumnNames.PRINCIPAL.getColumnName(),
            AttributeValue.builder().s(principal).build());
        values.put(ColumnNames.PREFIX.getColumnName(),
            AttributeValue.builder().s(ticket.getPrefix()).build());
        values.put(ColumnNames.CREATION_TIME.getColumnName(), AttributeValue.builder().
            s(ticket.getCreationTime().toString()).build());
        values.put(ColumnNames.COUNT_OF_USES.getColumnName(),
            AttributeValue.builder().n(Integer.toString(ticket.getCountOfUses())).build());
        values.put(ColumnNames.TIME_TO_LIVE.getColumnName(),
            AttributeValue.builder().n(Long.toString(ticket.getExpirationPolicy().getTimeToLive())).build());
        values.put(ColumnNames.TIME_TO_IDLE.getColumnName(),
            AttributeValue.builder().n(Long.toString(ticket.getExpirationPolicy().getTimeToIdle())).build());
        values.put(ColumnNames.ENCODED.getColumnName(),
            AttributeValue.builder().b(SdkBytes.fromByteBuffer(ByteBuffer.wrap(SerializationUtils.serialize(encTicket)))).build());
        LOGGER.debug("Created attribute values [{}] based on provided ticket [{}]", values, encTicket.getId());
        return values;
    }

    /**
     * Gets sessions for.
     *
     * @param principal the principal
     * @return the sessions for
     */
    public Stream<? extends Ticket> getSessionsFor(final String principal) {
        val keys = List.<DynamoDbQueryBuilder>of(
            DynamoDbQueryBuilder.builder()
                .key(ColumnNames.PRINCIPAL.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().s(principal).build()))
                .operator(ComparisonOperator.EQ)
                .build());
        return DynamoDbTableUtils.getRecordsByKeys(amazonDynamoDBClient,
            dynamoDbProperties.getTicketGrantingTicketsTableName(),
            keys,
            DynamoDbTicketRegistryFacilitator::deserializeTicket);
    }

    /**
     * Count tickets and return value.
     *
     * @param ticketType the ticket type
     * @param prefix     the prefix
     * @return the long
     */
    public long countTickets(final Class<? extends Ticket> ticketType, final String prefix) {
        val keys = List.<DynamoDbQueryBuilder>of(
            DynamoDbQueryBuilder.builder()
                .key(ColumnNames.PREFIX.getColumnName())
                .attributeValue(List.of(AttributeValue.builder().s(prefix).build()))
                .operator(ComparisonOperator.EQ)
                .build());
        return ticketCatalog.findTicketDefinition(ticketType)
            .map(def -> DynamoDbTableUtils.scan(amazonDynamoDBClient, def.getProperties().getStorageName(), keys).count())
            .orElse(-1);
    }


    /**
     * Column names for tables holding tickets.
     */
    @Getter
    @RequiredArgsConstructor
    public enum ColumnNames {

        /**
         * id column.
         */
        ID("id"),
        /**
         * prefix column.
         */
        PRINCIPAL("principal"),
        /**
         * prefix column.
         */
        PREFIX("prefix"),
        /**
         * creationTime column.
         */
        CREATION_TIME("creationTime"),
        /**
         * countOfUses column.
         */
        COUNT_OF_USES("countOfUses"),
        /**
         * timeToLive column.
         */
        TIME_TO_LIVE("timeToLive"),
        /**
         * timeToIdle column.
         */
        TIME_TO_IDLE("timeToIdle"),
        /**
         * expiration column.
         */
        EXPIRATION("expiration"),
        /**
         * encoded column.
         */
        ENCODED("encoded");

        private final String columnName;
    }
}
