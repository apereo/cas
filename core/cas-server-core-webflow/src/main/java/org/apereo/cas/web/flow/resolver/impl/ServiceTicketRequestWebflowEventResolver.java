package org.apereo.cas.web.flow.resolver.impl;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collections;
import java.util.Set;

/**
 * This is {@link ServiceTicketRequestWebflowEventResolver}
 * that creates the next event responding to requests that are service-ticket requests.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ServiceTicketRequestWebflowEventResolver extends AbstractCasWebflowEventResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceTicketRequestWebflowEventResolver.class);
    
    public ServiceTicketRequestWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                    final CentralAuthenticationService centralAuthenticationService, final ServicesManager servicesManager,
                                                    final TicketRegistrySupport ticketRegistrySupport, final CookieGenerator warnCookieGenerator,
                                                    final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                    final MultifactorAuthenticationProviderSelector selector) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport, warnCookieGenerator,
                authenticationSelectionStrategies, selector);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        if (isRequestAskingForServiceTicket(context)) {
            LOGGER.debug("Authentication request is asking for service tickets");
            return Collections.singleton(grantServiceTicket(context));
        }
        return null;
    }

    /**
     * Is request asking for service ticket?
     *
     * @param context the context
     * @return true, if both service and tgt are found, and the request is not asking to renew.
     * @since 4.1.0
     */
    protected boolean isRequestAskingForServiceTicket(final RequestContext context) {
        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        LOGGER.debug("Located ticket-granting ticket [{}] from the request context", ticketGrantingTicketId);

        final Service service = WebUtils.getService(context);
        LOGGER.debug("Located service [{}] from the request context", service);

        final String renewParam = context.getRequestParameters().get(CasProtocolConstants.PARAMETER_RENEW);
        LOGGER.debug("Provided value for [{}] request parameter is [{}]", CasProtocolConstants.PARAMETER_RENEW, renewParam);

        if (StringUtils.isNotBlank(renewParam) && StringUtils.isNotBlank(ticketGrantingTicketId) && service != null) {
            LOGGER.debug("Request identifies itself as one asking for service tickets. Checking for authentication context validity...");
            final boolean validAuthn = ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicketId) != null;
            if (validAuthn) {
                LOGGER.debug("Existing authentication context linked to ticket-granting ticket [{}] is valid. "
                        + "CAS should begin to issue service tickets for [{}] once credentials are renewed", ticketGrantingTicketId, service);
                return false;
            }
            LOGGER.debug("Existing authentication context linked to ticket-granting ticket [{}] is NOT valid. "
                            + "CAS will not issue service tickets for [{}] just yet without renewing the authentication context",
                    ticketGrantingTicketId, service);
            return false;
        }
        LOGGER.debug("Request is not eligible to be issued service tickets just yet");
        return false;
    }

    /**
     * Grant service ticket for the given credential based on the service and tgt
     * that are found in the request context.
     *
     * @param context the context
     * @return the resulting event. Warning, authentication failure or error.
     * @since 4.1.0
     */
    protected Event grantServiceTicket(final RequestContext context) {
        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        final Credential credential = getCredentialFromContext(context);

        try {
            final Service service = WebUtils.getService(context);
            final AuthenticationResult authenticationResult =
                    this.authenticationSystemSupport.handleAndFinalizeSingleAuthenticationTransaction(service, credential);

            final ServiceTicket serviceTicketId = this.centralAuthenticationService.grantServiceTicket(ticketGrantingTicketId, service, authenticationResult);
            WebUtils.putServiceTicketInRequestScope(context, serviceTicketId);
            WebUtils.putWarnCookieIfRequestParameterPresent(this.warnCookieGenerator, context);
            return newEvent(CasWebflowConstants.TRANSITION_ID_WARN);

        } catch (final AuthenticationException | AbstractTicketException e) {
            return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, e);
        }
    }
}
