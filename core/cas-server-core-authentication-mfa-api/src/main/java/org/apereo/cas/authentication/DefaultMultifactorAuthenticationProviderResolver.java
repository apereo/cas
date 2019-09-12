package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This is {@link DefaultMultifactorAuthenticationProviderResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class DefaultMultifactorAuthenticationProviderResolver implements MultifactorAuthenticationProviderResolver {
    private final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    @Override
    public Set<Event> resolveEventViaAuthenticationAttribute(final Authentication authentication,
                                                             final Collection<String> attributeNames,
                                                             final RegisteredService service,
                                                             final Optional<RequestContext> context,
                                                             final Collection<MultifactorAuthenticationProvider> providers,
                                                             final Predicate<String> predicate) {
        return resolveEventViaAttribute(authentication.getPrincipal(), authentication.getAttributes(),
            attributeNames, service, context, providers, predicate);
    }

    @Override
    public Set<Event> resolveEventViaAttribute(final Principal principal,
                                               final Map<String, List<Object>> attributesToExamine,
                                               final Collection<String> attributeNames,
                                               final RegisteredService service,
                                               final Optional<RequestContext> context,
                                               final Collection<MultifactorAuthenticationProvider> providers,
                                               final Predicate<String> predicate) {
        if (providers == null || providers.isEmpty()) {
            LOGGER.debug("No authentication provider is associated with this service");
            return null;
        }

        LOGGER.debug("Locating attribute value for attribute(s): [{}]", attributeNames);
        for (val attributeName : attributeNames) {
            val attributeValue = attributesToExamine.get(attributeName);
            if (attributeValue == null) {
                LOGGER.debug("Attribute value for [{}] to determine event is not configured for [{}]", attributeName, principal.getId());
                continue;
            }
            LOGGER.debug("Located attribute value [{}] for [{}]", attributeValue, attributeNames);

            val provider = this.multifactorAuthenticationProviderSelector.resolve(providers, service, principal);
            LOGGER.debug("Selected a multifactor authentication provider out of [{}] for [{}] and service [{}] as [{}]",
                providers, principal.getId(), service, provider);

            var results = MultifactorAuthenticationUtils.resolveEventViaSingleAttribute(principal, attributeValue,
                service, context, provider, predicate);
            if (results == null || results.isEmpty()) {
                results = MultifactorAuthenticationUtils.resolveEventViaMultivaluedAttribute(principal, attributeValue,
                    service, context, provider, predicate);
            }
            if (results != null && !results.isEmpty()) {
                LOGGER.debug("Resolved set of events based on the attribute [{}] are [{}]", attributeName, results);
                return results;
            }
        }
        LOGGER.debug("No set of events based on the attribute(s) [{}] could be matched", attributeNames);
        return null;
    }

    @Override
    public Set<Event> resolveEventViaPrincipalAttribute(final Principal principal,
                                                        final Collection<String> attributeNames,
                                                        final RegisteredService service,
                                                        final Optional<RequestContext> context,
                                                        final Collection<MultifactorAuthenticationProvider> providers,
                                                        final Predicate<String> predicate) {
        if (attributeNames.isEmpty()) {
            LOGGER.trace("No attribute names are provided to trigger a multifactor authentication provider via [{}]", getName());
            return null;
        }

        if (providers == null || providers.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            return null;
        }

        val attributes = principal.getAttributes();
        return resolveEventViaAttribute(principal, attributes, attributeNames, service, context, providers, predicate);
    }
}
