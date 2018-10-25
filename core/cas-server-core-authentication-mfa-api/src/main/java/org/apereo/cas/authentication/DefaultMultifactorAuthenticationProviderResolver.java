package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public Optional<MultifactorAuthenticationProvider> resolveProvider(final Map<String, MultifactorAuthenticationProvider> providers,
                                                                       final Collection<String> requestMfaMethod) {
        return providers.values()
            .stream()
            .filter(p -> requestMfaMethod.stream().filter(Objects::nonNull).anyMatch(p::matches))
            .findFirst();
    }

    @Override
    public Optional<MultifactorAuthenticationProvider> resolveProvider(final Map<String, MultifactorAuthenticationProvider> providers,
                                                                       final String requestMfaMethod) {
        return resolveProvider(providers, Stream.of(requestMfaMethod).collect(Collectors.toList()));
    }

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
                                               final Map<String, Object> attributesToExamine,
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

            LOGGER.debug("Selecting a multifactor authentication provider out of [{}] for [{}] and service [{}]", providers, principal.getId(), service);
            val provider = this.multifactorAuthenticationProviderSelector.resolve(providers, service, principal);

            LOGGER.debug("Located attribute value [{}] for [{}]", attributeValue, attributeNames);
            var results = resolveEventViaSingleAttribute(principal, attributeValue, service, context, provider, predicate);
            if (results == null || results.isEmpty()) {
                results = resolveEventViaMultivaluedAttribute(principal, attributeValue, service, context, provider, predicate);
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
    @SneakyThrows
    public Event validateEventIdForMatchingTransitionInContext(final String eventId,
                                                               final Optional<RequestContext> context,
                                                               final Map<String, Object> attributes) {

        val attributesMap = new LocalAttributeMap<Object>(attributes);
        val event = new Event(this, eventId, attributesMap);

        return context.map(ctx -> {
            LOGGER.debug("Resulting event id is [{}] by provider [{}]. Locating transitions in the context for that event id...",
                event.getId(), getName());

            val def = ctx.getMatchingTransition(event.getId());
            if (def == null) {
                LOGGER.warn("Transition definition cannot be found for event [{}]", event.getId());
                throw new AuthenticationException();
            }
            LOGGER.debug("Found matching transition [{}] with target [{}] for event [{}] with attributes [{}].",
                def.getId(), def.getTargetStateId(), event.getId(), event.getAttributes());
            return event;
        }).orElse(event);
    }

    @Override
    public Set<Event> resolveEventViaMultivaluedAttribute(final Principal principal,
                                                          final Object attributeValue,
                                                          final RegisteredService service,
                                                          final Optional<RequestContext> context,
                                                          final MultifactorAuthenticationProvider provider,
                                                          final Predicate<String> predicate) {
        val events = new HashSet<Event>();
        if (attributeValue instanceof Collection) {
            LOGGER.debug("Attribute value [{}] is a multi-valued attribute", attributeValue);
            val values = (Collection<String>) attributeValue;
            values.forEach(value -> {
                try {
                    if (predicate.test(value)) {
                        LOGGER.debug("Attribute value predicate [{}] has successfully matched the [{}]. "
                            + "Attempting to verify multifactor authentication for [{}]", predicate, value, service);
                        LOGGER.debug("Provider [{}] is successfully verified", provider);
                        val id = provider.getId();
                        val event = validateEventIdForMatchingTransitionInContext(id, context, MultifactorAuthenticationProviderResolver.buildEventAttributeMap(principal, Optional.of(service), provider));
                        events.add(event);
                    } else {
                        LOGGER.debug("Attribute value predicate [{}] could not match the [{}]", predicate, value);
                    }
                } catch (final Exception e) {
                    LOGGER.debug("Ignoring [{}] since no matching transition could be found", value);
                }
            });
            return (Set) events;
        }
        LOGGER.debug("Attribute value [{}] of type [{}] is not a multi-valued attribute", attributeValue, attributeValue.getClass());
        return null;
    }

    @Override
    @SneakyThrows
    public Set<Event> resolveEventViaSingleAttribute(final Principal principal,
                                                     final Object attributeValue,
                                                     final RegisteredService service,
                                                     final Optional<RequestContext> context,
                                                     final MultifactorAuthenticationProvider provider,
                                                     final Predicate<String> predicate) {
        if (attributeValue instanceof String) {
            LOGGER.debug("Attribute value [{}] is a single-valued attribute", attributeValue);
            if (predicate.test((String) attributeValue)) {
                LOGGER.debug("Attribute value predicate [{}] has matched the [{}]", predicate, attributeValue);
                return evaluateEventForProviderInContext(principal, service, context, provider);
            }
            LOGGER.debug("Attribute value predicate [{}] could not match the [{}]", predicate, attributeValue);

        }
        LOGGER.debug("Attribute value [{}] is not a single-valued attribute", attributeValue);
        return null;
    }

    @Override
    public Set<Event> evaluateEventForProviderInContext(final Principal principal,
                                                        final RegisteredService service,
                                                        final Optional<RequestContext> context,
                                                        final MultifactorAuthenticationProvider provider) {
        LOGGER.debug("Attempting check for availability of multifactor authentication provider [{}] for [{}]", provider, service);
        if (provider != null) {
            LOGGER.debug("Provider [{}] is successfully verified", provider);
            val id = provider.getId();
            val event = validateEventIdForMatchingTransitionInContext(id, context,
                MultifactorAuthenticationProviderResolver.buildEventAttributeMap(principal, Optional.of(service), provider));
            return CollectionUtils.wrapSet(event);
        }
        LOGGER.debug("Provider could not be verified");
        return new HashSet<>(0);
    }

    @Override
    public Set<Event> resolveEventViaPrincipalAttribute(final Principal principal,
                                                        final Collection<String> attributeNames,
                                                        final RegisteredService service,
                                                        final Optional<RequestContext> context,
                                                        final Collection<MultifactorAuthenticationProvider> providers,
                                                        final Predicate<String> predicate) {
        if (attributeNames.isEmpty()) {
            LOGGER.debug("No attribute names are provided to trigger a multifactor authentication provider via [{}]", getName());
            return null;
        }

        if (providers == null || providers.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            return null;
        }

        val attributes = getPrincipalAttributesForMultifactorAuthentication(principal);
        return resolveEventViaAttribute(principal, attributes, attributeNames, service, context, providers, predicate);
    }

    @Override
    public Map<String, Object> getPrincipalAttributesForMultifactorAuthentication(final Principal principal) {
        return principal.getAttributes();
    }
}
