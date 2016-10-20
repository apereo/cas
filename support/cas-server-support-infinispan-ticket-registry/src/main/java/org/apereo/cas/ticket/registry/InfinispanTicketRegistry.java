package org.apereo.cas.ticket.registry;

import org.apereo.cas.ticket.Ticket;
import org.infinispan.Cache;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link InfinispanTicketRegistry}. Infinispan is a distributed in-memory key/value data store with optional schema.
 * It offers advanced functionality such as transactions, events, querying and distributed processing.
 * See <a href="http://infinispan.org/features/">http://infinispan.org/features/</a> for more info.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class InfinispanTicketRegistry extends AbstractTicketRegistry {
    
    private Cache cache;

    /**
     * Instantiates a new Infinispan ticket registry.
     */
    public InfinispanTicketRegistry() {
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        logger.info("Setting up Infinispan Ticket Registry...");
    }
    
    @Override
    public void updateTicket(final Ticket ticket) {
        this.cache.put(ticket.getId(), ticket);
    }
    
    @Override
    public void addTicket(final Ticket ticketToAdd) {
        final Ticket ticket = encodeTicket(ticketToAdd);
        this.cache.put(ticket.getId(), ticket, 
                ticket.getExpirationPolicy().getTimeToLive(), TimeUnit.SECONDS,
                ticket.getExpirationPolicy().getTimeToIdle(), TimeUnit.SECONDS);
    }
    
    @Override
    public Ticket getTicket(final String ticketId) {
        final String encTicketId = encodeTicketId(ticketId);
        if (ticketId == null) {
            return null;
        }
        final Ticket ticket = Ticket.class.cast(cache.get(encTicketId));
        return ticket;
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        this.cache.evict(ticketId);
        return getTicket(ticketId) == null;
    }

    /**
     *
     * Retrieve all tickets from the registry.
     *
     * Note! Usage of this method can be computational and I/O intensive and should not be used for other than
     * debugging.
     *
     * @return collection of tickets currently stored in the registry. Tickets
     *         might or might not be valid i.e. expired.
     */
    @Override
    public Collection<Ticket> getTickets() {
        return decodeTickets(this.cache.values());
    }

    public void setCache(final Cache<String, Ticket> cache) {
        this.cache = cache;
    }
}
