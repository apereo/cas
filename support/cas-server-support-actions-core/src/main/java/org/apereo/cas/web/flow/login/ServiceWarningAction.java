package org.apereo.cas.web.flow.login;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.PrincipalElectionStrategy;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicketGeneratorAuthority;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;

/**
 * This is {@link ServiceWarningAction}. Populates the view
 * with the target url of the application after the warning
 * screen is displayed.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class ServiceWarningAction extends BaseCasWebflowAction {

    /**
     * Parameter name indicating that warning should be ignored and removed.
     */
    public static final String PARAMETER_NAME_IGNORE_WARNING = "ignorewarn";

    private final CentralAuthenticationService centralAuthenticationService;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final TicketRegistrySupport ticketRegistrySupport;

    private final CasCookieBuilder warnCookieGenerator;

    private final PrincipalElectionStrategy principalElectionStrategy;

    private final List<ServiceTicketGeneratorAuthority> serviceTicketAuthorities;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);

        val service = WebUtils.getService(requestContext);
        val ticketGrantingTicket = WebUtils.getTicketGrantingTicketId(requestContext);
        FunctionUtils.throwIf(StringUtils.isBlank(ticketGrantingTicket),
            () -> new InvalidTicketException(new AuthenticationException("No ticket-granting ticket could be found in the context"), ticketGrantingTicket));
        val authentication = ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicket);
        FunctionUtils.throwIf(authentication == null,
            () -> new InvalidTicketException(new AuthenticationException("No authentication found for ticket " + ticketGrantingTicket), ticketGrantingTicket));

        val credential = WebUtils.getCredential(requestContext);
        val authenticationResultBuilder = authenticationSystemSupport.establishAuthenticationContextFromInitial(authentication, credential);
        val authenticationResult = authenticationResultBuilder.build(principalElectionStrategy, service);
        grantServiceTicket(authenticationResult, service, requestContext);

        if (request.getParameterMap().containsKey(PARAMETER_NAME_IGNORE_WARNING)) {
            if (Boolean.parseBoolean(request.getParameter(PARAMETER_NAME_IGNORE_WARNING))) {
                val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
                this.warnCookieGenerator.removeCookie(response);
            }
        }
        return new Event(this, CasWebflowConstants.STATE_ID_REDIRECT);
    }

    private void grantServiceTicket(final AuthenticationResult authenticationResult,
                                    final Service service,
                                    final RequestContext requestContext) {
        serviceTicketAuthorities
            .stream()
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .filter(auth -> auth.supports(authenticationResult, service))
            .findFirst()
            .ifPresent(auth -> {
                val ticketGrantingTicket = WebUtils.getTicketGrantingTicketId(requestContext);
                val serviceTicketId = centralAuthenticationService.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
                WebUtils.putServiceTicketInRequestScope(requestContext, serviceTicketId);
            });
    }
}
