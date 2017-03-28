package org.apereo.cas.support.oauth.web;

import org.apereo.cas.logout.LogoutHandler;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link OAuth20LogoutHandler}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OAuth20LogoutHandler implements LogoutHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20LogoutHandler.class);
    private final ServicesManager servicesManager;
    private final TicketRegistry ticketRegistry;

    public OAuth20LogoutHandler(final ServicesManager servicesManager, final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
        this.servicesManager = servicesManager;
    }

    @Override
    public void handle(final TicketGrantingTicket ticketGrantingTicket) {
        ticketGrantingTicket.getDescendantTickets()
                .stream()
                .filter(t -> t.startsWith(AccessToken.PREFIX) || t.startsWith(RefreshToken.PREFIX))
                .forEach(t -> {
                    LOGGER.debug("Deleting ticket [{}] from the registry as a descendant of [{}]", t, ticketGrantingTicket.getId());
                    this.ticketRegistry.deleteTicket(t);
                });
    }
}
