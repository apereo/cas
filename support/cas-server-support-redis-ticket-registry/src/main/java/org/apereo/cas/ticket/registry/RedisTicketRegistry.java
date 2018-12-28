package org.apereo.cas.ticket.registry;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.ticket.Ticket;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Key-value ticket registry implementation that stores tickets in redis keyed on the ticket ID.
 *
 * @author serv
 * @since 5.1.0
 */
@Slf4j
@AllArgsConstructor
public class RedisTicketRegistry extends AbstractTicketRegistry {
    private static final String CAS_TICKET_PREFIX = "CAS_TICKET:";
    private static final long SCAN_COUNT = 100L;

    @NotNull
    private final RedisTemplate<String, Ticket> client;

    @Override
    public long deleteAll() {
        final Set<String> redisKeys = this.client.keys(getPatternTicketRedisKey());
        final int size = redisKeys.size();
        this.client.delete(redisKeys);
        return size;
    }
    
    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        try {
            final String redisKey = getTicketRedisKey(ticketId);
            this.client.delete(redisKey);
            return true;
        } catch (final Exception e) {
            LOGGER.error("Ticket not found or is already removed. Failed deleting [{}]", ticketId, e);
        }
        return false;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        try {
            LOGGER.debug("Adding ticket [{}]", ticket);
            final String redisKey = getTicketRedisKey(ticket.getId());
            // Encode first, then add
            final Ticket encodeTicket = this.encodeTicket(ticket);
            this.client.boundValueOps(redisKey)
                    .set(encodeTicket, getTimeout(ticket), TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.error("Failed to add [{}]", ticket, e);
        }
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        try {
            final String redisKey = getTicketRedisKey(ticketId);
            final Ticket t = this.client.boundValueOps(redisKey).get();
            if (t != null) {
                final Ticket result = decodeTicket(t);
                if (result != null && result.isExpired()) {
                    LOGGER.debug("Ticket [{}] has expired and is now removed from the cache", result.getId());
                    deleteSingleTicket(ticketId);
                    return null;
                }
                return result;
            }
        } catch (final Exception e) {
            LOGGER.error("Failed fetching [{}] ", ticketId, e);
        }
        return null;
    }

    @Override
    public Collection<Ticket> getTickets() {
        try (Stream<Ticket> ticketsStream = getTicketsStream()) {
            return ticketsStream.collect(Collectors.toSet());
        }
    }

    @Override
    public Stream<Ticket> getTicketsStream() {
        return getKeysStream()
                .map(redisKey -> {
                    final Ticket ticket = this.client.boundValueOps(redisKey).get();
                    if (ticket == null) {
                        this.client.delete(redisKey);
                        return null;
                    }
                    return ticket;
                })
                .filter(Objects::nonNull)
                .map(this::decodeTicket);

    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        try {
            LOGGER.debug("Updating ticket [{}]", ticket);
            final Ticket encodeTicket = this.encodeTicket(ticket);
            final String redisKey = getTicketRedisKey(ticket.getId());
            this.client.boundValueOps(redisKey).set(encodeTicket, getTimeout(ticket), TimeUnit.SECONDS);
            return encodeTicket;
        } catch (final Exception e) {
            LOGGER.error("Failed to update [{}]", ticket, e);
        }
        return null;
    }

    /**
     * Get a stream of all CAS-related keys from Redis DB.
     *
     * @return stream of all CAS-related keys from Redis DB
     */
    private Stream<String> getKeysStream() {
        final Cursor<byte[]> cursor =
                client
                        .getConnectionFactory()
                        .getConnection()
                        .scan(ScanOptions
                                .scanOptions()
                                .match(getPatternTicketRedisKey())
                                .count(SCAN_COUNT)
                                .build());
        return StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(cursor, Spliterator.ORDERED), false)
                .map(key -> (String) client.getKeySerializer().deserialize(key))
                .onClose(() -> {
                    try {
                        cursor.close();
                    } catch (final IOException e) {
                        LOGGER.error("Could not close Redis connection", e);
                    }
                });
    }

    /**
     * If not time out value is specified, expire the ticket immediately.
     *
     * @param ticket the ticket
     * @return timeout
     */
    private static int getTimeout(final Ticket ticket) {
        final int ttl = ticket.getExpirationPolicy().getTimeToLive().intValue();
        if (ttl == 0) {
            return 1;
        }
        return ttl;
    }

    // Add a prefix as the key of redis
    private static String getTicketRedisKey(final String ticketId) {
        return CAS_TICKET_PREFIX + ticketId;
    }

    // pattern all ticket redisKey
    private static String getPatternTicketRedisKey() {
        return CAS_TICKET_PREFIX + "*";
    }
}
