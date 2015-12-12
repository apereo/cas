package org.jasig.cas.ticket.registry;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyTicket;

/**
 * This provides a wrapper for {@link ProxyGrantingTicket} so they can transparently reference the
 * {@link TicketRegistry} they were loaded from.
 *
 * @author Daniel Frett
 * @since 4.2.0
 */
public final class ProxyGrantingTicketDelegator extends TicketGrantingTicketDelegator<ProxyGrantingTicket> implements
        ProxyGrantingTicket {
    private static final long serialVersionUID = 684505809948112983L;

    /**
     * Instantiates a new proxy granting ticket delegator.
     *
     * @param ticketRegistry       the ticket registry
     * @param ticketGrantingTicket the proxy granting ticket
     * @param callback             the callback
     */
    ProxyGrantingTicketDelegator(final AbstractDistributedTicketRegistry ticketRegistry,
                                 final ProxyGrantingTicket ticketGrantingTicket, final boolean callback) {
        super(ticketRegistry, ticketGrantingTicket, callback);
    }

    @Override
    public ProxyTicket grantProxyTicket(final String id, final Service service, final ExpirationPolicy expirationPolicy,
                                        final boolean onlyTrackMostRecentSession) {
        final ProxyTicket t = this.getTicket().grantProxyTicket(id, service, expirationPolicy,
                onlyTrackMostRecentSession);
        updateTicket();
        return t;
    }
}
