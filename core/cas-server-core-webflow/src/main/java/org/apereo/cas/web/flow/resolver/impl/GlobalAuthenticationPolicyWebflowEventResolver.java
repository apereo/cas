package org.apereo.cas.web.flow.resolver.impl;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link GlobalAuthenticationPolicyWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GlobalAuthenticationPolicyWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = WebUtils.getRegisteredService(context);
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
                WebUtils.getAllMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            logger.warn("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        final Optional<MultifactorAuthenticationProvider> providerFound = providerMap.values().stream()
                .filter(provider -> provider.getId().equals(mfaId))
                .findFirst();

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
}
