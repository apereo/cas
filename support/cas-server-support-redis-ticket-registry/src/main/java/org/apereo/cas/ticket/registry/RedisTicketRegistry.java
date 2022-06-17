package org.apereo.cas.ticket.registry;

import org.apereo.cas.redis.core.CasRedisTemplate;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.util.LoggingUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    private final CasRedisTemplate<String, Ticket> client;

    private final long scanCount;

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

    private static String getTicketRedisKey(final String ticketId, final String user) {
        return CAS_TICKET_PREFIX
               + StringUtils.defaultIfBlank(ticketId.trim(), "*")
               + ':'
               + StringUtils.defaultIfBlank(user.trim(), "*");
    }

    private static String getPatternTicketRedisKey() {
        return CAS_TICKET_PREFIX + '*';
    }

    @Override
    @SuppressWarnings("java:S2583")
    public long deleteAll() {
        val redisKeys = getKeysStream().collect(Collectors.toSet());
        val size = Objects.requireNonNull(redisKeys).size();
        this.client.delete(redisKeys);
        return size;
    }

    @Override
    public long deleteSingleTicket(final String ticketId) {
        val redisKey = getTicketRedisKey(encodeTicketId(ticketId), StringUtils.EMPTY);
        return getKeysStream(redisKey).mapToInt(id -> BooleanUtils.toBoolean(client.delete(id)) ? 1 : 0).sum();
    }

    @Override
    public void addTicketInternal(final Ticket ticket) {
        try {
            LOGGER.debug("Adding ticket [{}]", ticket);
            val userId = getPrincipalIdFrom(ticket);
            val redisKey = getTicketRedisKey(encodeTicketId(ticket.getId()), encodeTicketId(userId));
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
            val redisKey = getTicketRedisKey(encodeTicketId(ticketId), StringUtils.EMPTY);
            return getKeysStream(redisKey)
                .map(key -> client.boundValueOps(key).get())
                .filter(Objects::nonNull)
                .map(this::decodeTicket)
                .filter(predicate)
                .findFirst()
                .orElse(null);
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
            val encodeTicket = encodeTicket(ticket);

            val userId = getPrincipalIdFrom(ticket);
            val redisKey = getTicketRedisKey(encodeTicketId(ticket.getId()), encodeTicketId(userId));
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

    @Override
    public Stream<? extends Ticket> getSessionsFor(final String principalId) {
        val redisKey = getTicketRedisKey(StringUtils.EMPTY, encodeTicketId(principalId));
        return getKeysStream(redisKey)
            .map(key -> client.boundValueOps(key).get())
            .filter(Objects::nonNull)
            .map(this::decodeTicket)
            .filter(Objects::nonNull);
    }

    /**
     * Get a stream of all CAS-related keys from Redis DB.
     *
     * @return stream of all CAS-related keys from Redis DB
     */
    private Stream<String> getKeysStream() {
        return getKeysStream(getPatternTicketRedisKey());
    }

    private Stream<String> getKeysStream(final String key) {
        return client.keys(key, this.scanCount);
    }
}
