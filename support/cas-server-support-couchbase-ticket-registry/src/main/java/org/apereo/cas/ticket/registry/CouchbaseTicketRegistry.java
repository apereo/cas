package org.apereo.cas.ticket.registry;

import com.couchbase.client.java.document.SerializableDocument;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.View;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * A Ticket Registry storage backend which uses the memcached protocol.
 * CouchBase is a multi host NoSQL database with a memcached interface
 * to persistent storage which also is quite usable as a replicated
 * ticket storage engine for multiple front end CAS servers.
 *
 * @author Fredrik Jönsson "fjo@kth.se"
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class CouchbaseTicketRegistry extends AbstractTicketRegistry {
    private static final long MAX_EXP_TIME_IN_DAYS = 30;

    private static final String END_TOKEN = "\u02ad";

    private static final String VIEW_NAME_ALL_TICKETS = "all_tickets";

    private static final View ALL_TICKETS_VIEW = DefaultView.create(
            VIEW_NAME_ALL_TICKETS,
            "function(d,m) {emit(m.id);}",
            "_count");
    private static final List<View> ALL_VIEWS = Lists.newArrayList(new View[]{
            ALL_TICKETS_VIEW
    });
    private static final String UTIL_DOCUMENT = "statistics";

    @Autowired
    private CasConfigurationProperties casProperties;

    private CouchbaseClientFactory couchbase;

    /**
     * Default constructor.
     */
    public CouchbaseTicketRegistry() {
    }

    @Override
    public void updateTicket(final Ticket ticket) {
        logger.debug("Updating ticket {}", ticket);
        try {
            final SerializableDocument document =
                    SerializableDocument.create(ticket.getId(), getTimeToLive(ticket), ticket);

            logger.debug("Upserting document {} into couchbase bucket {}", document.id(),
                    this.couchbase.bucket().name());
            this.couchbase.bucket().upsert(document);
        } catch (final Exception e) {
            logger.error("Failed updating {}: {}", ticket, e);
        }
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        logger.debug("Adding ticket {}", ticketToAdd);
        try {
            final Ticket ticket = encodeTicket(ticketToAdd);
            final SerializableDocument document =
                    SerializableDocument.create(ticket.getId(), getTimeToLive(ticketToAdd), ticket);
            logger.debug("Created document for ticket {}. Upserting into bucket {}",
                    ticketToAdd, this.couchbase.bucket().name());
            this.couchbase.bucket().upsert(document);
        } catch (final Exception e) {
            logger.error("Failed adding {}: {}", ticketToAdd, e);
        }
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        try {
            logger.debug("Locating ticket id {}", ticketId);
            final String encTicketId = encodeTicketId(ticketId);
            if (encTicketId == null) {
                logger.debug("Ticket id {} could not be found", ticketId);
                return null;
            }

            final SerializableDocument document = this.couchbase.bucket().get(encTicketId, SerializableDocument.class);
            if (document != null) {
                final Ticket t = (Ticket) document.content();
                logger.debug("Got ticket {} from the registry.", t);
                return t;
            }
            logger.debug("Ticket {} not found in the registry.", encTicketId);
            return null;
        } catch (final Exception e) {
            logger.error("Failed fetching {}: {}", ticketId, e);
            return null;
        }
    }


    /**
     * Starts the couchbase client.
     */
    @PostConstruct
    public void initialize() {
        logger.info("Setting up Couchbase Ticket Registry instance");
        System.setProperty("com.couchbase.queryEnabled",
                Boolean.toString(casProperties.getTicket().getRegistry().getCouchbase().isQueryEnabled()));
        logger.debug("Setting up indexes on document {} and views {}", UTIL_DOCUMENT, ALL_VIEWS);
        this.couchbase.ensureIndexes(UTIL_DOCUMENT, ALL_VIEWS);

        logger.info("Initializing Couchbase...");
        this.couchbase.initialize();
        logger.info("Initialized Couchbase bucket {}", this.couchbase.bucket().name());
    }


    /**
     * Stops the couchbase client.
     */
    @PreDestroy
    public void destroy() {
        try {
            logger.debug("Shutting down Couchbase");
            this.couchbase.shutdown();
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public Collection<Ticket> getTickets() {
        logger.debug("getTickets() isn't supported by Couchbase. Returning empty list");
        return new ArrayList<>();
    }

    @Override
    public long sessionCount() {
        return runQuery(TicketGrantingTicket.PREFIX + '-');
    }

    @Override
    public long serviceTicketCount() {
        return runQuery(ServiceTicket.PREFIX + '-');
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        logger.debug("Deleting ticket {}", ticketId);
        try {
            return this.couchbase.bucket().remove(ticketId) != null;
        } catch (final Exception e) {
            logger.error("Failed deleting {}: {}", ticketId, e);
            return false;
        }
    }

    private int runQuery(final String prefix) {
        logger.debug("Running query on document {} and view {} with prefix {}",
                UTIL_DOCUMENT, VIEW_NAME_ALL_TICKETS, prefix);
        final ViewResult allKeys = this.couchbase.bucket().query(
                ViewQuery.from(UTIL_DOCUMENT, VIEW_NAME_ALL_TICKETS)
                        .startKey(prefix)
                        .endKey(prefix + END_TOKEN)
                        .reduce());
        final Iterator<ViewRow> iterator = allKeys.iterator();
        if (iterator.hasNext()) {
            final ViewRow res = iterator.next();

            final Integer count = (Integer) res.value();
            logger.debug("Found {} rows", count);
            return count;
        }
        logger.debug("No rows could be found by the query");
        return 0;
    }

    /**
     * Get the expiration policy value of the ticket in seconds.
     *
     * @param ticket the ticket
     * @return the exp value
     * @see <a href="http://docs.couchbase.com/developer/java-2.0/documents-basics.html">Couchbase Docs</a>
     */
    private int getTimeToLive(final Ticket ticket) {
        final int expTime = ticket.getExpirationPolicy().getTimeToLive().intValue();
        if (TimeUnit.SECONDS.toDays(expTime) >= MAX_EXP_TIME_IN_DAYS) {
            logger.warn("Any expiration time larger than {} days in seconds is considered absolute (as in a Unix time stamp) "
                    + "anything smaller is considered relative in seconds.", MAX_EXP_TIME_IN_DAYS);

        }
        return expTime;
    }

    public void setCouchbaseClientFactory(final CouchbaseClientFactory couchbase) {
        this.couchbase = couchbase;
    }
}

