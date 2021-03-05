package org.apereo.cas.ticket.registry;

import org.apereo.cas.couchbase.core.CouchbaseClientFactory;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.couchbase.client.java.codec.JacksonJsonSerializer;
import com.couchbase.client.java.codec.JsonTranscoder;
import com.couchbase.client.java.kv.GetOptions;
import com.couchbase.client.java.kv.UpsertOptions;
import com.couchbase.client.java.query.QueryOptions;
import com.couchbase.client.java.query.QueryResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
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
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    private final CouchbaseClientFactory couchbase;

    /**
     * Get the expiration policy value of the ticket in seconds.
     *
     * @param ticket the ticket
     * @return the exp value
     */
    private static Duration getTimeToLive(final Ticket ticket) {
        val ttl = ticket.getExpirationPolicy().getTimeToLive();
        if (ttl >= Integer.MAX_VALUE) {
            return Duration.ofSeconds(0);
        }
        val expTime = ttl.intValue();
        return Duration.ofSeconds(expTime);
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        LOGGER.debug("Adding ticket [{}]", ticketToAdd);
        try {
            val ticket = encodeTicket(ticketToAdd);
            LOGGER.debug("Created document for ticket [{}]. Upserting into bucket [{}]",
                ticketToAdd, couchbase.getBucket());
            this.couchbase.bucketUpsertDefaultCollection(ticket.getId(), ticket,
                UpsertOptions.upsertOptions()
                    .expiry(getTimeToLive(ticketToAdd))
                    .transcoder(JsonTranscoder.create(JacksonJsonSerializer.create(MAPPER))));
        } catch (final Exception e) {
            LOGGER.error("Failed adding [{}]", ticketToAdd);
            LoggingUtils.error(LOGGER, e);
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
            val document = couchbase.bucketGet(encTicketId, GetOptions.getOptions()
                .transcoder(JsonTranscoder.create(JacksonJsonSerializer.create(MAPPER))));
            if (document != null) {
                val ticket = document.contentAs(Ticket.class);
                LOGGER.debug("Got ticket [{}] from the registry.", ticket);
                val decoded = decodeTicket(ticket);
                if (predicate.test(decoded)) {
                    return decoded;
                }
                return null;
            }
        } catch (final Exception e) {
            LOGGER.error("Failed fetching [{}]", ticketId);
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public long deleteAll() {
        val query = getQueryForAllTickets();
        val count = couchbase.count(query);
        couchbase.remove(query);
        return count;
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        val results = queryForTickets();
        return results
            .rowsAs(Ticket.class)
            .stream()
            .map(ticket -> {
                LOGGER.debug("Found ticket [{}] from the registry. Decoding...", ticket);
                val decoded = decodeTicket(ticket);
                if (decoded == null || decoded.isExpired()) {
                    LOGGER.warn("Ticket has expired or cannot be decoded");
                    return null;
                }
                return decoded;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        addTicket(ticket);
        return ticket;
    }

    /**
     * Stops the couchbase client.
     */
    @Override
    public void destroy() {
        LOGGER.trace("Shutting down Couchbase");
        this.couchbase.shutdown();
    }

    @Override
    public long sessionCount() {
        return couchbase.count(String.format("prefix='%s'", TicketGrantingTicket.PREFIX));
    }

    @Override
    public long serviceTicketCount() {
        return couchbase.count(String.format("prefix='%s'", ServiceTicket.PREFIX));
    }

    @Override
    public boolean deleteSingleTicket(final String ticketIdToDelete) {
        val ticketId = encodeTicketId(ticketIdToDelete);
        LOGGER.trace("Deleting ticket [{}]", ticketId);
        return couchbase.bucketRemoveFromDefaultCollection(ticketId).isPresent();
    }

    private String getQueryForAllTickets() {
        return String.format("REGEX_CONTAINS(%s.`@class`, \".*Ticket.*\")", couchbase.getBucket());
    }

    private QueryResult queryForTickets() {
        val query = getQueryForAllTickets();
        return couchbase.select(query,
            QueryOptions.queryOptions().serializer(JacksonJsonSerializer.create(MAPPER)), false);
    }
}

