package org.apereo.cas.ticket.registry;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.apereo.cas.ticket.BaseTicketSerializers;
import org.apereo.cas.ticket.Ticket;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * A Ticket Registry storage backend based on MongoDB.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class MongoDbTicketRegistry extends AbstractTicketRegistry {

    private final String collectionName;

    private final boolean dropCollection;

    private final MongoOperations mongoTemplate;

    public MongoDbTicketRegistry(final String collectionName, final MongoOperations mongoTemplate) {
        this(collectionName, false, mongoTemplate);
    }

    public MongoDbTicketRegistry(final String collectionName, final boolean dropCollection, final MongoOperations mongoTemplate) {
        this.collectionName = collectionName;
        this.dropCollection = dropCollection;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Init registry.
     **/
    @PostConstruct
    public void initialize() {
        Assert.notNull(this.mongoTemplate);

        logger.debug("Setting up MongoDb Ticket Registry instance {}", this.collectionName);
        if (this.dropCollection) {
            logger.debug("Dropping database collection: {}", this.collectionName);
            this.mongoTemplate.dropCollection(this.collectionName);
        }

        if (!this.mongoTemplate.collectionExists(this.collectionName)) {
            logger.debug("Creating database collection: {}", this.collectionName);
            this.mongoTemplate.createCollection(this.collectionName);
        }
        logger.debug("Creating indices on collection {} to auto-expire documents...", this.collectionName);
        final DBCollection collection = mongoTemplate.getCollection(this.collectionName);
        collection.createIndex(new BasicDBObject(TicketHolder.FIELD_NAME_EXPIRE_AT, 1),
                new BasicDBObject("expireAfterSeconds", 0));

        logger.info("Configured MongoDb Ticket Registry instance {}", this.collectionName);
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        logger.debug("Updating ticket {}", ticket);
        try {
            final TicketHolder holder = buildTicketAsDocument(ticket);
            this.mongoTemplate.updateFirst(new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(holder.getTicketId())),
                    Update.update(TicketHolder.FIELD_NAME_JSON, holder.getJson()), this.collectionName);
        } catch (final Exception e) {
            logger.error("Failed updating {}: {}", ticket, e);
        }
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        logger.debug("Adding ticket {}", ticket);
        try {
            this.mongoTemplate.insert(buildTicketAsDocument(ticket), this.collectionName);
        } catch (final Exception e) {
            logger.error("Failed adding {}: {}", ticket, e);
        }
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        try {
            logger.debug("Locating ticket ticketId {}", ticketId);
            final String encTicketId = encodeTicketId(ticketId);
            if (encTicketId == null) {
                logger.debug("Ticket ticketId {} could not be found", ticketId);
                return null;
            }
            final TicketHolder d = this.mongoTemplate.findOne(new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(encTicketId)),
                    TicketHolder.class, this.collectionName);
            if (d != null) {
                return deserializeTicketFromMongoDocument(d);
            }
        } catch (final Exception e) {
            logger.error("Failed fetching {}: {}", ticketId, e);
        }
        return null;
    }

    @Override
    public Collection<Ticket> getTickets() {
        final Collection<TicketHolder> c = this.mongoTemplate.findAll(TicketHolder.class, this.collectionName);
        return c.stream().map(t -> deserializeTicketFromMongoDocument(t)).collect(Collectors.toSet());
    }

    @Override
    public long sessionCount() {
        return 0;
    }

    @Override
    public long serviceTicketCount() {
        return 0;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        logger.debug("Deleting ticket {}", ticketId);
        try {
            this.mongoTemplate.remove(new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).is(ticketId)), this.collectionName);
            return true;
        } catch (final Exception e) {
            logger.error("Failed deleting {}: {}", ticketId, e);
        }
        return false;
    }

    @Override
    public long deleteAll() {
        final Query query = new Query(Criteria.where(TicketHolder.FIELD_NAME_ID).regex(".+"));
        final long count = this.mongoTemplate.count(query, this.collectionName);
        mongoTemplate.remove(query, this.collectionName);
        return count;
    }

    private int getTimeToLive(final Ticket ticket) {
        return ticket.getExpirationPolicy().getTimeToLive().intValue();
    }

    private String serializeTicketForMongoDocument(final Ticket ticket) {
        return BaseTicketSerializers.serializeTicket(ticket);
    }

    private Ticket deserializeTicketFromMongoDocument(final TicketHolder holder) {
        return BaseTicketSerializers.deserializeTicket(holder.getJson(), holder.getType());
    }

    private TicketHolder buildTicketAsDocument(final Ticket ticket) {
        final String json = serializeTicketForMongoDocument(ticket);
        return new TicketHolder(json, ticket.getId(), ticket.getClass().getName(), getTimeToLive(ticket));
    }
}

