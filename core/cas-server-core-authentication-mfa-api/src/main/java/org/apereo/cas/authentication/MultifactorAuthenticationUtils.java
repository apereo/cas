package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link MultifactorAuthenticationUtils}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@UtilityClass
public class MultifactorAuthenticationUtils {

    /**
     * Build event attribute map.
     *
     * @param principal         the principal
     * @param service           the service
     * @param registeredService the service
     * @param provider          the provider
     * @return the map
     */
    public static Map<String, Object> buildEventAttributeMap(final Principal principal,
                                                             final Optional<Service> service,
                                                             final Optional<RegisteredService> registeredService,
                                                             final MultifactorAuthenticationProvider provider) {
        val map = new HashMap<String, Object>();
        map.put(Principal.class.getName(), principal);
        registeredService.ifPresent(svc -> map.put(RegisteredService.class.getName(), svc));
        service.ifPresent(svc -> map.put(Service.class.getName(), svc));
        map.put(MultifactorAuthenticationProvider.class.getName(), provider);
        return map;
    }

    /**
     * Validate event id for matching transition in context event.
     *
     * @param eventId    the event id
     * @param context    the context
     * @param attributes the attributes
     * @return the event
     */
    public static Event validateEventIdForMatchingTransitionInContext(final String eventId,
                                                                      final Optional<RequestContext> context,
                                                                      final Map<String, Object> attributes) {
        val attributesMap = new LocalAttributeMap<>(attributes);
        val event = new Event(eventId, eventId, attributesMap);
        LOGGER.trace("Attempting to find a matching transition for event id [{}]", event.getId());
        return context.map(ctx -> {
            LOGGER.trace("Reviewing current state [{}], event [{}] and transition [{}]",
                ctx.getCurrentState(), ctx.getCurrentEvent(), ctx.getCurrentTransition());
            val def = ctx.getMatchingTransition(event.getId());
            if (def == null) {
                val msg = String.format("State [%s:%s:%s] does not have a matching transition for %s",
                    ctx.getCurrentState().getId(),
                    ctx.getCurrentEvent() != null ? ctx.getCurrentEvent().getId() : "N/A",
                    ctx.getCurrentTransition() != null ? ctx.getCurrentTransition().getId() : "N/A",
                    event.getId());
                LoggingUtils.error(LOGGER, msg);
                throw new AuthenticationException(msg);
            }
            return event;
        }).orElse(event);
    }


    /**
     * Resolve event via multivalued attribute set.
     *
     * @param principal         the principal
     * @param attributeValue    the attribute value
     * @param registeredService the service
     * @param service           the service
     * @param context           the context
     * @param provider          the provider
     * @param predicate         the predicate
     * @return the set
     */
    public static Set<Event> resolveEventViaMultivaluedAttribute(
        final Principal principal,
        final Object attributeValue,
        final RegisteredService registeredService,
        final Service service,
        final Optional<RequestContext> context,
        final MultifactorAuthenticationProvider provider,
        final BiPredicate<String, MultifactorAuthenticationProvider> predicate) {

        if (attributeValue instanceof final Collection values) {
            LOGGER.debug("Attribute value [{}] is a multi-valued attribute", attributeValue);
            val events = new HashSet<Event>();
            values.forEach(value -> {
                val providerId = provider.getId();
                try {
                    LOGGER.trace("Testing attribute value [{}] against multifactor provider [{}]", value, provider);
                    if (predicate.test(value.toString(), provider)) {
                        val attributeMap = buildEventAttributeMap(principal,
                            Optional.ofNullable(service), Optional.ofNullable(registeredService), provider);
                        LOGGER.trace("Event attribute map for provider [{}] transition is [{}]", provider, attributeMap);
                        val event = validateEventIdForMatchingTransitionInContext(providerId, context, attributeMap);
                        events.add(event);
                    }
                } catch (final Exception e) {
                    LOGGER.debug("Ignoring [{}] since no matching transition could be found for provider [{}]", value, providerId);
                }
            });
            return events
                .stream()
                .filter(CollectionUtils.distinctByKey(Event::getId))
                .collect(Collectors.toSet());
        }
        LOGGER.debug("Attribute value [{}] is not a multi-valued attribute", attributeValue);
        return null;
    }

    /**
     * Resolve provider optional.
     *
     * @param providers        the providers
     * @param requestMfaMethod the request mfa method
     * @return the optional
     */
    public static Optional<MultifactorAuthenticationProvider> resolveProvider(
        final Map<String, MultifactorAuthenticationProvider> providers,
        final Collection<String> requestMfaMethod) {
        return providers.values()
            .stream()
            .filter(provider -> requestMfaMethod.stream().filter(Objects::nonNull).anyMatch(provider::matches))
            .findFirst();
    }

    /**
     * Resolve provider optional.
     *
     * @param providers        the providers
     * @param requestMfaMethod the request mfa method
     * @return the optional
     */
    public static Optional<MultifactorAuthenticationProvider> resolveProvider(
        final Map<String, MultifactorAuthenticationProvider> providers,
        final String requestMfaMethod) {
        return resolveProvider(providers, Stream.of(requestMfaMethod).collect(Collectors.toList()));
    }


    /**
     * Resolve event via single attribute set.
     *
     * @param principal              the principal
     * @param providedAttributeValue the attribute value
     * @param registeredService      the service
     * @param service                the service
     * @param context                the context
     * @param provider               the provider
     * @param predicate              the predicate
     * @return the set
     */
    public static Set<Event> resolveEventViaSingleAttribute(
        final Principal principal,
        final Object providedAttributeValue,
        final RegisteredService registeredService,
        final Service service,
        final Optional<RequestContext> context,
        final MultifactorAuthenticationProvider provider,
        final BiPredicate<String, MultifactorAuthenticationProvider> predicate) {
        return FunctionUtils.doUnchecked(() -> {
            val processSingleValue = !(providedAttributeValue instanceof Collection) || CollectionUtils.toCollection(providedAttributeValue).size() == 1;
            if (processSingleValue) {
                val attributeValue = CollectionUtils.firstElement(providedAttributeValue).map(Object::toString).orElse(StringUtils.EMPTY);
                LOGGER.debug("Attribute value [{}] is a single-valued attribute", attributeValue);
                if (predicate.test(attributeValue, provider)) {
                    LOGGER.debug("Attribute value predicate [{}] has matched the [{}]", predicate, attributeValue);
                    return evaluateEventForProviderInContext(principal, registeredService, service, context, provider);
                }
                LOGGER.debug("Attribute value predicate [{}] could not match the [{}]", predicate, attributeValue);
            }
            LOGGER.debug("Attribute value [{}] is not a single-valued attribute", providedAttributeValue);
            return null;
        });
    }

    /**
     * Gets authentication provider for service.
     *
     * @param service            the service
     * @param applicationContext the application context
     * @return the authentication provider for service
     */
    public Collection<MultifactorAuthenticationProvider> getMultifactorAuthenticationProviderForService(
        final RegisteredService service,
        final ApplicationContext applicationContext) {
        return Optional.ofNullable(service.getMultifactorAuthenticationPolicy())
            .map(policy -> policy.getMultifactorAuthenticationProviders()
                .stream()
                .map(provider ->
                    MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderFromApplicationContext(provider, applicationContext))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet()))
            .orElseGet(HashSet::new);
    }

    /**
     * Gets multifactor authentication provider from application context.
     *
     * @param providerId         the provider id
     * @param applicationContext the application context
     * @return the multifactor authentication provider from application context
     */
    public static Optional<MultifactorAuthenticationProvider> getMultifactorAuthenticationProviderFromApplicationContext(
        final String providerId,
        final ApplicationContext applicationContext) {
        LOGGER.trace("Locating bean definition for [{}]", providerId);
        return getAvailableMultifactorAuthenticationProviders(applicationContext).values().stream()
            .filter(provider -> provider.matches(providerId))
            .findFirst();
    }

    /**
     * Evaluate event for provider in context set.
     *
     * @param principal         the principal
     * @param registeredService the service
     * @param service           the service
     * @param context           the context
     * @param provider          the provider
     * @return the set
     */
    public static Set<Event> evaluateEventForProviderInContext(final Principal principal,
                                                               final RegisteredService registeredService,
                                                               final Service service,
                                                               final Optional<RequestContext> context,
                                                               final MultifactorAuthenticationProvider provider) {
        LOGGER.debug("Attempting check for availability of multifactor authentication provider [{}] for [{}]", provider, registeredService);
        if (provider != null) {
            LOGGER.debug("Provider [{}] is successfully verified", provider);
            val id = provider.getId();
            val eventAttrMap = MultifactorAuthenticationUtils.buildEventAttributeMap(principal,
                Optional.ofNullable(service), Optional.ofNullable(registeredService), provider);
            val event = MultifactorAuthenticationUtils.validateEventIdForMatchingTransitionInContext(id, context, eventAttrMap);
            return CollectionUtils.wrapSet(event);
        }
        LOGGER.debug("Provider could not be verified");
        return new HashSet<>();
    }

    /**
     * Gets all multifactor authentication providers from application context.
     *
     * @param applicationContext the application context
     * @return the all multifactor authentication providers from application context
     */
    public static Map<String, MultifactorAuthenticationProvider> getAvailableMultifactorAuthenticationProviders(final ApplicationContext applicationContext) {
        return BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, MultifactorAuthenticationProvider.class);
    }

    /**
     * Method returns an Optional that will contain a {@link MultifactorAuthenticationProvider} that has the
     * same id as the passed providerId parameter.
     *
     * @param providerId - the id to match
     * @param context    - ApplicationContext
     * @return - Optional
     */
    public static Optional<MultifactorAuthenticationProvider> getMultifactorAuthenticationProviderById(
        final String providerId, final ApplicationContext context) {
        return getAvailableMultifactorAuthenticationProviders(context)
            .values()
            .stream()
            .filter(provider -> StringUtils.isNotBlank(providerId) && provider.matches(providerId))
            .findFirst();
    }
}
