package org.apereo.cas.web.flow.resolver.impl.mfa;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link RestEndpointMultifactorAuthenticationPolicyEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@Setter
public class RestEndpointMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver
    implements MultifactorAuthenticationTrigger {

    private final String restEndpoint;
    private int order = Ordered.LOWEST_PRECEDENCE;

    public RestEndpointMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                    final CentralAuthenticationService centralAuthenticationService,
                                                                    final ServicesManager servicesManager, final TicketRegistrySupport ticketRegistrySupport,
                                                                    final CookieGenerator warnCookieGenerator,
                                                                    final AuthenticationServiceSelectionPlan authSelectionStrategies,
                                                                    final MultifactorAuthenticationProviderSelector selector,
                                                                    final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport,
            warnCookieGenerator, authSelectionStrategies, selector);
        this.restEndpoint = casProperties.getAuthn().getMfa().getRestEndpoint();
    }

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication, final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest,
                                                                   final Service service) {
        if (service == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return Optional.empty();
        }
        val principal = authentication.getPrincipal();
        if (StringUtils.isBlank(restEndpoint)) {
            LOGGER.debug("Rest endpoint to determine event is not configured for [{}]", principal.getId());
            return Optional.empty();
        }
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            return Optional.empty();
        }

        LOGGER.debug("Contacting [{}] to inquire about [{}]", restEndpoint, principal.getId());
        val results = callRestEndpointForMultifactor(principal, service);
        if (StringUtils.isNotBlank(results)) {
            return MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(results, applicationContext);
        }

        return Optional.empty();
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        val registeredService = resolveRegisteredServiceInRequestContext(context);
        val service = resolveServiceFromAuthenticationRequest(context);
        val authentication = WebUtils.getAuthentication(context);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        val result = isActivated(authentication, registeredService, request, service);
        return result.map(provider -> {
            LOGGER.debug("Attempting to build an event based on the authentication provider [{}] and service [{}]", provider, registeredService.getName());
            val event = validateEventIdForMatchingTransitionInContext(provider.getId(), Optional.of(context),
                buildEventAttributeMap(authentication.getPrincipal(), Optional.of(registeredService), provider));
            return CollectionUtils.wrapSet(event);
        }).orElse(null);
    }

    @Audit(action = "AUTHENTICATION_EVENT",
        actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
        resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }

    /**
     * Call rest endpoint for multifactor.
     *
     * @param principal       the principal
     * @param resolvedService the resolved service
     * @return return the rest response, typically the mfa id.
     */
    protected String callRestEndpointForMultifactor(final Principal principal, final Service resolvedService) {
        val restTemplate = new RestTemplate();
        val entity = new RestEndpointEntity(principal.getId(), resolvedService.getId());
        val responseEntity = restTemplate.postForEntity(restEndpoint, entity, String.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        }
        return null;
    }

    /**
     * The Rest endpoint entity passed along to the API.
     */
    @Getter
    @RequiredArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class RestEndpointEntity {
        private final String principalId;
        private final String serviceId;
    }
}
