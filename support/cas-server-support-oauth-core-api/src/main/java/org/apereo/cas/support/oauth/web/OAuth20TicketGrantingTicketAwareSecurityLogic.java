package org.apereo.cas.support.oauth.web;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Specific OAuth security logic to "synchronize the CAS and pac4j sessions".
 *
 * @author Jerome LELEU
 * @since 6.5.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20TicketGrantingTicketAwareSecurityLogic extends DefaultSecurityLogic {

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final TicketRegistry ticketRegistry;

    private final CentralAuthenticationService centralAuthenticationService;

    @Override
    protected List<UserProfile> loadProfiles(final ProfileManager manager, final WebContext context,
                                             final SessionStore sessionStore, final List<Client> clients) {

        var ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
            ticketGrantingTicketCookieGenerator, ticketRegistry, ((JEEContext) context).getNativeRequest());

        if (ticketGrantingTicket == null) {
            try {
                ticketGrantingTicket = sessionStore
                    .get(context, WebUtils.PARAMETER_TICKET_GRANTING_TICKET_ID)
                    .map(ticketId -> centralAuthenticationService.getTicket(ticketId.toString(), TicketGrantingTicket.class))
                    .orElse(null);
            } catch (final Exception e) {
                LOGGER.trace("Cannot find active ticket-granting-ticket: [{}]", e.getMessage());
            }
        }

        if (ticketGrantingTicket == null) {
            LOGGER.debug("No ticket-granting-ticket/SSO session => return no pac4j user profiles to be in sync");
            return new ArrayList<>();
        }

        return super.loadProfiles(manager, context, sessionStore, clients);
    }
}
