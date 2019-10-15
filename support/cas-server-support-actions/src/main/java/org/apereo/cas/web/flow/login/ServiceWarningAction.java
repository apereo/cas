package org.apereo.cas.web.flow.login;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link ServiceWarningAction}. Populates the view
 * with the target url of the application after the warning
 * screen is displayed.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class ServiceWarningAction extends AbstractAction {

    /**
     * Parameter name indicating that warning should be ignored and removed.
     */
    public static final String PARAMETER_NAME_IGNORE_WARNING = "ignorewarn";

    private final CentralAuthenticationService centralAuthenticationService;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final TicketRegistrySupport ticketRegistrySupport;
    private final CasCookieBuilder warnCookieGenerator;
    private final PrincipalElectionStrategy principalElectionStrategy;

    @Override
    protected Event doExecute(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        val service = WebUtils.getService(context);
        val ticketGrantingTicket = WebUtils.getTicketGrantingTicketId(context);
        if (StringUtils.isBlank(ticketGrantingTicket)) {
            throw new InvalidTicketException(new AuthenticationException("No ticket-granting ticket could be found in the context"), ticketGrantingTicket);
        }

        val authentication = this.ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicket);
        if (authentication == null) {
            throw new InvalidTicketException(new AuthenticationException("No authentication found for ticket " + ticketGrantingTicket), ticketGrantingTicket);
        }

        val credential = WebUtils.getCredential(context);
        val authenticationResultBuilder =
            authenticationSystemSupport.establishAuthenticationContextFromInitial(authentication, credential);
        val authenticationResult = authenticationResultBuilder.build(principalElectionStrategy, service);

        val serviceTicketId = this.centralAuthenticationService.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
        WebUtils.putServiceTicketInRequestScope(context, serviceTicketId);

        if (request.getParameterMap().containsKey(PARAMETER_NAME_IGNORE_WARNING)) {
            if (Boolean.parseBoolean(request.getParameter(PARAMETER_NAME_IGNORE_WARNING))) {
                val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
                this.warnCookieGenerator.removeCookie(response);
            }
        }
        return new Event(this, CasWebflowConstants.STATE_ID_REDIRECT);
    }
}
