package org.apereo.cas.web.flow.login;

import module java.base;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicketGeneratorAuthority;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.CasWebflowCredentialProvider;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
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
public class ServiceWarningAction extends BaseCasWebflowAction {

    /**
     * Parameter name indicating that warning should be ignored and removed.
     */
    public static final String PARAMETER_NAME_IGNORE_WARNING = "ignorewarn";

    private final CentralAuthenticationService centralAuthenticationService;

    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final TicketRegistrySupport ticketRegistrySupport;

    private final CasCookieBuilder warnCookieGenerator;

    private final List<ServiceTicketGeneratorAuthority> serviceTicketAuthorities;

    private final CasWebflowCredentialProvider casWebflowCredentialProvider;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val service = WebUtils.getService(requestContext);
        val ticketGrantingTicket = WebUtils.getTicketGrantingTicketId(requestContext);
        FunctionUtils.throwIf(StringUtils.isBlank(ticketGrantingTicket),
            () -> new InvalidTicketException(new AuthenticationException("No ticket-granting ticket could be found in the context"), ticketGrantingTicket));
        val authentication = ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicket);
        FunctionUtils.throwIfNull(authentication,
            () -> new InvalidTicketException(new AuthenticationException("No authentication found for ticket " + ticketGrantingTicket), ticketGrantingTicket));
        val credentials = casWebflowCredentialProvider.extract(requestContext);
        val authenticationResultBuilder = authenticationSystemSupport.establishAuthenticationContextFromInitial(
            authentication, credentials.toArray(credentials.toArray(Credential.EMPTY_CREDENTIALS_ARRAY)));
        val authenticationResult = FunctionUtils.doUnchecked(() -> authenticationResultBuilder.build(service));
        grantServiceTicket(authenticationResult, service, requestContext);

        if (request.getParameterMap().containsKey(PARAMETER_NAME_IGNORE_WARNING)
            && BooleanUtils.toBoolean(request.getParameter(PARAMETER_NAME_IGNORE_WARNING))) {
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            warnCookieGenerator.removeCookie(response);
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
            .ifPresent(Unchecked.consumer(auth -> {
                val ticketGrantingTicket = WebUtils.getTicketGrantingTicketId(requestContext);
                val serviceTicketId = centralAuthenticationService.grantServiceTicket(ticketGrantingTicket, service, authenticationResult);
                WebUtils.putServiceTicketInRequestScope(requestContext, serviceTicketId);
            }));
    }
}
