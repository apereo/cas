package org.jasig.cas.ticket.registry;

import org.infinispan.Cache;
import org.jasig.cas.ticket.Ticket;

import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.HashSet;

/**
 * This is {@link InfinispanTicketRegistry}. Infinispan is a distributed in-memory key/value data store with optional schema.
 * It offers advanced functionality such as transactions, events, querying and distributed processing.
 * See <a href="http://infinispan.org/features/">http://infinispan.org/features/</a> for more info.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("infinispanTicketRegistry")
public final class InfinispanTicketRegistry extends AbstractDistributedTicketRegistry {

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
     * @param ticketToAdd The ticket we wish to add to the cache.
     */
    @Override
    public void addTicket(final Ticket ticketToAdd) {
        final Ticket ticket = encodeTicket(ticketToAdd);
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
        final String encTicketId = encodeTicketId(ticketId);
        if (ticketId == null) {
            return null;
        }
        final Ticket ticket = this.cache.get(encTicketId);
        return getProxiedTicketInstance(ticket);
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
        final Collection<Ticket> allTickets = new HashSet<>();
        for (final Ticket ticket : this.cache.values()) {
            final Ticket proxiedTicket = getProxiedTicketInstance(ticket);
            allTickets.add(proxiedTicket);
        }
        return decodeTickets(allTickets);
    }

    public void setCache(final Cache<String, Ticket> cache) {
        this.cache = cache;
    }
}
