package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;

import com.mongodb.client.MongoCollection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.hjson.JsonValue;
import org.hjson.Stringify;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.StreamUtils;

import java.util.Collection;
import java.util.Date;
import java.util.List;
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
@RequiredArgsConstructor
public class MongoDbTicketRegistry extends AbstractTicketRegistry {

    private final TicketCatalog ticketCatalog;

    private final MongoOperations mongoTemplate;

    private final TicketSerializationManager ticketSerializationManager;

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        LOGGER.debug("Updating ticket [{}]", ticket);
        try {
            val holder = buildTicketAsDocument(ticket);
            val metadata = this.ticketCatalog.find(ticket);
            if (metadata == null) {
                LOGGER.error("Could not locate ticket definition in the catalog for ticket [{}]", ticket.getId());
                return null;
            }
            LOGGER.debug("Located ticket definition [{}] in the ticket catalog", metadata);
            val collectionName = getTicketCollectionInstanceByMetadata(metadata);
            if (StringUtils.isBlank(collectionName)) {
                LOGGER.error("Could not locate collection linked to ticket definition for ticket [{}]", ticket.getId());
                return null;
            }
            val query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(holder.getTicketId()));
            val update = Update.update(TicketHolder.FIELD_NAME_JSON, holder.getJson());
            this.mongoTemplate.upsert(query, update, collectionName);
            LOGGER.debug("Updated ticket [{}]", ticket);
        } catch (final Exception e) {
            LOGGER.error("Failed updating [{}]: [{}]", ticket, e);
        }
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        try {
            LOGGER.debug("Adding ticket [{}]", ticket.getId());
            val holder = buildTicketAsDocument(ticket);
            val metadata = this.ticketCatalog.find(ticket);
            if (metadata == null) {
                LOGGER.error("Could not locate ticket definition in the catalog for ticket [{}]", ticket.getId());
                return;
            }
            LOGGER.trace("Located ticket definition [{}] in the ticket catalog", metadata);
            val collectionName = getTicketCollectionInstanceByMetadata(metadata);
            if (StringUtils.isBlank(collectionName)) {
                LOGGER.error("Could not locate collection linked to ticket definition for ticket [{}]", ticket.getId());
                return;
            }
            LOGGER.trace("Found collection [{}] linked to ticket [{}]", collectionName, metadata);
            this.mongoTemplate.insert(holder, collectionName);
            LOGGER.debug("Added ticket [{}]", ticket.getId());
        } catch (final Exception e) {
            LOGGER.error(String.format("Failed adding %s", ticket), e);
        }
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        try {
            LOGGER.debug("Locating ticket ticketId [{}]", ticketId);
            val encTicketId = encodeTicketId(ticketId);
            if (encTicketId == null) {
                LOGGER.debug("Ticket id [{}] could not be found", ticketId);
                return null;
            }
            val metadata = this.ticketCatalog.find(ticketId);
            if (metadata == null) {
                LOGGER.debug("Ticket definition [{}] could not be found in the ticket catalog", ticketId);
                return null;
            }
            val collectionName = getTicketCollectionInstanceByMetadata(metadata);
            val query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(encTicketId));
            val d = this.mongoTemplate.findOne(query, TicketHolder.class, collectionName);
            if (d != null) {
                val decoded = deserializeTicketFromMongoDocument(d);
                val result = decodeTicket(decoded);

                if (predicate.test(result)) {
                    return result;
                }
                return null;
            }
        } catch (final Exception e) {
            LOGGER.error(String.format("Failed fetching %s", ticketId), e);
        }
        return null;
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return this.ticketCatalog.findAll().stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .map(map -> mongoTemplate.findAll(TicketHolder.class, map))
            .flatMap(List::stream)
            .map(ticket -> decodeTicket(deserializeTicketFromMongoDocument(ticket)))
            .collect(Collectors.toSet());
    }

    @Override
    public boolean deleteSingleTicket(final String ticketIdToDelete) {
        val ticketId = encodeTicketId(ticketIdToDelete);
        LOGGER.debug("Deleting ticket [{}]", ticketId);
        try {
            val metadata = this.ticketCatalog.find(ticketIdToDelete);
            val collectionName = getTicketCollectionInstanceByMetadata(metadata);
            val query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(ticketId));
            val res = this.mongoTemplate.remove(query, collectionName);
            LOGGER.debug("Deleted ticket [{}] with result [{}]", ticketIdToDelete, res);
            return true;
        } catch (final Exception e) {
            LOGGER.error("Failed deleting [{}]: [{}]", ticketId, e);
        }
        return false;
    }

    @Override
    public long deleteAll() {
        val query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).exists(true));
        return this.ticketCatalog.findAll().stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .filter(StringUtils::isNotBlank)
            .mapToLong(collectionName -> {
                val countTickets = this.mongoTemplate.count(query, collectionName);
                mongoTemplate.remove(query, collectionName);
                return countTickets;
            })
            .sum();
    }

    @Override
    public Stream<Ticket> getTicketsStream() {
        return ticketCatalog.findAll().stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .map(map -> mongoTemplate.stream(new Query(), TicketHolder.class, map))
            .flatMap(StreamUtils::createStreamFromIterator)
            .map(ticket -> decodeTicket(deserializeTicketFromMongoDocument(ticket)));
    }

    @Override
    public long serviceTicketCount() {
        return countTicketsByTicketType(ServiceTicket.class);
    }

    @Override
    public long sessionCount() {
        return countTicketsByTicketType(TicketGrantingTicket.class);
    }

    @Override
    public long countSessionsFor(final String principalId) {
        if (isCipherExecutorEnabled()) {
            return super.countSessionsFor(principalId);
        }

        val ticketDefinitions = ticketCatalog.find(TicketGrantingTicket.class);
        return ticketDefinitions.stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .mapToLong(map -> {
                val criteria = TextCriteria.forDefaultLanguage().matchingAny(principalId);
                val query = TextQuery.queryText(criteria)
                    .sortByScore()
                    .with(PageRequest.of(0, 10));
                return mongoTemplate.count(query, map);
            })
            .sum();
    }

    private long countTicketsByTicketType(final Class<? extends Ticket> ticketType) {
        val ticketDefinitions = ticketCatalog.find(ticketType);
        return ticketDefinitions.stream()
            .map(this::getTicketCollectionInstanceByMetadata)
            .mapToLong(map -> mongoTemplate.count(new Query(), map))
            .sum();
    }

    private TicketHolder buildTicketAsDocument(final Ticket ticket) {
        val encTicket = encodeTicket(ticket);
        val json = serializeTicketForMongoDocument(encTicket);
        if (StringUtils.isNotBlank(json)) {
            LOGGER.trace("Serialized ticket into a JSON document as \n [{}]", JsonValue.readJSON(json).toString(Stringify.FORMATTED));
            val expireAt = getExpireAt(ticket);
            return new TicketHolder(json, encTicket.getId(), encTicket.getClass().getName(), expireAt);
        }
        throw new IllegalArgumentException("Ticket " + ticket.getId() + " cannot be serialized to JSON");
    }

    private String getTicketCollectionInstanceByMetadata(final TicketDefinition metadata) {
        val mapName = metadata.getProperties().getStorageName();
        LOGGER.debug("Locating collection name [{}] for ticket definition [{}]", mapName, metadata);
        val c = getTicketCollectionInstance(mapName);
        if (c != null) {
            return c.getNamespace().getCollectionName();
        }
        throw new IllegalArgumentException("Could not locate MongoDb collection " + mapName);
    }

    private MongoCollection getTicketCollectionInstance(final String mapName) {
        try {
            val inst = this.mongoTemplate.getCollection(mapName);
            LOGGER.debug("Located MongoDb collection instance [{}]", mapName);
            return inst;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Calculate the time at which the ticket is eligible for automated deletion by MongoDb.
     * Makes the assumption that the CAS server date and the Mongo server date are in sync.
     */
    private static Date getExpireAt(final Ticket ticket) {
        val expirationPolicy = ticket.getExpirationPolicy();
        val ttl = ticket instanceof TicketState
            ? expirationPolicy.getTimeToLive((TicketState) ticket)
            : expirationPolicy.getTimeToLive();

        if (ttl < 1) {
            return null;
        }

        return new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(ttl));
    }

    private String serializeTicketForMongoDocument(final Ticket ticket) {
        try {
            return ticketSerializationManager.serializeTicket(ticket);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }

    private Ticket deserializeTicketFromMongoDocument(final TicketHolder holder) {
        return ticketSerializationManager.deserializeTicket(holder.getJson(), holder.getType());
    }
}

