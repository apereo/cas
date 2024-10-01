package org.apereo.cas.ticket.registry;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.ticket.ServiceAwareTicket;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import com.mongodb.client.MongoCollection;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.query.Update;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Ticket Registry storage backend based on MongoDB.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@Monitorable
public class MongoDbTicketRegistry extends AbstractTicketRegistry {
    private static final int PAGE_SIZE = 500;

    private final MongoOperations mongoTemplate;

    public MongoDbTicketRegistry(final CipherExecutor cipherExecutor, final TicketSerializationManager ticketSerializationManager,
                                 final TicketCatalog ticketCatalog, final ConfigurableApplicationContext applicationContext,
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
    public Ticket addSingleTicket(final Ticket ticket) {
        try {
            LOGGER.debug("Adding ticket [{}]", ticket.getId());
            val document = buildTicketAsDocument(ticket);
            val metadata = ticketCatalog.find(ticket);
            if (metadata == null) {
                LOGGER.error("Could not locate ticket definition in the catalog for ticket [{}]", ticket.getId());
            } else {
                LOGGER.trace("Located ticket definition [{}] in the ticket catalog", metadata);
                val collectionName = getTicketCollectionInstanceByMetadata(metadata);
                LOGGER.trace("Found collection [{}] linked to ticket [{}]", collectionName, metadata);
                mongoTemplate.insert(document, collectionName);
                LOGGER.debug("Added ticket [{}]", ticket.getId());
            }
        } catch (final Throwable e) {
            LOGGER.error("Failed adding [{}]", ticket);
            LoggingUtils.error(LOGGER, e);
        }
        return ticket;
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        try {
            LOGGER.debug("Locating ticket ticketId [{}]", ticketId);
            val encTicketId = digestIdentifier(ticketId);
            if (encTicketId == null) {
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
            if (found != null) {
                val decoded = deserializeTicket(found.getJson(), found.getType());
                val result = decodeTicket(decoded);

                if (predicate.test(result)) {
                    return result;
                }
                return null;
            }
        } catch (final Throwable e) {
            LOGGER.error("Failed fetching [{}]", ticketId);
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public long deleteAll() {
        val query = new Query(Criteria.where(MongoDbTicketDocument.FIELD_NAME_ID).exists(true));
        return ticketCatalog.findAll()
            .stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .filter(StringUtils::isNotBlank)
            .mapToLong(collectionName -> {
                val countTickets = mongoTemplate.count(query, collectionName);
                mongoTemplate.remove(query, collectionName);
                return countTickets;
            })
            .sum();
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return ticketCatalog.findAll()
            .stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .map(map -> mongoTemplate.findAll(MongoDbTicketDocument.class, map))
            .flatMap(List::stream)
            .map(ticket -> decodeTicket(deserializeTicket(ticket.getJson(), ticket.getType())))
            .filter(ticket -> !ticket.isExpired())
            .collect(Collectors.toSet());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        LOGGER.debug("Updating ticket [{}]", ticket);
        try {
            val holder = buildTicketAsDocument(ticket);
            val metadata = ticketCatalog.find(ticket);
            if (metadata == null) {
                LOGGER.error("Could not locate ticket definition in the catalog for ticket [{}]", ticket.getId());
                return null;
            }
            LOGGER.debug("Located ticket definition [{}] in the ticket catalog", metadata);
            val collectionName = getTicketCollectionInstanceByMetadata(metadata);
            val query = new Query(Criteria.where(MongoDbTicketDocument.FIELD_NAME_ID).is(holder.getTicketId()));
            val update = Update.update(MongoDbTicketDocument.FIELD_NAME_JSON, holder.getJson());
            val result = mongoTemplate.updateFirst(query, update, collectionName);
            LOGGER.debug("Updated ticket [{}] with result [{}]", ticket, result);
            return result.getMatchedCount() > 0 ? ticket : null;
        } catch (final Throwable e) {
            LOGGER.error("Failed updating [{}]", ticket);
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public Stream<Ticket> stream(final TicketRegistryStreamCriteria criteria) {
        return ticketCatalog
            .findAll()
            .stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .flatMap(map -> mongoTemplate.stream(new Query(), MongoDbTicketDocument.class, map))
            .skip(criteria.getFrom())
            .limit(criteria.getCount())
            .map(ticket -> decodeTicket(deserializeTicket(ticket.getJson(), ticket.getType())));
    }

    @Override
    public long sessionCount() {
        return countTicketsByTicketType(TicketGrantingTicket.class);
    }

    @Override
    public long countSessionsFor(final String principalId) {
        return getSessionsFor(principalId).count();
    }

    @Override
    public long countTickets() {
        return ticketCatalog
            .findAll()
            .stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .mapToLong(map -> mongoTemplate.count(new Query(), map))
            .sum();
    }

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        val ticketDefinitions = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class);
        return ticketDefinitions
            .stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .flatMap(map -> {
                val query = isCipherExecutorEnabled()
                    ? new Query(Criteria.where(MongoDbTicketDocument.FIELD_NAME_PRINCIPAL).is(digestIdentifier(principalId)))
                    : TextQuery.queryText(TextCriteria.forDefaultLanguage().matchingAny(principalId)).sortByScore().with(PageRequest.of(0, PAGE_SIZE));
                return mongoTemplate.stream(query, MongoDbTicketDocument.class, map);
            })
            .map(ticket -> decodeTicket(deserializeTicket(ticket.getJson(), ticket.getType())))
            .filter(ticket -> !ticket.isExpired());
    }

    @Override
    public Stream<? extends Ticket> getSessionsWithAttributes(final Map<String, List<Object>> queryAttributes) {
        val ticketDefinitions = ticketCatalog.findTicketDefinition(TicketGrantingTicket.class);
        return ticketDefinitions
            .stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .flatMap(map -> {
                val criteria = queryAttributes.entrySet()
                    .stream()
                    .map(entry -> {
                        val criteriaValues = entry.getValue()
                            .stream()
                            .map(queryValue -> {
                                val key = MongoDbTicketDocument.FIELD_NAME_ATTRIBUTES + '.' + digestIdentifier(entry.getKey());
                                return Criteria.where(key).is(digestIdentifier(queryValue.toString()));
                            })
                            .toList();
                        return new Criteria().orOperator(criteriaValues);
                    })
                    .collect(Collectors.toList());
                val finalCriteria = new Criteria().andOperator(criteria);
                LOGGER.debug("Authenticated sessions query criteria is [{}]", finalCriteria.getCriteriaObject());
                val query = new Query(finalCriteria);
                return mongoTemplate.stream(query, MongoDbTicketDocument.class, map);
            })
            .map(ticket -> decodeTicket(deserializeTicket(ticket.getJson(), ticket.getType())))
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
        val collectionName = getTicketCollectionInstanceByMetadata(metadata);
        val query = new Query(Criteria.where(MongoDbTicketDocument.FIELD_NAME_ID).is(ticketId));
        val res = mongoTemplate.remove(query, collectionName);
        LOGGER.debug("Deleted ticket [{}] with result [{}]", ticketToDelete.getId(), res);
        return res.getDeletedCount();
    }

    @Override
    public List<? extends Serializable> query(final TicketRegistryQueryCriteria criteria) {
        val ticketDefinitions = StringUtils.isNotBlank(criteria.getType())
            ? List.of(ticketCatalog.find(criteria.getType()))
            : ticketCatalog.findAll();
        return ticketDefinitions
            .stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .flatMap(map -> {
                val limit = criteria.getCount() > 0 ? Limit.of(Long.valueOf(criteria.getCount()).intValue()) : Limit.unlimited();
                val query = StringUtils.isNotBlank(criteria.getId())
                    ? new Query(Criteria.where(MongoDbTicketDocument.FIELD_NAME_ID).is(digestIdentifier(criteria.getId())))
                    : new Query();
                return mongoTemplate.stream(query.limit(limit), MongoDbTicketDocument.class, map);
            })
            .filter(document -> StringUtils.isBlank(criteria.getPrincipal())
                || StringUtils.equalsIgnoreCase(criteria.getPrincipal(), document.getPrincipal()))
            .map(document -> {
                if (criteria.isDecode()) {
                    val ticket = decodeTicket(deserializeTicket(document.getJson(), document.getType()));
                    return ticket != null ? !ticket.isExpired() : null;
                }
                return "%s:%s".formatted(document.getTicketId(), StringUtils.defaultIfBlank(document.getPrincipal(), "N/A"));
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public long countTicketsFor(final Service service) {
        return ticketCatalog
            .findAll()
            .stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .flatMap(map -> {
                val query = new Query(Criteria.where(MongoDbTicketDocument.FIELD_NAME_SERVICE).is(service.getId()));
                return mongoTemplate.stream(query, MongoDbTicketDocument.class, map);
            })
            .map(document -> {
                val ticket = decodeTicket(deserializeTicket(document.getJson(), document.getType()));
                return ticket != null ? !ticket.isExpired() : null;
            })
            .filter(Objects::nonNull)
            .count();
    }

    protected long countTicketsByTicketType(final Class<? extends Ticket> ticketType) {
        val ticketDefinitions = ticketCatalog.findTicketImplementations(ticketType);
        return ticketDefinitions
            .stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .mapToLong(map -> mongoTemplate.count(new Query(), map))
            .sum();
    }

    protected MongoDbTicketDocument buildTicketAsDocument(final Ticket ticket) throws Throwable {
        val encTicket = encodeTicket(ticket);
        val json = serializeTicket(encTicket);
        FunctionUtils.throwIf(StringUtils.isBlank(json),
            () -> new IllegalArgumentException("Ticket " + ticket.getId() + " cannot be serialized to JSON"));
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
        return FunctionUtils.doUnchecked(() -> {
            val inst = mongoTemplate.getCollection(mapName);
            LOGGER.debug("Located MongoDb collection instance [{}]", mapName);
            return inst;
        });
    }

}

