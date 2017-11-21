package org.apereo.cas.web.flow.resolver.impl.mfa;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link RestEndpointMultifactorAuthenticationPolicyEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RestEndpointMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestEndpointMultifactorAuthenticationPolicyEventResolver.class);

    private final String restEndpoint;

    public RestEndpointMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                    final CentralAuthenticationService centralAuthenticationService,
                                                                    final ServicesManager servicesManager,
                                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                                    final CookieGenerator warnCookieGenerator,
                                                                    final AuthenticationServiceSelectionPlan authSelectionStrategies,
                                                                    final MultifactorAuthenticationProviderSelector selector,
                                                                    final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator, authSelectionStrategies,
                selector);
        this.restEndpoint = casProperties.getAuthn().getMfa().getRestEndpoint();
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = resolveRegisteredServiceInRequestContext(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        final Principal principal = authentication.getPrincipal();
        if (StringUtils.isBlank(restEndpoint)) {
            LOGGER.debug("Rest endpoint to determine event is not configured for [{}]", principal.getId());
            return null;
        }

        final Map<String, MultifactorAuthenticationProvider> providerMap =
                MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            return null;
        }

        final Collection<MultifactorAuthenticationProvider> flattenedProviders = flattenProviders(providerMap.values());

        LOGGER.debug("Contacting [{}] to inquire about [{}]", restEndpoint, principal.getId());
        final String results = callRestEndpointForMultifactor(principal, context);

        if (StringUtils.isNotBlank(results)) {
            return resolveMultifactorEventViaRestResult(results, flattenedProviders);
        }
        LOGGER.debug("No providers are available to match rest endpoint results");
        return new HashSet<>(0);
    }

    @Audit(action = "AUTHENTICATION_EVENT",
            actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
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
    protected Set<Event> resolveMultifactorEventViaRestResult(final String results,
                                                              final Collection<MultifactorAuthenticationProvider> providers) {
        LOGGER.debug("Result returned from the rest endpoint is [{}]", results);
        final MultifactorAuthenticationProvider restProvider = providers.stream()
                .filter(p -> p.matches(results))
                .findFirst()
                .orElse(null);

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
        final RestTemplate restTemplate = new RestTemplate();
        final Service resolvedService = resolveServiceFromAuthenticationRequest(context);
        final RestEndpointEntity entity = new RestEndpointEntity(principal.getId(), resolvedService.getId());
        final ResponseEntity<String> responseEntity = restTemplate.postForEntity(restEndpoint, entity, String.class);
        if (responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();
        }
        return null;
    }

    /**
     * The Rest endpoint entity passed along to the API.
     */
    public static class RestEndpointEntity {
        private String principalId;
        private String serviceId;

        public RestEndpointEntity(final String principalId, final String serviceId) {
            this.principalId = principalId;
            this.serviceId = serviceId;
        }

        public String getPrincipalId() {
            return principalId;
        }

        public void setPrincipalId(final String principalId) {
            this.principalId = principalId;
        }

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(final String serviceId) {
            this.serviceId = serviceId;
        }
    }
}
