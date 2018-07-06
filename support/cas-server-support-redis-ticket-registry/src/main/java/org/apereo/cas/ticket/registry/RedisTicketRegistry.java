package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        if (ttl <= 0) {
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
    public long deleteAll() {
        val redisKeys = this.client.keys(getPatternTicketRedisKey());
        val size = redisKeys.size();
        this.client.delete(redisKeys);
        return size;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        try {
            val redisKey = getTicketRedisKey(ticketId);
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
            val redisKey = getTicketRedisKey(ticket.getId());
            val encodeTicket = encodeTicket(ticket);
            val timeout = getTimeout(ticket);
            this.client.boundValueOps(redisKey).set(encodeTicket, timeout.longValue(), TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.error("Failed to add [{}]", ticket, e);
        }
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        try {
            val redisKey = getTicketRedisKey(ticketId);
            val t = this.client.boundValueOps(redisKey).get();
            if (t != null) {
                val result = decodeTicket(t);
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
    public Collection<? extends Ticket> getTickets() {
        return this.client.keys(getPatternTicketRedisKey()).stream()
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
            .collect(Collectors.toSet());
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        try {
            LOGGER.debug("Updating ticket [{}]", ticket);
            val encodeTicket = this.encodeTicket(ticket);
            val redisKey = getTicketRedisKey(ticket.getId());
            val timeout = getTimeout(ticket);
            this.client.boundValueOps(redisKey).set(encodeTicket, timeout.longValue(), TimeUnit.SECONDS);
            return encodeTicket;
        } catch (final Exception e) {
            LOGGER.error("Failed to update [{}]", ticket, e);
        }
        return null;
    }
}
