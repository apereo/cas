package org.jasig.cas.ticket.registry;

import com.couchbase.client.java.document.SerializableDocument;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.View;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import org.jasig.cas.couchbase.core.CouchbaseClientFactory;
import org.jasig.cas.ticket.ServiceTicket;

import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.encrypt.AbstractCrypticTicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * A Ticket Registry storage backend which uses the memcached protocol.
 * CouchBase is a multi host NoSQL database with a memcached interface
 * to persistent storage which also is quite usable as a replicated
 * tickage storage engine for multiple front end CAS servers.
 *
 * @author Fredrik Jönsson "fjo@kth.se"
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("couchbaseTicketRegistry")
public final class CouchbaseTicketRegistry extends AbstractCrypticTicketRegistry implements TicketRegistryState {
    private static final String END_TOKEN = "\u02ad";

    private static final View ALL_TICKETS_VIEW = DefaultView.create(
            "all_tickets",
            "function(d,m) {emit(m.id);}",
            "_count");
    private static final List<View> ALL_VIEWS = Arrays.asList(new View[] {
            ALL_TICKETS_VIEW
    });
    private static final String UTIL_DOCUMENT = "statistics";

    /* Couchbase client factory */
    @NotNull
    @Autowired
    @Qualifier("ticketRegistryCouchbaseClientFactory")
    private CouchbaseClientFactory couchbase;

    @Min(0)
    @Value("${tgt.maxTimeToLiveInSeconds:28800}")
    private int tgtTimeout;

    @Min(0)
    @Value("${st.timeToKillInSeconds:10}")
    private int stTimeout;


    @Value("${ticketreg.couchbase.query.enabled:true}")
    private boolean queryEnabled;

    /**
     * Default constructor.
     */
    public CouchbaseTicketRegistry() {}

    @Override
    protected void updateTicket(final Ticket ticket) {
        logger.debug("Updating ticket {}", ticket);
        try {
            final SerializableDocument document =
                    SerializableDocument.create(ticket.getId(), getTimeout(ticket), ticket);
            couchbase.bucket().upsert(document);
        } catch (final Exception e) {
            logger.error("Failed updating {}: {}", ticket, e);
        }
    }

    @Override
    public void addTicket(final Ticket ticket) {
        logger.debug("Adding ticket {}", ticket);
        try {
            final SerializableDocument document =
                    SerializableDocument.create(ticket.getId(), getTimeout(ticket), ticket);
            couchbase.bucket().upsert(document);
        } catch (final Exception e) {
            logger.error("Failed adding {}: {}", ticket, e);
        }
    }

    @Override
    public boolean deleteTicket(final String ticketId) {
        logger.debug("Deleting ticket {}", ticketId);
        try {
            couchbase.bucket().remove(ticketId);
            return true;
        } catch (final Exception e) {
            logger.error("Failed deleting {}: {}", ticketId, e);
            return false;
        }
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        try {
            final SerializableDocument document = couchbase.bucket().get(ticketId, SerializableDocument.class);
            if (document != null) {
                final Ticket t = (Ticket) document.content();
                logger.debug("Got ticket {} from registry.", t);
                return getProxiedTicketInstance(t);
            }
            logger.debug("Ticket {} not found in registry.", ticketId);
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
        System.setProperty("com.couchbase.queryEnabled", Boolean.toString(this.queryEnabled));
        couchbase.ensureIndexes(UTIL_DOCUMENT, ALL_VIEWS);
        couchbase.initialize();
    }


    /**
     * Stops the couchbase client.
     */
    @PreDestroy
    public void destroy() {
        try {
            couchbase.shutdown();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean needsCallback() {
        return true;
    }

    @Override
    public Collection<Ticket> getTickets() {
        throw new UnsupportedOperationException("GetTickets not supported.");
    }

    @Override
    public int sessionCount() {
        return runQuery(TicketGrantingTicket.PREFIX + '-');
    }

    @Override
    public int serviceTicketCount() {
        return runQuery(ServiceTicket.PREFIX + '-');
    }

    private int runQuery(final String prefix) {
        final ViewResult allKeys = couchbase.bucket().query(
                ViewQuery.from(UTIL_DOCUMENT, "all_tickets")
                        .startKey(prefix)
                        .endKey(prefix + END_TOKEN)
                        .reduce());
        final Iterator<ViewRow> iterator = allKeys.iterator();
        if (iterator.hasNext()) {
            final ViewRow res = iterator.next();
            return (Integer) res.value();
        } else {
            return 0;
        }
    }


    /**
     * Sets the time after which a ticket granting ticket will be
     * purged from the registry.
     *
     * @param tgtTimeout Ticket granting ticket timeout in seconds.
     */
    public void setTgtTimeout(final int tgtTimeout) {
        this.tgtTimeout = tgtTimeout;
    }


    /**
     * Sets the time after which a session ticket will be purged
     * from the registry.
     *
     * @param stTimeout Session ticket timeout in seconds.
     */
    public void setStTimeout(final int stTimeout) {
        this.stTimeout = stTimeout;
    }


    /**
     * @param t a CAS ticket.
     * @return the ticket timeout for the ticket in the registry.
     */
    private int getTimeout(final Ticket t) {
        if (t instanceof TicketGrantingTicket) {
            return tgtTimeout;
        } else if (t instanceof ServiceTicket) {
            return stTimeout;
        }
        throw new IllegalArgumentException("Invalid ticket type");
    }


    /**
     * @param couchbase the client factory to use.
     */
    public void setCouchbaseClientFactory(final CouchbaseClientFactory couchbase) {
        this.couchbase = couchbase;
    }
}

