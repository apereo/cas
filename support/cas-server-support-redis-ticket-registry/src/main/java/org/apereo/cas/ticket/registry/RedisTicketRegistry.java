package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Key-value ticket registry implementation that stores tickets in redis keyed on the ticket ID.
 *
 * @author serv
 * @since 5.1.0
 */
public class RedisTicketRegistry extends AbstractTicketRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisTicketRegistry.class);
    
    private static final String CAS_TICKET_PREFIX = "CAS_TICKET:";

    @NotNull
    private final RedisTemplate<String, Ticket> client;

    public RedisTicketRegistry(final RedisTemplate<String, Ticket> client) {
        this.client = client;
    }

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
            LOGGER.error("Failed to add [{}]", ticket);
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
        return this.client.keys(getPatternTicketRedisKey()).stream()
                .map(redisKey -> {
                    final Ticket ticket = this.client.boundValueOps(redisKey).get();
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
            final Ticket encodeTicket = this.encodeTicket(ticket);
            final String redisKey = getTicketRedisKey(ticket.getId());
            this.client.boundValueOps(redisKey).set(encodeTicket, getTimeout(ticket), TimeUnit.SECONDS);
            return encodeTicket;
        } catch (final Exception e) {
            LOGGER.error("Failed to update [{}]", ticket);
        }
        return null;
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
