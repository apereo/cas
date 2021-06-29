package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.LoggingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

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

    @Override
    @SuppressWarnings("java:S2583")
    public long deleteAll() {
        val redisKeys = client.keys(getPatternTicketRedisKey());
        val size = Objects.requireNonNull(redisKeys).size();
        this.client.delete(redisKeys);
        return size;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        val redisKey = getTicketRedisKey(encodeTicketId(ticketId));
        this.client.delete(redisKey);
        return true;
    }

    @Override
    public void addTicketInternal(final Ticket ticket) {
        try {
            LOGGER.debug("Adding ticket [{}]", ticket);
            val redisKey = getTicketRedisKey(encodeTicketId(ticket.getId()));
            val encodeTicket = encodeTicket(ticket);
            val timeout = getTimeout(ticket);
            this.client.boundValueOps(redisKey).set(encodeTicket, timeout, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.error("Failed to add [{}]", ticket);
            LoggingUtils.error(LOGGER, e);
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
                LOGGER.trace("The condition enforced by [{}] cannot successfully accept/test the ticket id [{}]", ticketId,
                    predicate.getClass().getSimpleName());
                return null;
            }
        } catch (final Exception e) {
            LOGGER.error("Failed fetching [{}]", ticketId);
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public Collection<? extends Ticket> getTickets() {
        try (val ticketsStream = stream()) {
            return ticketsStream.collect(Collectors.toSet());
        }
    }

    @Override
    public Stream<? extends Ticket> stream() {
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
            .map(this::decodeTicket)
            .filter(Objects::nonNull);
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        try {
            LOGGER.debug("Updating ticket [{}]", ticket);
            val encodeTicket = this.encodeTicket(ticket);
            val redisKey = getTicketRedisKey(encodeTicketId(ticket.getId()));
            LOGGER.debug("Fetched redis key [{}] for ticket [{}]", redisKey, ticket);

            val timeout = getTimeout(ticket);
            client.boundValueOps(redisKey).set(encodeTicket, timeout, TimeUnit.SECONDS);
            return encodeTicket;
        } catch (final Exception e) {
            LOGGER.error("Failed to update [{}]", ticket);
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

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

    /**
     * Get a stream of all CAS-related keys from Redis DB.
     *
     * @return stream of all CAS-related keys from Redis DB
     */
    private Stream<String> getKeysStream() {
        val cursor = Objects.requireNonNull(client.getConnectionFactory()).getConnection()
            .scan(ScanOptions.scanOptions().match(getPatternTicketRedisKey()).build());
        return StreamSupport
            .stream(Spliterators.spliteratorUnknownSize(cursor, Spliterator.ORDERED), false)
            .map(key -> (String) client.getKeySerializer().deserialize(key))
            .collect(Collectors.toSet())
            .stream()
        .onClose(() -> IOUtils.closeQuietly(cursor));
    }
}
