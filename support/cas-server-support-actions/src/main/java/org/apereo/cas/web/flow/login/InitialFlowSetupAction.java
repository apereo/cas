package org.apereo.cas.web.flow.login;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.action.AbstractAction;
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
public class InitialFlowSetupAction extends AbstractAction {

    private final List<ArgumentExtractor> argumentExtractors;

    private final ServicesManager servicesManager;

    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    private final CasCookieBuilder ticketGrantingTicketCookieGenerator;

    private final CasCookieBuilder warnCookieGenerator;

    private final CasConfigurationProperties casProperties;

    private final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    private final SingleSignOnParticipationStrategy renewalStrategy;

    private final TicketRegistrySupport ticketRegistrySupport;

    /**
     * Configure the POST parameters in webflow.
     *
     * @param context the webflow context
     */
    protected static void configureWebflowForPostParameters(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        if (request.getMethod().equalsIgnoreCase(HttpMethod.POST.name())) {
            WebUtils.putInitialHttpRequestPostParameters(context);
        }
    }

    @Override
    public Event doExecute(final RequestContext context) {
        configureCookieGenerators(context);
        configureWebflowContext(context);

        configureWebflowForPostParameters(context);
        configureWebflowForCustomFields(context);
        configureWebflowForServices(context);

        val ticketGrantingTicketId = configureWebflowForTicketGrantingTicket(context);
        configureWebflowForSsoParticipation(context, ticketGrantingTicketId);

        return success();
    }

    /**
     * Configure the ticket granting ticket in webflow.
     *
     * @param context the webflow context
     * @return the TGT identifier or {@code null}
     */
    protected String configureWebflowForTicketGrantingTicket(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        val ticketGrantingTicketId = ticketGrantingTicketCookieGenerator.retrieveCookieValue(request);
        val ticket = ticketRegistrySupport.getTicketGrantingTicket(ticketGrantingTicketId);
        if (ticket != null) {
            WebUtils.putTicketGrantingTicketInScopes(context, ticket.getId());
            return ticket.getId();
        }
        ticketGrantingTicketCookieGenerator.removeCookie(response);
        ticketGrantingTicketCookieGenerator.removeAll(request, response);
        WebUtils.putTicketGrantingTicketInScopes(context, StringUtils.EMPTY);
        return null;
    }

    /**
     * Configure the custom fields in webflow.
     *
     * @param context the webflow context
     */
    protected void configureWebflowForCustomFields(final RequestContext context) {
        WebUtils.putCustomLoginFormFields(context, casProperties.getView().getCustomLoginFormFields());
    }

    /**
     * Configure the services in webflow.
     *
     * @param context the webflow context
     */
    protected void configureWebflowForServices(final RequestContext context) {
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        if (HttpStatus.valueOf(response.getStatus()).isError()) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, StringUtils.EMPTY);
        }

        val service = WebUtils.getService(this.argumentExtractors, context);
        if (service != null) {
            LOGGER.debug("Placing service in context scope: [{}]", service.getId());
            val selectedService = authenticationRequestServiceSelectionStrategies.resolveService(service);
            val registeredService = servicesManager.findServiceBy(selectedService);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service.getId(), registeredService);
            if (registeredService != null && registeredService.getAccessStrategy().isServiceAccessAllowed()) {
                LOGGER.debug("Placing registered service [{}] with id [{}] in context scope",
                    registeredService.getServiceId(),
                    registeredService.getId());
                WebUtils.putRegisteredService(context, registeredService);

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

    /**
     * Configure the SSO participation in webflow.
     *
     * @param context                the webflow context
     * @param ticketGrantingTicketId the TGT identifier
     */
    protected void configureWebflowForSsoParticipation(final RequestContext context, final String ticketGrantingTicketId) {
        val ssoRequest = SingleSignOnParticipationRequest.builder()
            .requestContext(context)
            .build();
        val ssoParticipation = renewalStrategy.supports(ssoRequest) && renewalStrategy.isParticipating(ssoRequest);
        if (!ssoParticipation && StringUtils.isNotBlank(ticketGrantingTicketId)) {
            val auth = this.ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicketId);
            WebUtils.putExistingSingleSignOnSessionAvailable(context, auth != null);
            WebUtils.putExistingSingleSignOnSessionPrincipal(context,
                Optional.ofNullable(auth).map(Authentication::getPrincipal).orElse(NullPrincipal.getInstance()));
        }
    }

    /**
     * Configure the webflow.
     *
     * @param context the webflow context
     */
    protected void configureWebflowContext(final RequestContext context) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        WebUtils.putWarningCookie(context, Boolean.valueOf(this.warnCookieGenerator.retrieveCookieValue(request)));

        WebUtils.putGeoLocationTrackingIntoFlowScope(context, casProperties.getEvents().getCore().isTrackGeolocation());
        WebUtils.putRememberMeAuthenticationEnabled(context, casProperties.getTicket().getTgt().getRememberMe().isEnabled());

        val staticAuthEnabled = (casProperties.getAuthn().getAccept().isEnabled()
            && StringUtils.isNotBlank(casProperties.getAuthn().getAccept().getUsers()))
            || StringUtils.isNotBlank(casProperties.getAuthn().getReject().getUsers());
        WebUtils.putStaticAuthenticationIntoFlowScope(context, staticAuthEnabled);

        if (casProperties.getAuthn().getPolicy().isSourceSelectionEnabled()) {
            val availableHandlers = authenticationEventExecutionPlan.getAuthenticationHandlers()
                .stream()
                .filter(h -> h.supports(UsernamePasswordCredential.class))
                .map(h -> StringUtils.capitalize(h.getName().trim()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
            WebUtils.putAvailableAuthenticationHandleNames(context, availableHandlers);
        }
    }

    /**
     * Configure the cookie generators in webflow.
     *
     * @param context the webflow context
     */
    protected void configureCookieGenerators(final RequestContext context) {
        val contextPath = context.getExternalContext().getContextPath();
        val cookiePath = StringUtils.isNotBlank(contextPath) ? contextPath + '/' : "/";

        if (casProperties.getWarningCookie().isAutoConfigureCookiePath()) {
            val path = this.warnCookieGenerator.getCookiePath();
            if (StringUtils.isBlank(path)) {
                LOGGER.debug("Setting path for cookies for warn cookie generator to: [{}]", cookiePath);
                this.warnCookieGenerator.setCookiePath(cookiePath);
            } else {
                LOGGER.trace("Warning cookie is set to [{}] with path [{}]", this.warnCookieGenerator.getCookieDomain(), path);
            }
        }

        if (casProperties.getTgc().isAutoConfigureCookiePath()) {
            val path = this.ticketGrantingTicketCookieGenerator.getCookiePath();
            if (StringUtils.isBlank(path)) {
                LOGGER.debug("Setting path for cookies for TGC cookie generator to: [{}]", cookiePath);
                this.ticketGrantingTicketCookieGenerator.setCookiePath(cookiePath);
            } else {
                LOGGER.trace("Ticket-granting cookie domain is [{}] with path [{}]", this.ticketGrantingTicketCookieGenerator.getCookieDomain(), path);
            }
        }
    }
}
