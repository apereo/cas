package org.apereo.cas.web.flow.login;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.web.util.CookieGenerator;
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
@Slf4j
@RequiredArgsConstructor
public class ServiceWarningAction extends AbstractAction {

    /**
     * Parameter name indicating that warning should be ignored and removed.
     */
    public static final String PARAMETER_NAME_IGNORE_WARNING = "ignorewarn";

    private final CentralAuthenticationService centralAuthenticationService;
    private final AuthenticationSystemSupport authenticationSystemSupport;
    private final TicketRegistrySupport ticketRegistrySupport;
    private final CookieGenerator warnCookieGenerator;
    private final PrincipalElectionStrategy principalElectionStrategy;

    @Override
    protected Event doExecute(final RequestContext context) {
        final var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

        final Service service = WebUtils.getService(context);
        final var ticketGrantingTicket = WebUtils.getTicketGrantingTicketId(context);
        if (StringUtils.isBlank(ticketGrantingTicket)) {
            throw new InvalidTicketException(new AuthenticationException("No ticket-granting ticket could be found in the context"), ticketGrantingTicket);
        }

        final var authentication = this.ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicket);
        if (authentication == null) {
            throw new InvalidTicketException(new AuthenticationException("No authentication found for ticket " + ticketGrantingTicket), ticketGrantingTicket);
        }

        final var credential = WebUtils.getCredential(context);
        final var authenticationResultBuilder =
            authenticationSystemSupport.establishAuthenticationContextFromInitial(authentication, credential);
        final var authenticationResult = authenticationResultBuilder.build(principalElectionStrategy, service);

        final var serviceTicketId = this.centralAuthenticationService.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
        WebUtils.putServiceTicketInRequestScope(context, serviceTicketId);

        if (request.getParameterMap().containsKey(PARAMETER_NAME_IGNORE_WARNING)) {
            if (Boolean.parseBoolean(request.getParameter(PARAMETER_NAME_IGNORE_WARNING))) {
                this.warnCookieGenerator.removeCookie(response);
            }
        }
        return new Event(this, CasWebflowConstants.STATE_ID_REDIRECT);
    }
}
