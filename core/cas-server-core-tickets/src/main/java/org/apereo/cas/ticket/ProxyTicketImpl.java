package org.apereo.cas.ticket;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.proxy.ProxyTicket;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * The {@link ProxyTicketImpl} is a concrete implementation of the {@link ProxyTicket}.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Entity
@DiscriminatorValue(ProxyTicket.PROXY_TICKET_PREFIX)
public class ProxyTicketImpl extends ServiceTicketImpl implements ProxyTicket {
    private static final long serialVersionUID = -4469960563289285371L;

    /**
     * Instantiates a new Proxy ticket.
     */
    public ProxyTicketImpl() {
    }

    /**
     * Instantiates a new Proxy ticket.
     *
     * @param id                 the id
     * @param ticket             the ticket
     * @param service            the service
     * @param credentialProvided the credential that prompted this ticket. Could be false.
     * @param policy             the expiration policy
     */
    public ProxyTicketImpl(final String id, final TicketGrantingTicketImpl ticket, final Service service,
                           final boolean credentialProvided, final ExpirationPolicy policy) {
        super(id, ticket, service, credentialProvided, policy);
    }
}
