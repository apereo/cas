package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
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
@RequiredArgsConstructor
public class RedisTicketRegistry extends AbstractTicketRegistry {
    private static final String CAS_TICKET_PREFIX = "CAS_TICKET:";

    private final RedisTemplate<String, Ticket> client;

    /**
     * If not time out value is specified, expire the ticket immediately.
     *
     * @param ticket the ticket
     * @return timeout
     */
    private static Long getTimeout(final Ticket ticket) {
        val ttl = ticket.getExpirationPolicy().getTimeToLive();
        if (ttl > Integer.MAX_VALUE) {
            return (long) Integer.MAX_VALUE;
        } else if (ttl <= 0) {
            return 1L;
        }
        return ttl;
    }

    private static String getTicketRedisKey(final String ticketId) {
        return CAS_TICKET_PREFIX + ticketId;
    }

    private static String getPatternTicketRedisKey() {
        return CAS_TICKET_PREFIX + '*';
    }

    @Override
    @SuppressWarnings("java:S2583")
    public long deleteAll() {
        val redisKeys = this.client.keys(getPatternTicketRedisKey());
        if (redisKeys == null) {
            LOGGER.warn("Unable to locate tickets via redis key");
            return 0;
        }
        val size = redisKeys.size();
        this.client.delete(redisKeys);
        return size;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        try {
            val redisKey = getTicketRedisKey(encodeTicketId(ticketId));
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
            val redisKey = getTicketRedisKey(encodeTicketId(ticket.getId()));
            val encodeTicket = encodeTicket(ticket);
            val timeout = getTimeout(ticket);
            this.client.boundValueOps(redisKey).set(encodeTicket, timeout, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.error("Failed to add [{}]", ticket, e);
        }
    }

    @Override
    public Ticket getTicket(final String ticketId, final Predicate<Ticket> predicate) {
        try {
            val redisKey = getTicketRedisKey(encodeTicketId(ticketId));
            val t = this.client.boundValueOps(redisKey).get();
            if (t != null) {
                val result = decodeTicket(t);
                if (predicate.test(result)) {
                    return result;
                }
                LOGGER.debug("The condition enforced by the predicate [{}] cannot successfully accept/test the ticket id [{}]", ticketId,
                        predicate.getClass().getSimpleName());
                return null;
            }
        } catch (final Exception e) {
            LOGGER.error("Failed fetching [{}] ", ticketId, e);
        }
        return null;
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        try (val ticketsStream = getTicketsStream()) {
            return ticketsStream.collect(Collectors.toSet());
        }
    }

    @Override
    public Stream<? extends Ticket> getTicketsStream() {
        return getKeysStream()
            .map(redisKey -> {
                val ticket = this.client.boundValueOps(redisKey).get();
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
            val encodeTicket = this.encodeTicket(ticket);
            val redisKey = getTicketRedisKey(encodeTicketId(ticket.getId()));
            LOGGER.debug("Fetched redis key [{}] for ticket [{}]", redisKey, ticket);

            val timeout = getTimeout(ticket);
            this.client.boundValueOps(redisKey).set(encodeTicket, timeout, TimeUnit.SECONDS);
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
        val cursor = client.getConnectionFactory().getConnection()
                .scan(ScanOptions.scanOptions().match(getPatternTicketRedisKey())
                .build());
        return StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(cursor, Spliterator.ORDERED), false)
            .map(key -> (String) client.getKeySerializer().deserialize(key))
            .collect(Collectors.toSet())
            .stream()
            .onClose(() -> {
                try {
                    cursor.close();
                } catch (final IOException e) {
                    LOGGER.error("Could not close Redis connection", e);
                }
            });
    }
}
