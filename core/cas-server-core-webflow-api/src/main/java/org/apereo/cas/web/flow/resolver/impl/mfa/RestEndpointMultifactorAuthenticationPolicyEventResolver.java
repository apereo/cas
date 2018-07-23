package org.apereo.cas.web.flow.resolver.impl.mfa;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.HashSet;
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
public class RestEndpointMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {

    private final String restEndpoint;

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
    public Set<Event> resolveInternal(final RequestContext context) {
        val service = resolveRegisteredServiceInRequestContext(context);
        val authentication = WebUtils.getAuthentication(context);
        if (service == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return null;
        }
        val principal = authentication.getPrincipal();
        if (StringUtils.isBlank(restEndpoint)) {
            LOGGER.debug("Rest endpoint to determine event is not configured for [{}]", principal.getId());
            return null;
        }
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            return null;
        }
        val flattenedProviders = flattenProviders(providerMap.values());
        LOGGER.debug("Contacting [{}] to inquire about [{}]", restEndpoint, principal.getId());
        val results = callRestEndpointForMultifactor(principal, context);
        if (StringUtils.isNotBlank(results)) {
            return resolveMultifactorEventViaRestResult(results, flattenedProviders);
        }
        LOGGER.debug("No providers are available to match rest endpoint results");
        return new HashSet<>(0);
    }

    @Audit(action = "AUTHENTICATION_EVENT", actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER", resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }

    /**
     * Resolve multifactor event via rest result collection.
     *
     * @param results   the results
     * @param providers the flattened providers
     * @return the events
     */
    protected Set<Event> resolveMultifactorEventViaRestResult(final String results, final Collection<MultifactorAuthenticationProvider> providers) {
        LOGGER.debug("Result returned from the rest endpoint is [{}]", results);
        val restProvider = providers.stream().filter(p -> p.matches(results)).findFirst().orElse(null);
        if (restProvider != null) {
            LOGGER.debug("Found multifactor authentication provider [{}]", restProvider.getId());
            return CollectionUtils.wrapSet(new Event(this, restProvider.getId()));
        }
        LOGGER.debug("No multifactor authentication provider could be matched against [{}]", results);
        return new HashSet<>(0);
    }

    /**
     * Call rest endpoint for multifactor.
     *
     * @param principal the principal
     * @param context   the context
     * @return return the rest response, typically the mfa id.
     */
    protected String callRestEndpointForMultifactor(final Principal principal, final RequestContext context) {
        val restTemplate = new RestTemplate();
        val resolvedService = resolveServiceFromAuthenticationRequest(context);
        val entity = new RestEndpointEntity(principal.getId(), resolvedService.getId());
        val responseEntity = restTemplate.postForEntity(restEndpoint, entity, String.class);
        if (responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK) {
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
