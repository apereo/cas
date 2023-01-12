package org.apereo.cas.support.pac4j.logout;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.factory.DelegatedTicketGrantingTicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.session.SessionStore;
import org.pac4j.core.logout.handler.LogoutHandler;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.factory.ProfileManagerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Predicate;

/**
 * The pac4j logout handler which triggers a CAS logout.
 *
 * @author Jerome LELEU
 * @since 7.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class TriggerCasSLOLogoutHandler implements LogoutHandler {

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final TicketRegistry ticketRegistry;

    private final ConfigurableApplicationContext applicationContext;

    @Override
    public void recordSession(final WebContext context, final SessionStore sessionStore, final String key) {
        context.setRequestAttribute(DelegatedTicketGrantingTicketFactory.DELEGATED_SESSION_KEY_REQUEST_ATTRIBUTE, key);
    }

    @Override
    public void destroySessionFront(final WebContext context, final SessionStore sessionStore,
                                    final ProfileManagerFactory profileManagerFactory, final String key) {
        triggerCasSLO(context, sessionStore, key);
    }

    @Override
    public void destroySessionBack(final WebContext context, final SessionStore sessionStore,
                                   final ProfileManagerFactory profileManagerFactory, final String key) {
        triggerCasSLO(context, sessionStore, key);
    }

    protected void triggerCasSLO(final WebContext context, final SessionStore sessionStore, final String key) {

        val profileManager = new ProfileManager(context, sessionStore);
        profileManager.removeProfiles();

        val requestContext = RequestContextHolder.getRequestContext();
        if (requestContext != null) {
            val externalContext = requestContext.getExternalContext();
            if (externalContext instanceof ServletExternalContext sec) {
                val request = (HttpServletRequest) sec.getNativeRequest();
                var tgtId = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
                LOGGER.debug("Found TGT cookie value: [{}]", tgtId);
                if (StringUtils.isBlank(tgtId)) {
                    val predicate = (Predicate<Ticket>) ticket -> ticket instanceof TicketGrantingTicketImpl
                                                        && !ticket.isExpired()
                                                        && StringUtils.equals(((TicketGrantingTicketImpl) ticket).getDelegatedSessionKey(), key);
                    val optTicket = ticketRegistry.getTickets(predicate).findFirst();
                    LOGGER.debug("Found TGT: [{}] for key: [{}]", optTicket, key);
                    if (optTicket.isPresent()) {
                        tgtId = optTicket.get().getId();
                    }
                }

                if (StringUtils.isNotBlank(tgtId)) {
                    LOGGER.debug("Performing CAS SLO for tgt: [{}]", tgtId);
                    try {
                        WebUtils.putTicketGrantingTicketInScopes(requestContext, tgtId);
                        val action = (Action) applicationContext.getBean(CasWebflowConstants.ACTION_ID_TERMINATE_SESSION);
                        action.execute(requestContext);
                    } catch (final Exception e) {
                        LOGGER.error("Failed to process CAS SLO", e);
                    }
                }
            }
        }
    }
}
