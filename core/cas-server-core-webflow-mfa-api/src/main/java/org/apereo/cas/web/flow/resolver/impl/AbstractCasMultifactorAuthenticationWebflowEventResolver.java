package org.apereo.cas.web.flow.resolver.impl;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link AbstractCasMultifactorAuthenticationWebflowEventResolver} that provides parent
 * operations for all child event resolvers to handle MFA webflow changes.
 *
 * @author Travis Schmidt
 * @since 6.0.0
 */
@Slf4j
public abstract class AbstractCasMultifactorAuthenticationWebflowEventResolver extends AbstractCasWebflowEventResolver {

    /**
     * The mfa selector.
     */
    protected final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    public AbstractCasMultifactorAuthenticationWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                    final CentralAuthenticationService centralAuthenticationService,
                                                                    final ServicesManager servicesManager,
                                                                    final TicketRegistrySupport ticketRegistrySupport,
                                                                    final CookieGenerator warnCookieGenerator,
                                                                    final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies,
                                                                    final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport,
                warnCookieGenerator, authenticationRequestServiceSelectionStrategies);
        this.multifactorAuthenticationProviderSelector = multifactorAuthenticationProviderSelector;
    }

    /**
     * Build event attribute map map.
     *
     * @param principal the principal
     * @param service   the service
     * @param provider  the provider
     * @return the map
     */
    protected static Map<String, Object> buildEventAttributeMap(final Principal principal, final RegisteredService service,
                                                                final MultifactorAuthenticationProvider provider) {
        val map = new HashMap<String, Object>();
        map.put(Principal.class.getName(), principal);
        if (service != null) {
            map.put(RegisteredService.class.getName(), service);
        }
        map.put(MultifactorAuthenticationProvider.class.getName(), provider);
        return map;
    }

    /**
     * Gets authentication provider for service.
     *
     * @param service the service
     * @return the authentication provider for service
     */
    protected Collection<MultifactorAuthenticationProvider> getAuthenticationProviderForService(final RegisteredService service) {
        val policy = service.getMultifactorPolicy();
        if (policy != null) {
            return policy.getMultifactorAuthenticationProviders().stream()
                .map(this::getMultifactorAuthenticationProviderFromApplicationContext)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toSet());
        }
        return null;
    }

    private Set<Event> resolveEventViaMultivaluedAttribute(final Principal principal,
                                                           final Object attributeValue,
                                                           final RegisteredService service,
                                                           final RequestContext context,
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
                        val event = validateEventIdForMatchingTransitionInContext(id, context,
                            buildEventAttributeMap(principal, service, provider));
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

    @SneakyThrows
    private Set<Event> resolveEventViaSingleAttribute(final Principal principal,
                                                      final Object attributeValue,
                                                      final RegisteredService service,
                                                      final RequestContext context,
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

    /**
     * Verify provider for current context and validate event id.
     *
     * @param principal the principal
     * @param service   the service
     * @param context   the context
     * @param provider  the provider
     * @return the set
     */
    protected Set<Event> evaluateEventForProviderInContext(final Principal principal,
                                                           final RegisteredService service,
                                                           final RequestContext context,
                                                           final MultifactorAuthenticationProvider provider) {
        LOGGER.debug("Attempting check for availability of multifactor authentication provider [{}] for [{}]", provider, service);
        if (provider != null) {
            LOGGER.debug("Provider [{}] is successfully verified", provider);
            val id = provider.getId();
            val event = validateEventIdForMatchingTransitionInContext(id, context, buildEventAttributeMap(principal, service, provider));
            return CollectionUtils.wrapSet(event);
        }
        LOGGER.debug("Provider [{}] could not be verified", provider);
        return new HashSet<>(0);
    }

    private Set<Event> resolveEventViaAttribute(final Principal principal,
                                                final Map<String, Object> attributesToExamine,
                                                final Collection<String> attributeNames,
                                                final RegisteredService service,
                                                final RequestContext context,
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

    /**
     * Resolve event via authentication attribute set.
     *
     * @param authentication the authentication
     * @param attributeNames the attribute name
     * @param service        the service
     * @param context        the context
     * @param providers      the providers
     * @param predicate      the predicate
     * @return the set of resolved events
     */
    protected Set<Event> resolveEventViaAuthenticationAttribute(final Authentication authentication,
                                                                final Collection<String> attributeNames,
                                                                final RegisteredService service,
                                                                final RequestContext context,
                                                                final Collection<MultifactorAuthenticationProvider> providers,
                                                                final Predicate<String> predicate) {
        return resolveEventViaAttribute(authentication.getPrincipal(), authentication.getAttributes(),
            attributeNames, service, context, providers, predicate);
    }

    /**
     * Resolve event via principal attribute set.
     *
     * @param principal      the principal
     * @param attributeNames the attribute name
     * @param service        the service
     * @param context        the context
     * @param providers      the providers
     * @param predicate      the predicate
     * @return the set of resolved events
     */
    protected Set<Event> resolveEventViaPrincipalAttribute(final Principal principal,
                                                           final Collection<String> attributeNames,
                                                           final RegisteredService service,
                                                           final RequestContext context,
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

    /**
     * Find the MultifactorAuthenticationProvider in the application contact that matches the specified providerId (e.g. "mfa-duo").
     *
     * @param providerId the provider id
     * @return the registered service multifactor authentication provider
     */
    protected Optional<MultifactorAuthenticationProvider> getMultifactorAuthenticationProviderFromApplicationContext(final String providerId) {
        try {
            LOGGER.debug("Locating bean definition for [{}]", providerId);
            return MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(applicationContext).values().stream()
                .filter(p -> p.matches(providerId))
                .findFirst();
        } catch (final Exception e) {
            LOGGER.debug("Could not locate [{}] bean id in the application context as an authentication provider.", providerId);
        }
        return Optional.empty();
    }

    /**
     * Gets principal attributes for multifactor authentication.
     *
     * @param principal the principal
     * @return the principal attributes for multifactor authentication
     */
    protected Map<String, Object> getPrincipalAttributesForMultifactorAuthentication(final Principal principal) {
        return principal.getAttributes();
    }
}
