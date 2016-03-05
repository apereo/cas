package org.jasig.cas.extension.clearpass;

import org.jasig.cas.ticket.registry.TicketRegistryState;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.AbstractTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Map;

/**
 * Decorator that captures tickets and attempts to map them.
 *
 * @deprecated As of 4.1, use {@link org.jasig.cas.authentication.CacheCredentialsMetaDataPopulator} instead.
 * @author Scott Battaglia
 * @since 1.0.7
 */
@Deprecated
public final class TicketRegistryDecorator extends AbstractTicketRegistry {

    /** The real instance of the ticket registry that is to be decorated. */
    @NotNull
    private final TicketRegistry ticketRegistry;

    /** Map instance where credentials are stored. */
    @NotNull
    private final Map<String, String> cache;

    /**
     * Constructs an instance of the decorator wrapping the real ticket registry instance inside.
     *
     * @param actualTicketRegistry The real instance of the ticket registry that is to be decorated
     * @param cache Map instance where credentials are stored.
     *
     * @see EhcacheBackedMap
     */
    public TicketRegistryDecorator(final TicketRegistry actualTicketRegistry, final Map<String, String> cache) {
        this.ticketRegistry = actualTicketRegistry;
        this.cache = cache;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        if (ticket instanceof TicketGrantingTicket) {
            final TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) ticket;
            final String ticketId = ticketGrantingTicket.getId();
            final String userName = ticketGrantingTicket.getAuthentication().getPrincipal().getId().toLowerCase();

            logger.debug("Creating mapping ticket {} to user name {}", ticketId, userName);

            this.cache.put(ticketId, userName);
        }

        this.ticketRegistry.addTicket(ticket);
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        return this.ticketRegistry.getTicket(ticketId);
    }

    @Override
    public Collection<Ticket> getTickets() {
        return this.ticketRegistry.getTickets();
    }

    @Override
    public long sessionCount() {
        if (this.ticketRegistry instanceof TicketRegistryState) {
            return ((TicketRegistryState) this.ticketRegistry).sessionCount();
        }
        logger.debug("Ticket registry {} does not report the sessionCount() operation of the registry state.",
                this.ticketRegistry.getClass().getName());
        return super.sessionCount();
    }

    @Override
    public long serviceTicketCount() {
        if (this.ticketRegistry instanceof TicketRegistryState) {
            return ((TicketRegistryState) this.ticketRegistry).serviceTicketCount();
        }
        logger.debug("Ticket registry {} does not report the serviceTicketCount() operation of the registry state.",
                this.ticketRegistry.getClass().getName());
        return super.serviceTicketCount();
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final String userName = this.cache.get(ticketId);

        if (userName != null) {
            logger.debug("Removing mapping ticket {} for user name {}", ticketId, userName);
            this.cache.remove(userName);
        }

        return this.ticketRegistry.deleteTicket(ticketId);
    }

    @Override
    protected void updateTicket(final Ticket ticket) {
        addTicket(ticket);
    }

    @Override
    protected boolean needsCallback() {
        return false;
    }
}
