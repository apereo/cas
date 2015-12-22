package org.jasig.cas.ticket.registry;

import org.infinispan.Cache;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.encrypt.AbstractCrypticTicketRegistry;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;

/**
 * This is {@link InfinispanTicketRegistry}. Infinispan is a distributed in-memory key/value data store with optional schema.
 * It offers advanced functionality such as transactions, events, querying and distributed processing.
 * See <a href="http://infinispan.org/features/">http://infinispan.org/features/</a> for more info.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("infinispanTicketRegistry")
public final class InfinispanTicketRegistry extends AbstractCrypticTicketRegistry {

    @Resource(name="infinispanTicketsCache")
    private Cache<String, Ticket> cache;

    /**
     * Instantiates a new Infinispan ticket registry.
     */
    public InfinispanTicketRegistry() {
    }

    @Override
    protected void updateTicket(final Ticket ticket) {
        this.cache.put(ticket.getId(), ticket);
    }

    @Override
    protected boolean needsCallback() {
        return true;
    }

    /**
     * Add a ticket to the registry. Ticket storage is based on the ticket id.
     *
     * @param ticket The ticket we wish to add to the cache.
     */
    @Override
    public void addTicket(final Ticket ticket) {
        this.cache.put(ticket.getId(), ticket);
    }

    /**
     * Retrieve a ticket from the registry.
     *
     * @param ticketId the id of the ticket we wish to retrieve
     * @return the requested ticket.
     */
    @Override
    public Ticket getTicket(final String ticketId) {
        final Ticket ticket = this.cache.get(ticketId);
        return getProxiedTicketInstance(ticket);
    }

    /**
     * Remove a specific ticket from the registry.
     *
     * @param ticketId The id of the ticket to delete.
     * @return true if the ticket was removed and false if the ticket did not
     *         exist.
     */
    @Override
    public boolean deleteTicket(final String ticketId) {
        if (getTicket(ticketId) == null) {
            return false;
        }
        this.cache.evict(ticketId);
        return true;

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
        return this.cache.values();
    }

    public void setCache(final Cache<String, Ticket> cache) {
        this.cache = cache;
    }
}
