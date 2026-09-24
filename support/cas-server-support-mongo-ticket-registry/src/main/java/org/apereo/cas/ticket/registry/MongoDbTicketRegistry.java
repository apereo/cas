package org.apereo.cas.ticket.registry;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.mongo.MongoDbConnectionFactory;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.mongodb.client.MongoCollection;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * A Ticket Registry storage backend based on MongoDB.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Monitorable
public class MongoDbTicketRegistry extends AbstractTicketRegistry {

    private final MongoOperations mongoTemplate;

    public MongoDbTicketRegistry(final CipherExecutor cipherExecutor,
                                 final TicketSerializationManager ticketSerializationManager,
                                 final TicketCatalog ticketCatalog,
                                 final ConfigurableApplicationContext applicationContext,
                                 final MongoOperations mongoTemplate) {
        super(cipherExecutor, ticketSerializationManager, ticketCatalog, applicationContext);
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Calculate the time at which the ticket is eligible for automated deletion by MongoDb.
     * Makes the assumption that the CAS server date and the Mongo server date are in sync.
     */
    private static Date getExpireAt(final Ticket ticket) {
        val expirationPolicy = ticket.getExpirationPolicy();
        val ttl = expirationPolicy.getTimeToLive(ticket);
        if (ttl < 1 || ttl == Long.MAX_VALUE) {
            LOGGER.trace("Expiration date is undefined for ttl value [{}]", ttl);
            return null;
        }
        val exp = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(ttl);
        return DateTimeUtils.dateOf(Instant.ofEpochMilli(exp));
    }

    @Override
    public Ticket addSingleTicket(final Ticket ticket) throws Exception {
        LOGGER.debug("Adding ticket [{}]", ticket.getId());
        val metadata = findTicketDefinition(ticket);
        LOGGER.trace("Located ticket definition [{}] in the ticket catalog", metadata);
        val document = buildTicketAsDocument(ticket);
        val collectionName = getTicketCollectionInstanceByMetadata(metadata);
        LOGGER.trace("Found collection [{}] linked to ticket [{}]", collectionName, metadata);
        mongoTemplate.insert(document, collectionName);
        LOGGER.debug("Added ticket [{}]", ticket.getId());
        return ticket;
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        LOGGER.debug("Locating ticket [{}]", ticketId);
        val encTicketId = digestIdentifier(ticketId);
        if (StringUtils.isBlank(encTicketId)) {
            LOGGER.debug("Ticket id [{}] could not be found", ticketId);
            return null;
        }
        val metadata = ticketCatalog.find(ticketId);
        if (metadata == null) {
            LOGGER.debug("Ticket definition [{}] could not be found in the ticket catalog", ticketId);
            return null;
        }
        val collectionName = getTicketCollectionInstanceByMetadata(metadata);
        val query = new Query(Criteria.where(MongoDbTicketDocument.FIELD_NAME_ID).is(encTicketId));
        val found = mongoTemplate.findOne(query, MongoDbTicketDocument.class, collectionName);
        if (found == null) {
            LOGGER.debug("Ticket [{}] could not be found in collection [{}]", ticketId, collectionName);
            return null;
        }
        val result = decodeTicketFromDocument(found);
        return result != null && predicate.test(result) ? result : null;
    }

    @Override
    public long deleteAll() {
        val query = new Query(Criteria.where(MongoDbTicketDocument.FIELD_NAME_ID).exists(true));
        return getTicketCollectionNames(ticketCatalog.findAll().stream())
            .stream()
            .mapToLong(collectionName -> mongoTemplate.remove(query, collectionName).getDeletedCount())
            .sum();
    }

    @Override
    public long deleteTicketsFor(final String principalId) {
        val query = buildTicketQuery(buildPrincipalCriteria(principalId));
        return getTicketCollectionNames(ticketCatalog.findAll().stream())
            .stream()
            .mapToLong(collectionName -> mongoTemplate.remove(query, collectionName).getDeletedCount())
            .sum();
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        try (val ticketStream = stream()) {
            return ticketStream
                .filter(ticket -> !ticket.isExpired())
                .collect(Collectors.toSet());
        }
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
        LOGGER.debug("Updating ticket [{}]", ticket);
        val metadata = findTicketDefinition(ticket);
        LOGGER.debug("Located ticket definition [{}] in the ticket catalog", metadata);
        val holder = buildTicketAsDocument(ticket);
        val collectionName = getTicketCollectionInstanceByMetadata(metadata);
        val query = new Query(Criteria.where(MongoDbTicketDocument.FIELD_NAME_ID).is(holder.getTicketId()));
        val update = new Update()
            .set(MongoDbTicketDocument.FIELD_NAME_JSON, holder.getJson())
            .set(MongoDbTicketDocument.FIELD_NAME_TYPE, holder.getType())
            .set(MongoDbTicketDocument.FIELD_NAME_EXPIRE_AT, holder.getExpireAt())
            .set(MongoDbTicketDocument.FIELD_NAME_PRINCIPAL, holder.getPrincipal())
            .set(MongoDbTicketDocument.FIELD_NAME_SERVICE, holder.getService())
            .set(MongoDbTicketDocument.FIELD_NAME_ATTRIBUTES, holder.getAttributes());
        val result = mongoTemplate.updateFirst(query, update, collectionName);
        LOGGER.debug("Updated ticket [{}] with result [{}]", ticket, result);
        return result.getMatchedCount() > 0 ? ticket : null;
    }

    @Override
    public Stream<? extends Ticket> stream(final TicketRegistryStreamCriteria criteria) {
        val maxResults = criteria.isInfiniteCount() ? -1 : criteria.getFrom() + criteria.getCount();
        var ticketStream = streamTicketDocuments(getTicketCollectionNames(ticketCatalog.findAll().stream()),
            () -> limitQuery(new Query(), maxResults));
        if (criteria.getFrom() > 0) {
            ticketStream = ticketStream.skip(criteria.getFrom());
        }
        if (!criteria.isInfiniteCount()) {
            ticketStream = ticketStream.limit(criteria.getCount());
        }
        return ticketStream
            .map(this::decodeTicketFromDocument)
            .filter(Objects::nonNull);
    }

    @Override
    public long sessionCount() {
        return countTicketsByTicketType(TicketGrantingTicket.class);
    }

    @Override
    public long countSessionsFor(final String principalId) {
        val query = buildTicketQuery(buildPrincipalCriteria(principalId), buildUnexpiredTicketCriteria());
        return getTicketCollectionNames(ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).stream())
            .stream()
            .mapToLong(collectionName -> mongoTemplate.count(query, collectionName))
            .sum();
    }

    @Override
    public long countTickets() {
        return getTicketCollectionNames(ticketCatalog.findAll().stream())
            .stream()
            .mapToLong(collectionName -> mongoTemplate.count(new Query(), collectionName))
            .sum();
    }

    @Override
    public Stream<? extends Ticket> getTicketsFor(final Service service) {
        val collectionNames = getTicketCollectionNames(ticketCatalog.findAll().stream());
        return streamTicketDocuments(collectionNames,
            () -> buildTicketQuery(Criteria.where(MongoDbTicketDocument.FIELD_NAME_SERVICE).is(service.getId()),
                buildUnexpiredTicketCriteria()))
            .map(this::decodeTicketFromDocument)
            .filter(Objects::nonNull)
            .filter(ticket -> !ticket.isExpired());
    }

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        val collectionNames = getTicketCollectionNames(ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).stream());
        return streamTicketDocuments(collectionNames,
            () -> buildTicketQuery(buildPrincipalCriteria(principalId), buildUnexpiredTicketCriteria()))
            .map(this::decodeTicketFromDocument)
            .filter(Objects::nonNull)
            .filter(ticket -> !ticket.isExpired());
    }

    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        if (queryAttributes.isEmpty()) {
            return Stream.empty();
        }
        val criteria = new ArrayList<Criteria>();
        queryAttributes.forEach((key, values) -> {
            val attributeKey = MongoDbTicketDocument.FIELD_NAME_ATTRIBUTES + '.' + digestAttributeKey(key);
            val criteriaValues = values
                .stream()
                .map(queryValue -> Criteria.where(attributeKey).is(digestIdentifier(queryValue.toString())))
                .toList();
            if (!criteriaValues.isEmpty()) {
                criteria.add(new Criteria().orOperator(criteriaValues));
            }
        });
        if (criteria.isEmpty()) {
            return Stream.empty();
        }
        criteria.add(buildUnexpiredTicketCriteria());
        val collectionNames = getTicketCollectionNames(ticketCatalog.findTicketDefinition(TicketGrantingTicket.class).stream());
        return streamTicketDocuments(collectionNames,
            () -> {
                val query = buildTicketQuery(criteria);
                LOGGER.debug("Authenticated sessions query criteria is [{}]", query.getQueryObject());
                return query;
            })
            .map(this::decodeTicketFromDocument)
            .filter(Objects::nonNull)
            .filter(ticket -> !ticket.isExpired());
    }

    @Override
    public long serviceTicketCount() {
        return countTicketsByTicketType(ServiceTicket.class);
    }

    @Override
    public long deleteSingleTicket(final Ticket ticketToDelete) {
        val ticketId = digestIdentifier(ticketToDelete.getId());
        LOGGER.debug("Deleting ticket [{}]", ticketId);
        val metadata = ticketCatalog.find(ticketToDelete);
        val collectionName = getTicketCollectionInstanceByMetadata(Objects.requireNonNull(metadata));
        val query = new Query(Criteria.where(MongoDbTicketDocument.FIELD_NAME_ID).is(ticketId));
        val res = mongoTemplate.remove(query, collectionName);
        LOGGER.debug("Deleted ticket [{}] with result [{}]", ticketToDelete.getId(), res);
        return res.getDeletedCount();
    }

    @Override
    public List<? extends Serializable> query(final TicketRegistryQueryCriteria criteria) {
        val ticketDefinitions = StringUtils.isNotBlank(criteria.getType())
            ? Stream.of(Objects.requireNonNull(ticketCatalog.find(criteria.getType())))
            : ticketCatalog.findAll().stream();
        val queryCriteria = new ArrayList<Criteria>();
        if (StringUtils.isNotBlank(criteria.getId())) {
            queryCriteria.add(Criteria.where(MongoDbTicketDocument.FIELD_NAME_ID).is(digestIdentifier(criteria.getId())));
        }
        if (StringUtils.isNotBlank(criteria.getPrincipal())) {
            queryCriteria.add(buildPrincipalCriteria(criteria.getPrincipal()));
        }
        val maxResults = criteria.getCount();
        try (val documentStream = streamTicketDocuments(getTicketCollectionNames(ticketDefinitions),
            () -> limitQuery(buildTicketQuery(queryCriteria), maxResults))) {
            val limitedDocumentStream = maxResults > 0 ? documentStream.limit(maxResults) : documentStream;
            return limitedDocumentStream
                .map(document -> {
                    if (criteria.isDecode()) {
                        val ticket = decodeTicketFromDocument(document);
                        return ticket != null && !ticket.isExpired() ? ticket : null;
                    }
                    return "%s:%s".formatted(document.getTicketId(), StringUtils.defaultIfBlank(document.getPrincipal(), "N/A"));
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
    }

    @Override
    public long countTicketsFor(final Service service) {
        val query = buildTicketQuery(Criteria.where(MongoDbTicketDocument.FIELD_NAME_SERVICE).is(service.getId()),
            buildUnexpiredTicketCriteria());
        return getTicketCollectionNames(ticketCatalog.findAll().stream())
            .stream()
            .mapToLong(collectionName -> mongoTemplate.count(query, collectionName))
            .sum();
    }

    protected Ticket decodeTicketFromDocument(final MongoDbTicketDocument document) {
        return decodeTicket(deserializeTicket(document.getJson(), document.getType()));
    }

    protected Query buildTicketQuery(final Criteria... criteria) {
        return buildTicketQuery(Arrays.stream(criteria).toList());
    }

    protected Query buildTicketQuery(final Collection<Criteria> criteria) {
        return criteria.isEmpty()
            ? new Query()
            : new Query(new Criteria().andOperator(criteria));
    }

    protected Criteria buildPrincipalCriteria(final String principalId) {
        return Criteria.where(MongoDbTicketDocument.FIELD_NAME_PRINCIPAL).is(digestIdentifier(principalId));
    }

    protected Criteria buildUnexpiredTicketCriteria() {
        return new Criteria().orOperator(
            Criteria.where(MongoDbTicketDocument.FIELD_NAME_EXPIRE_AT).is(null),
            Criteria.where(MongoDbTicketDocument.FIELD_NAME_EXPIRE_AT).gt(DateTimeUtils.dateOf(Instant.now())));
    }

    protected long countTicketsByTicketType(final Class<? extends Ticket> ticketType) {
        return getTicketCollectionNames(ticketCatalog.findTicketImplementations(ticketType).stream())
            .stream()
            .mapToLong(collectionName -> mongoTemplate.count(new Query(), collectionName))
            .sum();
    }

    protected MongoDbTicketDocument buildTicketAsDocument(final Ticket ticket) throws Exception {
        val encTicket = encodeTicket(ticket);
        val json = serializeTicket(encTicket);
        if (StringUtils.isBlank(json)) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " cannot be serialized to JSON");
        }
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Serialized ticket into a JSON document as\n [{}]",
                JsonValue.readJSON(json).toString(Stringify.FORMATTED));
        }

        val expireAt = getExpireAt(ticket);
        LOGGER.trace("Calculated expiration date for ticket ttl as [{}]", expireAt);

        val principal = getPrincipalIdFrom(ticket);
        return MongoDbTicketDocument
            .builder()
            .expireAt(expireAt)
            .type(encTicket.getClass().getName())
            .ticketId(encTicket.getId())
            .json(json)
            .principal(digestIdentifier(principal))
            .service(ticket instanceof final ServiceAwareTicket sat && Objects.nonNull(sat.getService()) ? sat.getService().getId() : null)
            .attributes(collectAndDigestTicketAttributes(ticket))
            .build();
    }

    protected String getTicketCollectionInstanceByMetadata(final TicketDefinition metadata) {
        val mapName = metadata.getProperties().getStorageName();
        LOGGER.debug("Locating collection name [{}] for ticket definition [{}]", mapName, metadata);
        val collection = getTicketCollectionInstance(mapName);
        return Objects.requireNonNull(collection).getNamespace().getCollectionName();
    }

    protected MongoCollection getTicketCollectionInstance(final String mapName) {
        val inst = mongoTemplate.getCollection(mapName);
        LOGGER.debug("Located MongoDb collection instance [{}]", mapName);
        return inst;
    }

    /**
     * Resolve the distinct collection names backing the given ticket definitions. Several
     * definitions may share a storage name; addressing the resulting collection once keeps
     * counts accurate and stops the same collection from being scanned repeatedly.
     *
     * @param definitions the ticket definitions to resolve
     * @return the distinct collection names, in definition order
     */
    protected List<String> getTicketCollectionNames(final Stream<? extends TicketDefinition> definitions) {
        return definitions
            .map(this::getTicketCollectionInstanceByMetadata)
            .filter(StringUtils::isNotBlank)
            .distinct()
            .toList();
    }

    /**
     * Stream documents from every given collection as one stream. Each cursor is recorded as it is
     * opened and closed from the returned stream's close handler, because a consumer that
     * short-circuits (via {@code limit} or {@code findFirst}) never lets {@code flatMap} close the
     * inner stream it was reading. Callers must close the returned stream.
     *
     * @param collectionNames the collections to read from
     * @param queryProvider   supplies a fresh query per collection
     * @return the combined stream of ticket documents
     */
    protected Stream<MongoDbTicketDocument> streamTicketDocuments(final Collection<String> collectionNames,
                                                                 final Supplier<Query> queryProvider) {
        val cursors = Collections.synchronizedList(new ArrayList<Stream<MongoDbTicketDocument>>());
        return collectionNames
            .stream()
            .flatMap(collectionName -> {
                val documents = mongoTemplate.stream(queryProvider.get(), MongoDbTicketDocument.class, collectionName);
                cursors.add(documents);
                return documents;
            })
            .onClose(() -> cursors.forEach(Stream::close));
    }

    /**
     * Bound a query to the largest number of documents a single collection could contribute to the
     * final result, so the driver stops streaming an entire collection back only for the JVM to
     * discard it. A non-positive or out-of-range bound leaves the query unbounded.
     *
     * @param query      the query to bound
     * @param maxResults the greatest number of documents that can be needed from one collection
     * @return the same query instance
     */
    protected Query limitQuery(final Query query, final long maxResults) {
        if (maxResults > 0 && maxResults <= Integer.MAX_VALUE) {
            query.limit((int) maxResults);
        }
        return query;
    }

    /**
     * Address an attribute key the way the mapping converter stores it. Dots are reserved by MongoDB
     * as a path separator and are escaped on write, so a query that uses the raw key would address a
     * nested document rather than the attribute.
     *
     * @param key the attribute key
     * @return the key as it is persisted
     */
    protected String digestAttributeKey(final String key) {
        return digestIdentifier(key).replace(".", MongoDbConnectionFactory.MAP_KEY_DOT_REPLACEMENT);
    }

    private TicketDefinition findTicketDefinition(final Ticket ticket) {
        val metadata = ticketCatalog.find(ticket);
        if (metadata == null) {
            throw new IllegalArgumentException("Could not locate a ticket definition in the catalog for ticket " + ticket.getId());
        }
        return metadata;
    }
}
