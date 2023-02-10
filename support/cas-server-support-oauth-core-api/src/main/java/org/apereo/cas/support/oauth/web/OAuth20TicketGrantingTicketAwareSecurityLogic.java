package org.apereo.cas.support.oauth.web;

import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.CookieUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.engine.DefaultSecurityLogic;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;
import org.pac4j.jee.context.JEEContext;

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

    @Override
    protected List<UserProfile> loadProfiles(final CallContext callContext, final ProfileManager manager, final List<Client> clients) {
        val request = ((JEEContext) callContext.webContext()).getNativeRequest();
        var ticketGrantingTicket = CookieUtils.getTicketGrantingTicketFromRequest(
            ticketGrantingTicketCookieGenerator, ticketRegistry, request);
        if (ticketGrantingTicket == null) {
            try {
                ticketGrantingTicket = manager.getProfile()
                    .map(profile -> profile.getAttribute(TicketGrantingTicket.class.getName()))
                    .map(ticketId -> ticketRegistry.getTicket(ticketId.toString(), TicketGrantingTicket.class))
                    .orElse(null);
            } catch (final Exception e) {
                LOGGER.trace("Cannot find active ticket-granting-ticket: [{}]", e.getMessage());
            }
        }

        if (ticketGrantingTicket == null) {
            LOGGER.debug("No ticket-granting-ticket/SSO session => return no pac4j user profiles to be in sync");
            return new ArrayList<>();
        }

        return super.loadProfiles(callContext, manager, clients);
    }
}
