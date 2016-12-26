package org.apereo.cas.web.flow.resolver.impl;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.AbstractTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link ServiceTicketRequestWebflowEventResolver}
 * that creates the next event responding to requests that are service-ticket requests.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ServiceTicketRequestWebflowEventResolver extends AbstractCasWebflowEventResolver {
    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        if (isRequestAskingForServiceTicket(context)) {
            return ImmutableSet.of(grantServiceTicket(context));
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
        logger.debug("Located ticket-granting ticket [{}] from the request context", ticketGrantingTicketId);

        final Service service = WebUtils.getService(context);
        logger.debug("Located service [{}] from the request context", service);

        final String renewParam = context.getRequestParameters().get(CasProtocolConstants.PARAMETER_RENEW);
        logger.debug("Provided value for [{}] request parameter is [{}]", CasProtocolConstants.PARAMETER_RENEW, renewParam);

        if (StringUtils.isNotBlank(renewParam) && StringUtils.isNotBlank(ticketGrantingTicketId) && service != null) {
            logger.debug("Request identifies itself as one asking for service tickets. Checking for authentication context validity...");
            final boolean validAuthn = ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicketId) != null;
            if (validAuthn) {
                logger.debug("Existing authentication context linked to ticket-granting ticket [{}] is valid. "
                        + "CAS should begin to issue service tickets for [{}] once credentials are renewed", ticketGrantingTicketId, service);
                return false;
            }
            logger.debug("Existing authentication context linked to ticket-granting ticket [{}] is NOT valid. "
                            + "CAS will not issue service tickets for [{}] just yet without renewing the authentication context",
                    ticketGrantingTicketId, service);
            return false;
        } else {
            logger.debug("Request is not eligible to be issued service tickets just yet");
            return false;
        }
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

            final ServiceTicket serviceTicketId = this.centralAuthenticationService.grantServiceTicket(
                    ticketGrantingTicketId, service, authenticationResult);
            WebUtils.putServiceTicketInRequestScope(context, serviceTicketId);
            WebUtils.putWarnCookieIfRequestParameterPresent(this.warnCookieGenerator, context);
            return newEvent(CasWebflowConstants.TRANSITION_ID_WARN);

        } catch (final AuthenticationException | AbstractTicketException e) {
            return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, e);
        }
    }
}
