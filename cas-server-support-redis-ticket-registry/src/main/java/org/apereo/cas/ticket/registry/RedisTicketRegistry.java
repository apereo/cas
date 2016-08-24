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
            this.client.delete(CAS_TICKET_PREFIX+ticketId);
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
            this.client.boundValueOps(CAS_TICKET_PREFIX + ticket.getId()).set(this.encodeTicket(ticket), getTimeout(ticket), TimeUnit.SECONDS);
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
            final Ticket t = this.client.boundValueOps(CAS_TICKET_PREFIX + ticketId).get();
            if (t != null) {
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
        Set<String> keys = this.client.keys(CAS_TICKET_PREFIX + "*");
        for (String key:keys){
            Ticket ticket = this.client.boundValueOps(key).get();
            if(ticket==null){
                this.client.delete(key);
            }else{
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
    public void destroy() throws Exception {
        client.getConnectionFactory().getConnection().close();
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
}