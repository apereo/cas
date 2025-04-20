package org.apereo.cas.web.flow.login;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceProperty.RegisteredServiceProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CookieUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Class to automatically set the paths for the CookieGenerators.
 * <p>
 * Note: This is technically not thread-safe, but because its overriding with a
 * constant value it doesn't matter.
 * <p>
 * Note: As of CAS 3.1, this is a required class that retrieves and exposes the
 * values in the two cookies for subclasses to use.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class InitialFlowSetupAction extends BaseCasWebflowAction {

    private final List<ArgumentExtractor> argumentExtractors;

    private final ServicesManager servicesManager;

    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final CasCookieBuilder warnCookieGenerator;

    private final CasConfigurationProperties casProperties;

    private final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    private final SingleSignOnParticipationStrategy renewalStrategy;

    private final TicketRegistrySupport ticketRegistrySupport;

    protected static void configureWebflowForPostParameters(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        if (request.getMethod().equalsIgnoreCase(HttpMethod.POST.name())) {
            WebUtils.putInitialHttpRequestPostParameters(context);
        }
    }

    @Override
    protected Event doExecuteInternal(final RequestContext context) throws Throwable {
        configureCookieGenerators(context);
        configureWebflowForPostParameters(context);
        configureWebflowForCustomFields(context);
        configureWebflowForServices(context);
        configureWebflowContext(context);

        val ticketGrantingTicketId = configureWebflowForTicketGrantingTicket(context);
        configureWebflowForSsoParticipation(context, ticketGrantingTicketId);
        
        return success();
    }

    protected String configureWebflowForTicketGrantingTicket(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val ticketGrantingTicketId = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        LOGGER.trace("Retrieved the ticket-granting ticket identifier in the login webflow: [{}]", ticketGrantingTicketId);
        val ticket = ticketRegistrySupport.getTicketGrantingTicket(ticketGrantingTicketId);
        LOGGER.trace("Retrieved the ticket-granting ticket in the login webflow: [{}]", ticket);
        if (ticket != null) {
            WebUtils.putTicketGrantingTicketInScopes(context, ticket.getId());
            return ticket.getId();
        }
        clearTicketGrantingCookieFromContext(context);
        return null;
    }

    protected void clearTicketGrantingCookieFromContext(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        ticketGrantingTicketCookieGenerator.removeAll(request, response);
        WebUtils.putTicketGrantingTicketInScopes(context, StringUtils.EMPTY);
    }

    protected void configureWebflowForCustomFields(final RequestContext context) {
        WebUtils.putCustomLoginFormFields(context, casProperties.getView().getCustomLoginFormFields());
    }

    protected void configureWebflowForServices(final RequestContext context) {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        if (HttpStatus.valueOf(response.getStatus()).isError()) {
            throw UnauthorizedServiceException.denied("Denied");
        }

        val service = WebUtils.getService(argumentExtractors, context);
        if (service != null) {
            LOGGER.debug("Placing service in context scope: [{}]", service.getId());
            val selectedService = FunctionUtils.doUnchecked(() -> authenticationRequestServiceSelectionStrategies.resolveService(service));
            val registeredService = servicesManager.findServiceBy(selectedService);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            if (registeredService != null && registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, selectedService)) {
                LOGGER.debug("Placing registered service [{}] with id [{}] in context scope",
                    registeredService.getServiceId(),
                    registeredService.getId());
                WebUtils.putRegisteredService(context, registeredService);
                WebUtils.putWildcardedRegisteredService(context,
                    RegisteredServiceProperties.WILDCARDED_SERVICE_DEFINITION.isAssignedTo(registeredService));
                val accessStrategy = registeredService.getAccessStrategy();
                if (accessStrategy.getUnauthorizedRedirectUrl() != null) {
                    LOGGER.debug("Placing registered service's unauthorized redirect url [{}] with id [{}] in context scope",
                        accessStrategy.getUnauthorizedRedirectUrl(),
                        registeredService.getServiceId());
                    WebUtils.putUnauthorizedRedirectUrlIntoFlowScope(context, accessStrategy.getUnauthorizedRedirectUrl());
                }
            }
            WebUtils.putServiceIntoFlowScope(context, service);
        }
    }

    protected void configureWebflowForSsoParticipation(final RequestContext context, final String ticketGrantingTicketId) throws Throwable {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .requestContext(context)
            .httpServletRequest(request)
            .httpServletResponse(response)
            .build();
        val ssoParticipation = renewalStrategy.supports(ssoRequest) && renewalStrategy.isParticipating(ssoRequest);
        if (!ssoParticipation && StringUtils.isNotBlank(ticketGrantingTicketId)) {
            val auth = ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicketId);
            WebUtils.putExistingSingleSignOnSessionAvailable(context, auth != null);
            WebUtils.putExistingSingleSignOnSessionPrincipal(context,
                Optional.ofNullable(auth).map(Authentication::getPrincipal).orElseGet(NullPrincipal::getInstance));
            WebUtils.putTicketGrantingTicketInScopes(context, StringUtils.EMPTY);
        }
    }

    protected void configureWebflowContext(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        WebUtils.putWarningCookie(context, Boolean.valueOf(warnCookieGenerator.retrieveCookieValue(request)));

        WebUtils.putGeoLocationTrackingIntoFlowScope(context, casProperties.getEvents().getCore().isTrackGeolocation());
        WebUtils.putRememberMeAuthenticationEnabled(context, casProperties.getTicket().getTgt().getRememberMe().isEnabled());

        val staticAuthEnabled = (casProperties.getAuthn().getAccept().isEnabled()
                                 && StringUtils.isNotBlank(casProperties.getAuthn().getAccept().getUsers()))
                                || StringUtils.isNotBlank(casProperties.getAuthn().getReject().getUsers());
        WebUtils.putStaticAuthenticationIntoFlowScope(context, staticAuthEnabled);

        if (casProperties.getAuthn().getPolicy().isSourceSelectionEnabled()) {
            val availableHandlers = determineAuthenticationHandlersForSourceSelection(context);
            WebUtils.putAvailableAuthenticationHandleNames(context, availableHandlers);
        }
        context.getFlowScope().put("httpRequestSecure", request.isSecure());
        context.getFlowScope().put("httpRequestMethod", request.getMethod());
        context.getFlowScope().put("httpRequestHeaders", HttpRequestUtils.getRequestHeaders(request));
    }

    protected List<String> determineAuthenticationHandlersForSourceSelection(final RequestContext context) {
        val availableHandlers = authenticationEventExecutionPlan.getAuthenticationHandlers()
            .stream()
            .filter(handler -> handler.supports(UsernamePasswordCredential.class))
            .map(handler -> StringUtils.capitalize(handler.getName().trim()))
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        val registeredService = WebUtils.getRegisteredService(context);
        if (registeredService != null && registeredService.getAuthenticationPolicy() != null) {
            val requiredHandlers = registeredService.getAuthenticationPolicy().getRequiredAuthenticationHandlers();
            if (requiredHandlers != null && !requiredHandlers.isEmpty()) {
                availableHandlers.removeIf(handler -> !requiredHandlers.contains(handler));
            }
        }
        return availableHandlers;
    }

    protected void configureCookieGenerators(final RequestContext context) {
        CookieUtils.configureCookiePath(context, warnCookieGenerator);
        CookieUtils.configureCookiePath(context, ticketGrantingTicketCookieGenerator);
    }
}
