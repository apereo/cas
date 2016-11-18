package org.apereo.cas.web.flow.resolver.impl;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link RestEndpointMultifactorAuthenticationPolicyEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RestEndpointMultifactorAuthenticationPolicyEventResolver
        extends BaseMultifactorAuthenticationProviderEventResolver {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = resolveRegisteredServiceInRequestContext(context);
        final Authentication authentication = WebUtils.getAuthentication(context);
        final String restEndpoint = casProperties.getAuthn().getMfa().getRestEndpoint();

        if (service == null || authentication == null) {
            logger.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        final Principal principal = authentication.getPrincipal();
        if (StringUtils.isBlank(restEndpoint)) {
            logger.debug("Rest endpoint to determine event is not configured for {}", principal.getId());
            return null;
        }

        final Map<String, MultifactorAuthenticationProvider> providerMap =
                WebUtils.getAllMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            logger.warn("No multifactor authentication providers are available in the application context");
            return null;
        }

        final Collection<MultifactorAuthenticationProvider> flattenedProviders = flattenProviders(providerMap.values());

        logger.debug("Contacting {} to inquire about {}", restEndpoint, principal.getId());
        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<String> responseEntity = restTemplate.postForEntity(restEndpoint, principal.getId(), String.class);
        if (responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK) {
            final String results = responseEntity.getBody();
            if (StringUtils.isNotBlank(results)) {
                logger.debug("Result returned from the rest endpoint is {}", results);
                final MultifactorAuthenticationProvider restProvider = flattenedProviders.stream()
                        .filter(p -> p.matches(results))
                        .findFirst()
                        .orElse(null);

                if (restProvider != null) {
                    logger.debug("Found multifactor authentication provider {}", restProvider.getId());
                    return Sets.newHashSet(new Event(this, restProvider.getId()));
                }
                logger.debug("No multifactor authentication provider could be matched against {}", results);
                return Sets.newHashSet();
            }
        }
        logger.debug("No providers are available to match rest endpoint results");
        return Sets.newHashSet();
    }


    @Audit(action = "AUTHENTICATION_EVENT", actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
