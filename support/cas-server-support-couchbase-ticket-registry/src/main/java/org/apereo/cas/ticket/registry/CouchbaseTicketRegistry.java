package org.apereo.cas.ticket.registry;

import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;

import com.couchbase.client.java.document.SerializableDocument;
import com.couchbase.client.java.view.DefaultView;
import com.couchbase.client.java.view.View;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
@Slf4j
@RequiredArgsConstructor
public class CouchbaseTicketRegistry extends AbstractTicketRegistry implements DisposableBean {
    /**
     * The all tickets view name.
     */
    public static final String VIEW_NAME_ALL_TICKETS = "all_tickets";

    /**
     * All tickets view.
     */
    public static final View ALL_TICKETS_VIEW = DefaultView.create(
        VIEW_NAME_ALL_TICKETS,
        "function(d,m) {emit(m.id);}",
        "_count");

    /**
     * Views available.
     */
    public static final Collection<View> ALL_VIEWS = CollectionUtils.wrap(ALL_TICKETS_VIEW);

    /**
     * "statistics" document.
     */
    public static final String UTIL_DOCUMENT = "statistics";

    private static final long MAX_EXP_TIME_IN_DAYS = 30;
    private static final String END_TOKEN = "\u02ad";

    private final TicketCatalog ticketCatalog;
    private final CouchbaseClientFactory couchbase;

    private static int getViewRowCountFromViewResultIterator(final Iterator<ViewRow> iterator) {
        if (iterator.hasNext()) {
            val res = iterator.next();
            val count = (Integer) res.value();
            LOGGER.debug("Found [{}] rows", count);
            return count;
        }
        LOGGER.debug("No rows could be found by the query iterator.");
        return 0;
    }

    /**
     * Get the expiration policy value of the ticket in seconds.
     *
     * @param ticket the ticket
     * @return the exp value
     * @see <a href="http://docs.couchbase.com/developer/java-2.0/documents-basics.html">Couchbase Docs</a>
     */
    private static int getTimeToLive(final Ticket ticket) {
        val expTime = ticket.getExpirationPolicy().getTimeToLive().intValue();
        if (TimeUnit.SECONDS.toDays(expTime) >= MAX_EXP_TIME_IN_DAYS) {
            LOGGER.warn("Any expiration time larger than [{}] days in seconds is considered absolute (as in a Unix time stamp) "
                + "anything smaller is considered relative in seconds.", MAX_EXP_TIME_IN_DAYS);

        }
        return expTime;
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        LOGGER.debug("Adding ticket [{}]", ticketToAdd);
        try {
            val ticket = encodeTicket(ticketToAdd);
            val document = SerializableDocument.create(ticket.getId(), getTimeToLive(ticketToAdd), ticket);
            val bucket = this.couchbase.getBucket();
            LOGGER.debug("Created document for ticket [{}]. Upserting into bucket [{}]", ticketToAdd, bucket.name());
            bucket.upsert(document);
        } catch (final Exception e) {
            LOGGER.error("Failed adding [{}]: [{}]", ticketToAdd, e);
        }
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        try {
            LOGGER.debug("Locating ticket id [{}]", ticketId);
            val encTicketId = encodeTicketId(ticketId);
            if (encTicketId == null) {
                LOGGER.debug("Ticket id [{}] could not be found", ticketId);
                return null;
            }

            val document = this.couchbase.getBucket().get(encTicketId, SerializableDocument.class);
            if (document != null) {
                val t = (Ticket) document.content();
                LOGGER.debug("Got ticket [{}] from the registry.", t);

                val decoded = decodeTicket(t);
                if (predicate.test(decoded)) {
                    return decoded;
                }
                return null;
            }
            LOGGER.debug("Ticket [{}] not found in the registry.", encTicketId);
            return null;
        } catch (final Exception e) {
            LOGGER.error("Failed fetching [{}]: [{}]", ticketId, e);
            return null;
        }
    }

    /**
     * Stops the couchbase client.
     */
    @SneakyThrows
    @Override
    public void destroy() {
        LOGGER.debug("Shutting down Couchbase");
        this.couchbase.shutdown();
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        return this.ticketCatalog.findAll().stream().flatMap(t -> getViewResultIteratorForPrefixedTickets(t.getPrefix() + '-').allRows().stream())
            .filter(row -> StringUtils.isNotBlank(row.id())).map(row -> {
                val ticket = (Ticket) row.document().content();
                LOGGER.debug("Got ticket [{}] from the registry.", ticket);
                return decodeTicket(ticket);
            }).map(decoded -> {
                if (decoded == null || decoded.isExpired()) {
                    LOGGER.warn("Ticket has expired or cannot be decoded");
                    return null;
                }
                return decoded;
            }).collect(Collectors.toList());
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
    public boolean deleteSingleTicket(final String ticketIdToDelete) {
        val ticketId = encodeTicketId(ticketIdToDelete);
        LOGGER.debug("Deleting ticket [{}]", ticketId);
        try {
            return this.couchbase.getBucket().remove(ticketId) != null;
        } catch (final Exception e) {
            LOGGER.error("Failed deleting [{}]: [{}]", ticketId, e);
            return false;
        }
    }

    @Override
    public long deleteAll() {
        val remove = (Consumer<? super ViewRow>) t -> this.couchbase.getBucket().remove(t.document());
        return this.ticketCatalog.findAll()
            .stream()
            .mapToLong(t -> {
                val it = getViewResultIteratorForPrefixedTickets(t.getPrefix() + '-').iterator();
                val count = getViewRowCountFromViewResultIterator(it);
                it.forEachRemaining(remove);
                return count;
            })
            .sum();
    }

    private int runQuery(final String prefix) {
        val iterator = getViewResultIteratorForPrefixedTickets(prefix).iterator();
        return getViewRowCountFromViewResultIterator(iterator);
    }

    private ViewResult getViewResultIteratorForPrefixedTickets(final String prefix) {
        LOGGER.debug("Running query on document [{}] and view [{}] with prefix [{}]",
            UTIL_DOCUMENT, VIEW_NAME_ALL_TICKETS, prefix);
        return this.couchbase.getBucket().query(
            ViewQuery.from(UTIL_DOCUMENT, VIEW_NAME_ALL_TICKETS)
                .startKey(prefix)
                .endKey(prefix + END_TOKEN)
                .reduce());
    }
}

