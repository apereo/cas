package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Key-value ticket registry implementation that stores tickets in redis keyed on the ticket ID.
 *
 * @author serv
 */
public class RedisTicketRegistry extends AbstractTicketRegistry {

    private static final String CAS_TICKET_PREFIX = "CAS_TICKET:";

    /**
     * redis client.
     */
    @NotNull
    private final TicketRedisTemplate client;


    public RedisTicketRegistry(final TicketRedisTemplate client) {
        this.client = client;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        try {
            Assert.notNull(this.client, "No redis client is defined.");
            String redisKey = this.getTicketRedisKey(ticketId);
            this.client.delete(redisKey);
            return true;
        } catch (final Exception e) {
            logger.error("Ticket not found or is already removed. Failed deleting {}", ticketId, e);
        }
        return false;
    }


    @Override
    public void addTicket(final Ticket ticket) {
        if (this.client == null) {
            logger.error("No redis client is configured.");
        }
        logger.debug("Adding ticket {}", ticket);
        try {
            String redisKey = this.getTicketRedisKey(ticket.getId());
            //Encode first, then add
            Ticket encodeTicket = this.encodeTicket(ticket);
            this.client.boundValueOps(redisKey)
                    .set(encodeTicket, getTimeout(ticket), TimeUnit.SECONDS);
        } catch (final Exception e) {
            logger.error("Failed to add {}", ticket);
        }
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        if (this.client == null) {
            logger.error("No redis client is configured.");
            return null;
        }

        try {
            String redisKey = this.getTicketRedisKey(ticketId);
            final Ticket t = this.client.boundValueOps(redisKey).get();
            if (t != null) {
                //Decoding add first
                return decodeTicket(t);
            }
        } catch (final Exception e) {
            logger.error("Failed fetching {} ", ticketId, e);
        }
        return null;
    }

    @Override
    public Collection<Ticket> getTickets() {
        if (this.client == null) {
            logger.error("No redis client is configured.");
            return null;
        }

        Set<Ticket> tickets = new HashSet<Ticket>();
        // ticket keys in the redis
        Set<String> redisKeys = this.client.keys(this.getPatternTicketRedisKey());
        for (String redisKey : redisKeys) {
            Ticket ticket = this.client.boundValueOps(redisKey).get();
            if (ticket == null) {
                this.client.delete(redisKey);
            } else {
                //Decoding add first
                tickets.add(this.decodeTicket(ticket));
            }
        }
        return tickets;
    }

    @Override
    public void updateTicket(final Ticket ticket) {
        addTicket(ticket);
    }

    @PreDestroy
    public void destroy() {
        try {
            client.getConnectionFactory().getConnection().close();
        } catch (Exception e) {
            logger.error("Failed destroy redis connection ", e);
        }


    }

    /**
     * If not time out value is specified, expire the ticket immediately.
     *
     * @param ticket the ticket
     * @return timeout in milliseconds.
     */
    private static int getTimeout(final Ticket ticket) {
        final int ttl = ticket.getExpirationPolicy().getTimeToLive().intValue();
        if (ttl == 0) {
            return 1;
        }
        return ttl;
    }

    //Add a prefix as the key of redis
    private String getTicketRedisKey(String ticketId) {
        return CAS_TICKET_PREFIX + ticketId;
    }

    // pattern all ticket redisKey
    private String getPatternTicketRedisKey() {
        return CAS_TICKET_PREFIX + "*";
    }
}