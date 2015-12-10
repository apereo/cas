package org.jasig.cas.ticket.registry;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;

/**
 * Abstract Implementation that handles some of the commonalities between
 * distributed ticket registries.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public abstract class AbstractDistributedTicketRegistry extends AbstractTicketRegistry {

    /**
     * Update the received ticket.
     *
     * @param ticket the ticket
     */
    protected abstract void updateTicket(Ticket ticket);

    /**
     * Whether or not a callback to the TGT is required when checking for expiration.
     *
     * @return true, if successful
     */
    protected abstract boolean needsCallback();

    /**
     * Gets the proxied ticket instance.
     *
     * @param ticket the ticket
     * @return the proxied ticket instance
     */
    protected final Ticket getProxiedTicketInstance(final Ticket ticket) {
        if (ticket == null) {
            return null;
        }

        if (ticket instanceof TicketGrantingTicket) {
            return new TicketGrantingTicketDelegator(this, (TicketGrantingTicket) ticket, needsCallback());
        }

        return new ServiceTicketDelegator(this, (ServiceTicket) ticket, needsCallback());
    }

}
