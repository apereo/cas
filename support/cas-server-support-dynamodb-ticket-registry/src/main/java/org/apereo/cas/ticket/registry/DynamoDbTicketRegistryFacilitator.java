package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.model.support.dynamodb.AbstractDynamoDbProperties;
import org.apereo.cas.configuration.model.support.dynamodb.DynamoDbTicketRegistryProperties;
import org.apereo.cas.dynamodb.DynamoDbTableUtils;
import org.apereo.cas.ticket.IdleExpirationPolicy;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import com.google.common.collect.Streams;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.Select;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;

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

    private static final int BATCH_WRITE_MAX_ATTEMPTS = 10;

    private static final long BATCH_WRITE_RETRY_DELAY_MILLIS = 50;

    private static final long BATCH_WRITE_MAX_RETRY_DELAY_MILLIS = 1_000;

    private static final long DEFAULT_QUERY_PAGE_SIZE = 250;

    private static final String PRINCIPAL_INDEX_NAME = "principalExpirationIndex";

    private static final String SERVICE_INDEX_NAME = "serviceExpirationIndex";

    private static final String PREFIX_INDEX_NAME = "prefixExpirationIndex";

    private static final String EXPRESSION_NAME_KEY = "#key";

    private static final String EXPRESSION_NAME_ID = "#id";

    private static final String EXPRESSION_NAME_EXPIRATION = "#expiration";

    private static final String EXPRESSION_VALUE_KEY = ":key";

    private static final String EXPRESSION_VALUE_NOW = ":now";

    private final TicketCatalog ticketCatalog;

    private final DynamoDbTicketRegistryProperties dynamoDbProperties;

    private final DynamoDbClient amazonDynamoDBClient;

    /**
     * Delete tickets for principal.
     *
     * @param principalId the principal id
     * @return the long
     */
    public long deleteTicketsFor(final String principalId) {
        val metadata = ticketCatalog.findAll();
        return metadata
            .stream()
            .mapToLong(definition -> {
                val tableName = definition.getProperties().getStorageName();
                val deletedCount = deleteTickets(tableName,
                    queryTicketIdsByIndex(tableName, PRINCIPAL_INDEX_NAME, ColumnNames.PRINCIPAL, principalId, false));
                LOGGER.debug("Deleted [{}] tickets for principal id [{}] from table [{}]", deletedCount, principalId,
                    tableName);
                return deletedCount;
            })
            .sum();
    }

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
        val deletor = new Supplier<Integer>() {
            @Override
            public Integer get() {
                val count = new AtomicInteger();
                val metadata = ticketCatalog.findAll();
                metadata.forEach(definition -> {
                    val tableName = definition.getProperties().getStorageName();
                    count.addAndGet(Math.toIntExact(deleteTickets(tableName, scanTicketIds(tableName))));
                });
                return count.get();
            }
        };
        try {
            return deletor.get();
        } catch (final ResourceNotFoundException e) {
            LOGGER.warn(e.getMessage(), e);
            createTicketTables(false);
            return deletor.get();
        }
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
            .map(defn -> queryTicketsByIndex(defn.getProperties().getStorageName(),
                PREFIX_INDEX_NAME, ColumnNames.PREFIX, defn.getPrefix(), 0, true))
            .toList();
        return Streams.concat(resultStreams.toArray(new Stream[0]));
    }

    /**
     * Query the storage name.
     *
     * @param criteria the criteria
     * @return the stream
     */
    public Stream<Ticket> query(final TicketRegistryQueryCriteria criteria) {
        val definitions = StringUtils.isNotBlank(criteria.getType())
            ? Optional.ofNullable(ticketCatalog.find(criteria.getType()))
            .map(List::of)
            .orElseGet(List::of)
            : ticketCatalog.findAll();
        val results = definitions
            .stream()
            .flatMap(definition -> {
                val tableName = definition.getProperties().getStorageName();
                if (StringUtils.isNotBlank(criteria.getPrincipal())) {
                    return queryTicketsByIndex(tableName, PRINCIPAL_INDEX_NAME, ColumnNames.PRINCIPAL,
                        criteria.getPrincipal(), criteria.getCount(), true);
                }
                return queryTicketsByIndex(tableName, PREFIX_INDEX_NAME, ColumnNames.PREFIX,
                    definition.getPrefix(), criteria.getCount(), true);
            });
        return criteria.getCount() > 0 ? results.limit(criteria.getCount()) : results;
    }

    /**
     * Gets all.
     *
     * @return the all
     */
    public Collection<Ticket> getAll() {
        val metadata = ticketCatalog.findAll();
        val tickets = new ArrayList<Ticket>(metadata.size());
        metadata.forEach(r -> {
            val scan = ScanRequest.builder()
                .tableName(r.getProperties().getStorageName())
                .build();
            LOGGER.debug("Scanning table with request [{}]", scan);
            tickets.addAll(this.amazonDynamoDBClient.scanPaginator(scan)
                .stream()
                .flatMap(result -> result.items().stream())
                .map(DynamoDbTicketRegistryFacilitator::deserializeTicket)
                .filter(Objects::nonNull)
                .filter(ticket -> !ticket.isExpired())
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
    public void put(final Stream<TicketPayload> toSave) {
        val queue = new HashMap<String, List<WriteRequest>>();
        val count = new AtomicLong(0);
        toSave.forEach(entry -> {
            val metadata = ticketCatalog.find(entry.getOriginalTicket());
            val entries = queue.computeIfAbsent(metadata.getProperties().getStorageName(), __ -> new ArrayList<>());
            entries.add(WriteRequest.builder().putRequest(buildPutRequest(entry)).build());
            count.getAndIncrement();
            if (count.get() >= BATCH_PUT_REQUEST_LIMIT) {
                submitBatchWriteRequest(queue);
                queue.clear();
                count.set(0);
            }
        });
        if (!queue.isEmpty()) {
            submitBatchWriteRequest(queue);
        }
    }

    /**
     * Put ticket.
     *
     * @param payload the payload
     */
    public void put(final TicketPayload payload) {
        val putItemRequest = buildPutItemRequest(payload);
        LOGGER.debug("Submitting put request [{}] for ticket id [{}]", putItemRequest, payload.getEncodedTicket().getId());
        val putItemResult = amazonDynamoDBClient.putItem(putItemRequest);
        LOGGER.debug("Ticket added with result [{}]", putItemResult);
    }

    /**
     * Gets sessions with attributes.
     *
     * @param filterExpression the filter expression
     * @param attributeNames   the attribute names
     * @param attributeValues  the attribute values
     * @return the sessions with attributes
     */
    public Stream<? extends Ticket> getSessionsWithAttributes(final String filterExpression,
                                                              final Map<String, String> attributeNames,
                                                              final Map<String, AttributeValue> attributeValues) {
        return getEntitiesWithAttributes(dynamoDbProperties.getTicketGrantingTicketsTableName(),
            filterExpression, attributeNames, attributeValues);
    }

    /**
     * Gets entities with attributes.
     *
     * @param tableName        the table name
     * @param filterExpression the filter expression
     * @param attributeNames   the attribute names
     * @param attributeValues  the attribute values
     * @return the entities with attributes
     */
    public Stream<Ticket> getEntitiesWithAttributes(final String tableName,
                                                    final String filterExpression,
                                                    final Map<String, String> attributeNames,
                                                    final Map<String, AttributeValue> attributeValues) {
        val expressionAttributeNames = new HashMap<>(attributeNames);
        expressionAttributeNames.put(EXPRESSION_NAME_KEY, ColumnNames.PREFIX.getColumnName());
        expressionAttributeNames.put(EXPRESSION_NAME_EXPIRATION, ColumnNames.EXPIRATION.getColumnName());
        val expressionAttributeValues = new HashMap<>(attributeValues);
        expressionAttributeValues.put(EXPRESSION_VALUE_NOW, currentEpochAttributeValue());
        return DynamoDbTableUtils.queryPaginator(amazonDynamoDBClient,
                tableName,
                PREFIX_INDEX_NAME,
                keyConditionExpression(),
                filterExpression,
                expressionAttributeNames,
                expressionAttributeValues,
                DEFAULT_QUERY_PAGE_SIZE,
                DynamoDbTicketRegistryFacilitator::deserializeTicket)
            .filter(ticket -> !ticket.isExpired());
    }

    private PutRequest buildPutRequest(final TicketPayload payload) {
        val values = buildTableAttributeValuesMapFromTicket(payload);
        LOGGER.debug("Adding ticket id [{}] with attribute values [{}]", payload.getEncodedTicket().getId(), values);
        return PutRequest.builder().item(values).build();
    }

    private PutItemRequest buildPutItemRequest(final TicketPayload payload) {
        val metadata = this.ticketCatalog.find(payload.getOriginalTicket());
        val values = buildTableAttributeValuesMapFromTicket(payload);
        LOGGER.debug("Building a put request for ticket id [{}] with attribute values [{}]", payload.getEncodedTicket().getId(), values);
        return PutItemRequest.builder().tableName(metadata.getProperties().getStorageName()).item(values).build();
    }

    /**
     * Create ticket tables.
     *
     * @param deleteTables the delete tables
     */
    public void createTicketTables(final boolean deleteTables) {
        val metadata = this.ticketCatalog.findAll();
        metadata.forEach(Unchecked.consumer(defn -> {
            val attributeDefinitions = List.of(
                AttributeDefinition.builder()
                    .attributeName(ColumnNames.ID.getColumnName())
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName(ColumnNames.PRINCIPAL.getColumnName())
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName(ColumnNames.SERVICE.getColumnName())
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName(ColumnNames.PREFIX.getColumnName())
                    .attributeType(ScalarAttributeType.S)
                    .build(),
                AttributeDefinition.builder()
                    .attributeName(ColumnNames.EXPIRATION.getColumnName())
                    .attributeType(ScalarAttributeType.N)
                    .build());
            val keySchemaElements = List.of(KeySchemaElement.builder()
                .attributeName(ColumnNames.ID.getColumnName())
                .keyType(KeyType.HASH)
                .build());
            val tableDesc = DynamoDbTableUtils.createTable(amazonDynamoDBClient,
                dynamoDbProperties,
                defn.getProperties().getStorageName(),
                deleteTables,
                attributeDefinitions,
                keySchemaElements,
                buildGlobalSecondaryIndexes());
            DynamoDbTableUtils.enableTimeToLiveOnTable(amazonDynamoDBClient,
                tableDesc.tableName(), ColumnNames.EXPIRATION.getColumnName());
        }));
    }

    /**
     * Build table attribute values from ticket map.
     *
     * @param payload the payload
     * @return the map
     */
    public Map<String, AttributeValue> buildTableAttributeValuesMapFromTicket(
        final TicketPayload payload) {
        val values = new HashMap<String, AttributeValue>();
        val ttl = Optional.ofNullable(payload.getOriginalTicket().getExpirationPolicy().toMaximumExpirationTime(payload.getOriginalTicket()))
            .or(() -> Optional.ofNullable(NeverExpiresExpirationPolicy.INSTANCE.toMaximumExpirationTime(payload.getEncodedTicket())))
            .map(ChronoZonedDateTime::toEpochSecond)
            .orElse(-1L);

        values.put(ColumnNames.ATTRIBUTES.getColumnName(),
            AttributeValue.builder().m(convertAttributes(payload)).build());
        values.put(ColumnNames.EXPIRATION.getColumnName(),
            AttributeValue.builder().n(String.valueOf(ttl)).build());
        values.put(ColumnNames.ID.getColumnName(),
            AttributeValue.builder().s(payload.getEncodedTicket().getId()).build());
        if (StringUtils.isNotBlank(payload.getPrincipal())) {
            values.put(ColumnNames.PRINCIPAL.getColumnName(),
                AttributeValue.builder().s(payload.getPrincipal()).build());
        }
        if (StringUtils.isNotBlank(payload.getService())) {
            values.put(ColumnNames.SERVICE.getColumnName(),
                AttributeValue.builder().s(payload.getService()).build());
        }
        values.put(ColumnNames.PREFIX.getColumnName(),
            AttributeValue.builder().s(payload.getOriginalTicket().getPrefix()).build());
        values.put(ColumnNames.CREATION_TIME.getColumnName(), AttributeValue.builder().
            s(payload.getOriginalTicket().getCreationTime().toString()).build());
        values.put(ColumnNames.COUNT_OF_USES.getColumnName(),
            AttributeValue.builder().n(Integer.toString(payload.getOriginalTicket().getCountOfUses())).build());
        values.put(ColumnNames.TIME_TO_LIVE.getColumnName(),
            AttributeValue.builder().n(Long.toString(payload.getOriginalTicket().getExpirationPolicy().getTimeToLive())).build());

        val idleTimeout = payload.getOriginalTicket().getExpirationPolicy() instanceof final IdleExpirationPolicy iep ? Long.toString(iep.getTimeToIdle()) : "0";
        values.put(ColumnNames.TIME_TO_IDLE.getColumnName(), AttributeValue.builder().n(idleTimeout).build());

        values.put(ColumnNames.ENCODED.getColumnName(),
            AttributeValue.builder().b(SdkBytes.fromByteBuffer(ByteBuffer.wrap(SerializationUtils.serialize(payload.getEncodedTicket())))).build());
        LOGGER.debug("Created attribute values [{}] based on provided ticket [{}]", values, payload.getEncodedTicket().getId());
        return values;
    }

    private static Map<String, AttributeValue> convertAttributes(final TicketPayload payload) {
        val attributes = new HashMap<String, AttributeValue>();
        payload.getAttributes()
            .entrySet()
            .stream()
            .filter(entry -> !entry.getValue().isEmpty())
            .forEach(entry -> {
                val allValues = entry.getValue().stream().map(Object::toString).collect(Collectors.toList());
                if (!allValues.isEmpty()) {
                    val attributeValues = AttributeValue.builder().ss(allValues).build();
                    attributes.put(entry.getKey(), attributeValues);
                }
            });
        return attributes;
    }

    /**
     * Gets sessions for.
     *
     * @param principal the principal
     * @return the sessions for
     */
    public Stream<? extends Ticket> getSessionsFor(final String principal) {
        return queryTicketsByIndex(dynamoDbProperties.getTicketGrantingTicketsTableName(),
                PRINCIPAL_INDEX_NAME, ColumnNames.PRINCIPAL, principal, 0, true)
            .filter(ticket -> !ticket.isExpired());
    }

    /**
     * Gets tickets for service.
     *
     * @param service the service
     * @return the tickets for service
     */
    public Stream<? extends Ticket> getTicketsFor(final Service service) {
        return ticketCatalog.findAll()
            .stream()
            .flatMap(definition -> queryTicketsByIndex(definition.getProperties().getStorageName(),
                SERVICE_INDEX_NAME, ColumnNames.SERVICE, service.getId(), 0, true))
            .filter(ticket -> !ticket.isExpired());
    }

    /**
     * Count tickets for.
     *
     * @param tableName the table name
     * @param service   the service
     * @return the long
     */
    public long countTicketsFor(final String tableName, final Service service) {
        return countByIndex(tableName, SERVICE_INDEX_NAME, ColumnNames.SERVICE, service.getId());
    }

    /**
     * Count tickets and return value.
     *
     * @param ticketType the ticket type
     * @param prefix     the prefix
     * @return the long
     */
    public long countTickets(final Class<? extends Ticket> ticketType, final String prefix) {
        return ticketCatalog.findTicketDefinition(ticketType)
            .map(def -> countByIndex(def.getProperties().getStorageName(), PREFIX_INDEX_NAME, ColumnNames.PREFIX, prefix))
            .orElse(-1L);
    }

    private List<GlobalSecondaryIndex> buildGlobalSecondaryIndexes() {
        return List.of(
            buildGlobalSecondaryIndex(PRINCIPAL_INDEX_NAME, ColumnNames.PRINCIPAL),
            buildGlobalSecondaryIndex(SERVICE_INDEX_NAME, ColumnNames.SERVICE),
            buildGlobalSecondaryIndex(PREFIX_INDEX_NAME, ColumnNames.PREFIX));
    }

    private GlobalSecondaryIndex buildGlobalSecondaryIndex(final String indexName, final ColumnNames hashKey) {
        val indexBuilder = GlobalSecondaryIndex.builder()
            .indexName(indexName)
            .keySchema(List.of(
                KeySchemaElement.builder()
                    .attributeName(hashKey.getColumnName())
                    .keyType(KeyType.HASH)
                    .build(),
                KeySchemaElement.builder()
                    .attributeName(ColumnNames.EXPIRATION.getColumnName())
                    .keyType(KeyType.RANGE)
                    .build()))
            .projection(Projection.builder().projectionType(ProjectionType.ALL).build());
        Optional.ofNullable(getIndexProvisionedThroughput()).ifPresent(indexBuilder::provisionedThroughput);
        return indexBuilder.build();
    }

    private ProvisionedThroughput getIndexProvisionedThroughput() {
        return dynamoDbProperties.getBillingMode() == AbstractDynamoDbProperties.BillingMode.PROVISIONED
            ? ProvisionedThroughput.builder()
            .readCapacityUnits(dynamoDbProperties.getReadCapacity())
            .writeCapacityUnits(dynamoDbProperties.getWriteCapacity())
            .build()
            : null;
    }

    private Stream<Ticket> queryTicketsByIndex(final String tableName,
                                               final String indexName,
                                               final ColumnNames partitionKey,
                                               final String partitionValue,
                                               final long limit,
                                               final boolean excludeExpired) {
        if (StringUtils.isBlank(partitionValue)) {
            return Stream.empty();
        }
        return DynamoDbTableUtils.queryPaginator(amazonDynamoDBClient,
            tableName,
            indexName,
            keyConditionExpression(excludeExpired),
            StringUtils.EMPTY,
            expressionAttributeNames(partitionKey),
            expressionAttributeValues(partitionValue, excludeExpired),
            limit > 0 ? limit : DEFAULT_QUERY_PAGE_SIZE,
            DynamoDbTicketRegistryFacilitator::deserializeTicket);
    }

    private Stream<String> queryTicketIdsByIndex(final String tableName,
                                                 final String indexName,
                                                 final ColumnNames partitionKey,
                                                 final String partitionValue,
                                                 final boolean excludeExpired) {
        if (StringUtils.isBlank(partitionValue)) {
            return Stream.empty();
        }
        val expressionAttributeNames = new HashMap<String, String>();
        expressionAttributeNames.put(EXPRESSION_NAME_KEY, partitionKey.getColumnName());
        expressionAttributeNames.put(EXPRESSION_NAME_ID, ColumnNames.ID.getColumnName());
        if (excludeExpired) {
            expressionAttributeNames.put(EXPRESSION_NAME_EXPIRATION, ColumnNames.EXPIRATION.getColumnName());
        }
        val request = QueryRequest.builder()
            .tableName(tableName)
            .indexName(indexName)
            .keyConditionExpression(keyConditionExpression(excludeExpired))
            .projectionExpression(EXPRESSION_NAME_ID)
            .expressionAttributeNames(expressionAttributeNames)
            .expressionAttributeValues(expressionAttributeValues(partitionValue, excludeExpired))
            .limit((int) DEFAULT_QUERY_PAGE_SIZE)
            .build();
        LOGGER.debug("Querying ticket ids with request [{}]", request);
        return amazonDynamoDBClient.queryPaginator(request)
            .stream()
            .flatMap(response -> response.items().stream())
            .map(item -> item.get(ColumnNames.ID.getColumnName()))
            .filter(Objects::nonNull)
            .map(AttributeValue::s);
    }

    private Stream<String> scanTicketIds(final String tableName) {
        val request = ScanRequest.builder()
            .tableName(tableName)
            .projectionExpression(EXPRESSION_NAME_ID)
            .expressionAttributeNames(CollectionUtils.wrap(EXPRESSION_NAME_ID, ColumnNames.ID.getColumnName()))
            .limit((int) DEFAULT_QUERY_PAGE_SIZE)
            .build();
        LOGGER.debug("Scanning ticket ids with request [{}]", request);
        return amazonDynamoDBClient.scanPaginator(request)
            .stream()
            .flatMap(response -> response.items().stream())
            .map(item -> item.get(ColumnNames.ID.getColumnName()))
            .filter(Objects::nonNull)
            .map(AttributeValue::s);
    }

    private long countByIndex(final String tableName,
                              final String indexName,
                              final ColumnNames partitionKey,
                              final String partitionValue) {
        if (StringUtils.isBlank(partitionValue)) {
            return 0;
        }
        val request = QueryRequest.builder()
            .tableName(tableName)
            .indexName(indexName)
            .keyConditionExpression(keyConditionExpression(true))
            .expressionAttributeNames(expressionAttributeNames(partitionKey))
            .expressionAttributeValues(expressionAttributeValues(partitionValue, true))
            .select(Select.COUNT)
            .limit((int) DEFAULT_QUERY_PAGE_SIZE)
            .build();
        LOGGER.debug("Counting tickets with query request [{}]", request);
        return amazonDynamoDBClient.queryPaginator(request)
            .stream()
            .mapToLong(QueryResponse::count)
            .sum();
    }

    private static String keyConditionExpression() {
        return keyConditionExpression(true);
    }

    private static String keyConditionExpression(final boolean excludeExpired) {
        return excludeExpired
            ? EXPRESSION_NAME_KEY + " = " + EXPRESSION_VALUE_KEY + " AND " + EXPRESSION_NAME_EXPIRATION + " > " + EXPRESSION_VALUE_NOW
            : EXPRESSION_NAME_KEY + " = " + EXPRESSION_VALUE_KEY;
    }

    private static Map<String, String> expressionAttributeNames(final ColumnNames partitionKey) {
        return CollectionUtils.wrap(
            EXPRESSION_NAME_KEY, partitionKey.getColumnName(),
            EXPRESSION_NAME_EXPIRATION, ColumnNames.EXPIRATION.getColumnName());
    }

    private static Map<String, AttributeValue> expressionAttributeValues(final String partitionValue,
                                                                         final boolean includeExpiration) {
        val values = new HashMap<String, AttributeValue>();
        values.put(EXPRESSION_VALUE_KEY, AttributeValue.builder().s(partitionValue).build());
        if (includeExpiration) {
            values.put(EXPRESSION_VALUE_NOW, currentEpochAttributeValue());
        }
        return values;
    }

    private static AttributeValue currentEpochAttributeValue() {
        return AttributeValue.builder().n(String.valueOf(Instant.now().getEpochSecond())).build();
    }

    private long deleteTickets(final String tableName, final Stream<String> encodedTicketIds) {
        val queue = new ArrayList<WriteRequest>(BATCH_PUT_REQUEST_LIMIT);
        val count = new AtomicLong();
        encodedTicketIds.forEach(encodedTicketId -> {
            queue.add(WriteRequest.builder()
                .deleteRequest(DeleteRequest.builder()
                    .key(CollectionUtils.wrap(ColumnNames.ID.getColumnName(), AttributeValue.builder().s(encodedTicketId).build()))
                    .build())
                .build());
            count.incrementAndGet();
            if (queue.size() == BATCH_PUT_REQUEST_LIMIT) {
                submitBatchWriteRequest(tableName, queue);
                queue.clear();
            }
        });
        submitBatchWriteRequest(tableName, queue);
        return count.get();
    }

    private void submitBatchWriteRequest(final String tableName, final List<WriteRequest> writeRequests) {
        if (!writeRequests.isEmpty()) {
            submitBatchWriteRequest(CollectionUtils.wrap(tableName, new ArrayList<>(writeRequests)));
        }
    }

    private void submitBatchWriteRequest(final Map<String, List<WriteRequest>> requestItems) {
        var unprocessedItems = new HashMap<>(requestItems);
        var attempt = 0;
        while (!unprocessedItems.isEmpty()) {
            val batchRequest = BatchWriteItemRequest.builder().requestItems(unprocessedItems).build();
            val response = amazonDynamoDBClient.batchWriteItem(batchRequest);
            unprocessedItems = new HashMap<>(response.unprocessedItems());
            if (!unprocessedItems.isEmpty()) {
                attempt++;
                if (attempt >= BATCH_WRITE_MAX_ATTEMPTS) {
                    throw new IllegalStateException("DynamoDb batch write failed to process [%s] item collection(s) after [%s] attempts"
                        .formatted(unprocessedItems.size(), attempt));
                }
                val delay = Math.min(BATCH_WRITE_MAX_RETRY_DELAY_MILLIS,
                    BATCH_WRITE_RETRY_DELAY_MILLIS * (1L << Math.min(attempt, 8)));
                LOGGER.warn("DynamoDb batch write returned [{}] unprocessed item collection(s). Retrying attempt [{}] in [{}]ms",
                    unprocessedItems.size(), attempt, delay);
                FunctionUtils.doUnchecked(__ -> Thread.sleep(delay));
            }
        }
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
         * principal column.
         */
        PRINCIPAL("principal"),
        /**
         * service column.
         */
        SERVICE("service"),
        /**
         * attributes column.
         */
        ATTRIBUTES("attributes"),
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

    @SuperBuilder
    @Getter
    public static class TicketPayload {
        private final Ticket originalTicket;

        private final Ticket encodedTicket;

        private final String principal;

        private final String service;

        @Builder.Default
        private final Map<String, List<Object>> attributes = new HashMap<>();
    }
}
