package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.proxy.ProxyGrantingTicket;
import org.jasig.cas.ticket.proxy.ProxyTicket;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Concrete implementation of a proxy granting ticket (PGT). A PGT is
 * used by a service to obtain proxy tickets for obtaining access to a back-end
 * service on behalf of a client. It is analogous to a ticket-granting ticket
 * but only for proxying purposes. Proxy tickets will be issued off of
 * a given proxy granting ticket.
 * <p>
 * NOTE: A PGT shares the same implementation as a {@link TicketGrantingTicket}.
 * The intension of this implementation at this point is make explicit and visible
 * the use and declaration of a PGT per the CAS protocol.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
@Entity
@DiscriminatorValue(ProxyGrantingTicket.PROXY_GRANTING_TICKET_PREFIX)
public final class ProxyGrantingTicketImpl extends TicketGrantingTicketImpl implements ProxyGrantingTicket {
    private static final long serialVersionUID = -8126909926138945649L;

    /**
     * Instantiates a new proxy granting ticket impl.
     */
    public ProxyGrantingTicketImpl() {
    }

    /**
     * Instantiates a new proxy granting ticket impl.
     *
     * @param id the id
     * @param authentication the authentication
     * @param policy the policy
     */
    public ProxyGrantingTicketImpl(final String id, final Authentication authentication, final ExpirationPolicy policy) {
        super(id, authentication, policy);
    }

    /**
     * Instantiates a new proxy granting ticket impl.
     *
     * @param id                   the id
     * @param proxiedBy            the proxied by
     * @param ticketGrantingTicket the ticket granting ticket
     * @param authentication       the authentication
     * @param policy               the policy
     */
    public ProxyGrantingTicketImpl(final String id, final Service proxiedBy, final TicketGrantingTicket ticketGrantingTicket,
                                   final Authentication authentication, final ExpirationPolicy policy) {
        super(id, proxiedBy, ticketGrantingTicket, authentication, policy);
    }

    @Override
    public ProxyTicket grantProxyTicket(final String id, final Service service, final ExpirationPolicy expirationPolicy,
                                        final boolean onlyTrackMostRecentSession) {
        final ProxyTicket serviceTicket = new ProxyTicketImpl(id, this,
                service, this.getCountOfUses() == 0,
                expirationPolicy);

        updateServiceAndTrackSession(serviceTicket.getId(), service, onlyTrackMostRecentSession);
        return serviceTicket;
    }

}
