package org.jasig.cas.ticket.registry;

import org.jasig.cas.ticket.proxy.ProxyTicket;

/**
 * This provides a wrapper for {@link ProxyTicket} so they can transparently reference the
 * {@link TicketRegistry} they were loaded from.
 *
 * @author Daniel Frett
 * @since 4.2.0
 */
public final class ProxyTicketDelegator extends ServiceTicketDelegator<ProxyTicket> implements ProxyTicket {
    private static final long serialVersionUID = 8458011748781011393L;

    /**
     * Instantiates a new service ticket delegator.
     *
     * @param ticketRegistry the ticket registry
     * @param serviceTicket  the service ticket
     * @param callback       the callback
     */
    ProxyTicketDelegator(final AbstractDistributedTicketRegistry ticketRegistry, final ProxyTicket serviceTicket,
                         final boolean callback) {
        super(ticketRegistry, serviceTicket, callback);
    }
}
