package org.apereo.cas.web.flow.resolver.impl;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link GlobalMultifactorAuthenticationPolicyEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GlobalMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = resolveRegisteredServiceInRequestContext(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (authentication == null) {
            logger.debug("No authentication is available to determine event for principal");
            return null;
        }
        final String mfaId = casProperties.getAuthn().getMfa().getGlobalProviderId();
        if (StringUtils.isBlank(mfaId)) {
            logger.debug("No value could be found for request parameter {}", mfaId);
            return null;
        }
        logger.debug("Attempting to globally activate {}", mfaId);

        final Map<String, MultifactorAuthenticationProvider> providerMap =
                WebUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            logger.error("No multifactor authentication providers are available in the application context to handle " + mfaId);
            throw new AuthenticationException();
        }

        final Optional<MultifactorAuthenticationProvider> providerFound = resolveProvider(providerMap, mfaId);


        if (providerFound.isPresent()) {
            if (providerFound.get().isAvailable(service)) {
                logger.debug("Attempting to build an event based on the authentication provider [{}] and service [{}]",
                        providerFound.get(), service.getName());
                final Event event = validateEventIdForMatchingTransitionInContext(providerFound.get().getId(), context,
                        buildEventAttributeMap(authentication.getPrincipal(), service, providerFound.get()));
                return ImmutableSet.of(event);
            }
            logger.warn("Located multifactor provider {}, yet the provider cannot be reached or verified", providerFound.get());
            return null;
        }
        logger.warn("No multifactor provider could be found for {}", mfaId);
        throw new AuthenticationException();
    }


    @Audit(action = "AUTHENTICATION_EVENT", actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
