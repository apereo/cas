package org.jasig.cas.ticket.registry;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;

/**
 * This is {@link ServiceTicketDelegator}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class ServiceTicketDelegator<T extends ServiceTicket> extends AbstractTicketDelegator<T>
        implements ServiceTicket {

    private static final long serialVersionUID = 8160636219307822967L;

    /**
     * Instantiates a new service ticket delegator.
     *
     * @param ticketRegistry the ticket registry
     * @param serviceTicket the service ticket
     * @param callback the callback
     */
    ServiceTicketDelegator(final AbstractDistributedTicketRegistry ticketRegistry,
                           final T serviceTicket, final boolean callback) {
        super(ticketRegistry, serviceTicket, callback);
    }

    @Override
    public Service getService() {
        return getTicket().getService();
    }

    @Override
    public boolean isFromNewLogin() {
        return getTicket().isFromNewLogin();
    }

    @Override
    public boolean isValidFor(final Service service) {
        final boolean b = this.getTicket().isValidFor(service);
        updateTicket();
        return b;
    }

    @Override
    public ProxyGrantingTicket grantProxyGrantingTicket(final String id,
                                                        final Authentication authentication,
                                                        final ExpirationPolicy expirationPolicy) {
        final ProxyGrantingTicket t = this.getTicket().grantProxyGrantingTicket(id,
                authentication, expirationPolicy);
        updateTicket();
        return t;
    }
}
