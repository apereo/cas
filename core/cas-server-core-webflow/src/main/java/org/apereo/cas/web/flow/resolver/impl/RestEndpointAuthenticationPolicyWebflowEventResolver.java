package org.apereo.cas.web.flow.resolver.impl;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Set;

/**
 * This is {@link RestEndpointAuthenticationPolicyWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RestEndpointAuthenticationPolicyWebflowEventResolver
        extends BaseMultifactorAuthenticationWebflowEventResolver {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = WebUtils.getRegisteredService(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            logger.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        final Principal principal = authentication.getPrincipal();
        if (StringUtils.isBlank(casProperties.getAuthn().getMfa().getRestEndpoint())) {
            logger.debug("Rest endpoint to determine event is not configured for {}", principal.getId());
            return null;
        }

        final Map<String, MultifactorAuthenticationProvider> providerMap =
                WebUtils.getAllMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            logger.warn("No multifactor authentication providers are available in the application context");
            return null;
        }

        logger.debug("Contacting {} to inquire about {}", casProperties.getAuthn().getMfa().getRestEndpoint(), principal.getId());
        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(casProperties.getAuthn().getMfa().getRestEndpoint(), principal.getId(), String.class);
        if (responseEntity != null && responseEntity.getStatusCode() == HttpStatus.OK) {
            final String results = responseEntity.getBody();
            if (StringUtils.isNotBlank(results)) {
                logger.debug("Result returned from the rest endpoint is {}", results);
                return Sets.newHashSet(new Event(this, results));
            }
        }
        return Sets.newHashSet();
    }


    @Audit(action = "AUTHENTICATION_EVENT", actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
