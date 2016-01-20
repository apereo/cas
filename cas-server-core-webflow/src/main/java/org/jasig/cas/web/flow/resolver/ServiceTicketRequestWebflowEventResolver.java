package org.jasig.cas.web.flow.resolver;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationResult;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.AbstractTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketCreationException;
import org.jasig.cas.web.flow.CasWebflowConstants;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link ServiceTicketRequestWebflowEventResolver}
 * that creates the next event responding to requests that are service-ticket requests.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("serviceTicketRequestWebflowEventResolver")
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
        final Service service = WebUtils.getService(context);
        return StringUtils.isNotBlank(context.getRequestParameters().get(CasProtocolConstants.PARAMETER_RENEW))
                && StringUtils.isNotBlank(ticketGrantingTicketId)
                && service != null;
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

        } catch (final AuthenticationException e) {
            return newEvent(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, e);
        } catch (final TicketCreationException e) {
            logger.warn("Invalid attempt to access service using renew=true with different credential. Ending SSO session.");
            this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketId);
        } catch (final AbstractTicketException e) {
            return newEvent(CasWebflowConstants.TRANSITION_ID_ERROR, e);
        }
        return newEvent(CasWebflowConstants.TRANSITION_ID_ERROR);

    }
}
