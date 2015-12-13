package org.jasig.cas.ticket.registry;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyTicket;

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

        if (ticket instanceof ProxyGrantingTicket) {
            return new ProxyGrantingTicketDelegator(this, (ProxyGrantingTicket) ticket, needsCallback());
        }

        if (ticket instanceof TicketGrantingTicket) {
            return new TicketGrantingTicketDelegator<>(this, (TicketGrantingTicket) ticket, needsCallback());
        }

        if (ticket instanceof ProxyTicket) {
            return new ProxyTicketDelegator(this, (ProxyTicket) ticket, needsCallback());
        }

        if (ticket instanceof ServiceTicket) {
            return new ServiceTicketDelegator<>(this, (ServiceTicket) ticket, needsCallback());
        }

        throw new IllegalStateException("Cannot wrap ticket of type: " + ticket.getClass() + " with a proxy delegator");
    }

}
