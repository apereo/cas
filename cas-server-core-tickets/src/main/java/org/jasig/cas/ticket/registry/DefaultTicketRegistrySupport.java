package org.jasig.cas.ticket.registry;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * This is {@link DefaultTicketRegistrySupport}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
@Component("defaultAuthenticationSupport")
public class DefaultTicketRegistrySupport implements TicketRegistrySupport {

    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry = new DefaultTicketRegistry();

    /**
     * Instantiates a new Default ticket registry support.
     */
    public DefaultTicketRegistrySupport() {}

    @Override
    public Authentication getAuthenticationFrom(final String ticketGrantingTicketId) throws RuntimeException {
        final TicketGrantingTicket tgt = this.ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
        return tgt == null ? null : tgt.getAuthentication();
    }

    @Override
    public Principal getAuthenticatedPrincipalFrom(final String ticketGrantingTicketId) throws RuntimeException {
        final Authentication auth = getAuthenticationFrom(ticketGrantingTicketId);
        return auth == null ? null : auth.getPrincipal();
    }

    @Override
    public Map<String, Object> getPrincipalAttributesFrom(final String ticketGrantingTicketId) throws RuntimeException {
        final Principal principal = getAuthenticatedPrincipalFrom(ticketGrantingTicketId);
        return principal == null ? null : principal.getAttributes();
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
}
