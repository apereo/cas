package org.jasig.cas.ticket.registry;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;

import java.util.List;
import java.util.Map;

/**
 * This is {@link TicketGrantingTicketDelegator}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class TicketGrantingTicketDelegator<T extends TicketGrantingTicket> extends AbstractTicketDelegator<T>
        implements TicketGrantingTicket {

    private static final long serialVersionUID = 5312560061970601497L;

    /**
     * Instantiates a new ticket granting ticket delegator.
     *
     * @param ticketRegistry the ticket registry
     * @param ticketGrantingTicket the ticket granting ticket
     * @param callback the callback
     */
    TicketGrantingTicketDelegator(final AbstractDistributedTicketRegistry ticketRegistry,
                                  final T ticketGrantingTicket, final boolean callback) {
        super(ticketRegistry, ticketGrantingTicket, callback);
    }

    @Override
    public Authentication getAuthentication() {
        return getTicket().getAuthentication();
    }

    @Override
    public Service getProxiedBy() {
        return getTicket().getProxiedBy();
    }

    @Override
    public List<Authentication> getSupplementalAuthentications() {
        return getTicket().getSupplementalAuthentications();
    }

    @Override
    public ServiceTicket grantServiceTicket(final String id, final Service service,
                                            final ExpirationPolicy expirationPolicy, final boolean credentialsProvided,
                                            final boolean onlyTrackMostRecentSession) {
        final ServiceTicket t = this.getTicket().grantServiceTicket(id, service,
                expirationPolicy, credentialsProvided, onlyTrackMostRecentSession);
        updateTicket();
        return t;
    }

    @Override
    public void markTicketExpired() {
        this.getTicket().markTicketExpired();
        updateTicket();
    }

    @Override
    public boolean isRoot() {
        return getTicket().isRoot();
    }

    @Override
    public TicketGrantingTicket getRoot() {
        return getTicket().getRoot();
    }

    @Override
    public List<Authentication> getChainedAuthentications() {
        return getTicket().getChainedAuthentications();
    }

    @Override
    public Map<String, Service> getServices() {
        return this.getTicket().getServices();
    }

    @Override
    public void removeAllServices() {
        this.getTicket().removeAllServices();
    }
}
