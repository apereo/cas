package org.apereo.cas.web.flow.resolver.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This is {@link AbstractCasWebflowEventResolver} that provides parent
 * operations for all child event resolvers to handle core webflow changes.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCasWebflowEventResolver implements CasWebflowEventResolver {

    private static final String RESOLVED_AUTHENTICATION_EVENTS = "resolvedAuthenticationEvents";
    private static final String DEFAULT_MESSAGE_BUNDLE_PREFIX = "authenticationFailure.";

    /**
     * CAS event publisher.
     */
    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    /**
     * The Application context.
     */
    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    /**
     * The Authentication system support.
     */
    protected final AuthenticationSystemSupport authenticationSystemSupport;

    /**
     * The Central authentication service.
     */
    protected final CentralAuthenticationService centralAuthenticationService;

    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Ticket registry support.
     */
    protected final TicketRegistrySupport ticketRegistrySupport;

    /**
     * Warn cookie generator.
     */
    protected final CookieGenerator warnCookieGenerator;

    /**
     * Extract the service specially in the event that it's proxied by a callback.
     */
    protected final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    /**
     * The mfa selector.
     */
    protected final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    /**
     * New event based on the id, which contains an error attribute referring to the exception occurred.
     *
     * @param id    the id
     * @param error the error
     * @return the event
     */
    protected Event newEvent(final String id, final Exception error) {
        return newEvent(id, new LocalAttributeMap(CasWebflowConstants.TRANSITION_ID_ERROR, error));
    }

    /**
     * New event event.
     *
     * @param id the id
     * @return the event
     */
    protected Event newEvent(final String id) {
        return newEvent(id, new LocalAttributeMap<>());
    }

    /**
     * New event based on the given id.
     *
     * @param id         the id
     * @param attributes the attributes
     * @return the event
     */
    protected Event newEvent(final String id, final AttributeMap attributes) {
        return new Event(this, id, attributes);
    }


    /**
     * Gets credential from context.
     *
     * @param context the context
     * @return the credential from context
     */
    protected Credential getCredentialFromContext(final RequestContext context) {
        return WebUtils.getCredential(context);
    }

    /**
     * Grant ticket granting ticket.
     *
     * @param context                     the context
     * @param authenticationResultBuilder the authentication result builder
     * @param service                     the service
     * @return the event
     */
    protected Event grantTicketGrantingTicketToAuthenticationResult(final RequestContext context,
                                                                    final AuthenticationResultBuilder authenticationResultBuilder,
                                                                    final Service service) {
        WebUtils.putAuthenticationResultBuilder(authenticationResultBuilder, context);
        WebUtils.putService(context, service);
        return newEvent(CasWebflowConstants.TRANSITION_ID_SUCCESS);
    }

    /**
     * Gets authentication provider for service.
     *
     * @param service the service
     * @return the authentication provider for service
     */
    protected Collection<MultifactorAuthenticationProvider> getAuthenticationProviderForService(final RegisteredService service) {
        final RegisteredServiceMultifactorPolicy policy = service.getMultifactorPolicy();
        if (policy != null) {
            return policy.getMultifactorAuthenticationProviders().stream()
                .map(this::getMultifactorAuthenticationProviderFromApplicationContext)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toSet());
        }
        return null;
    }

    /**
     * Validate event for transition.
     *
     * @param eventId    the event id
     * @param context    the context
     * @param attributes the attributes
     * @return the event
     */
    @SneakyThrows
    protected Event validateEventIdForMatchingTransitionInContext(final String eventId, final RequestContext context,
                                                                  final Map<String, Object> attributes) {

        final AttributeMap<Object> attributesMap = new LocalAttributeMap<>(attributes);
        final Event event = new Event(this, eventId, attributesMap);

        LOGGER.debug("Resulting event id is [{}] by provider [{}]. Locating transitions in the context for that event id...",
            event.getId(), getName());

        final TransitionDefinition def = context.getMatchingTransition(event.getId());
        if (def == null) {
            LOGGER.warn("Transition definition cannot be found for event [{}]", event.getId());
            throw new AuthenticationException();
        }
        LOGGER.debug("Found matching transition [{}] with target [{}] for event [{}] with attributes [{}].",
            def.getId(), def.getTargetStateId(), event.getId(), event.getAttributes());
        return event;

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
        final Map<String, Object> map = new HashMap<>();
        map.put(Principal.class.getName(), principal);
        if (service != null) {
            map.put(RegisteredService.class.getName(), service);
        }
        map.put(MultifactorAuthenticationProvider.class.getName(), provider);
        return map;
    }

    private Set<Event> resolveEventViaMultivaluedAttribute(final Principal principal,
                                                           final Object attributeValue,
                                                           final RegisteredService service,
                                                           final RequestContext context,
                                                           final MultifactorAuthenticationProvider provider,
                                                           final Predicate<String> predicate) {
        final Set<Event> events = new HashSet<>();
        if (attributeValue instanceof Collection) {
            LOGGER.debug("Attribute value [{}] is a multi-valued attribute", attributeValue);
            final Collection<String> values = (Collection<String>) attributeValue;
            values.forEach(value -> {
                try {
                    if (predicate.test(value)) {
                        LOGGER.debug("Attribute value predicate [{}] has successfully matched the [{}]. "
                            + "Attempting to verify multifactor authentication for [{}]", predicate, value, service);
                        final String id = provider.getId();
                        final Event event = validateEventIdForMatchingTransitionInContext(id, context,
                            buildEventAttributeMap(principal, service, provider));
                        events.add(event);
                    } else {
                        LOGGER.debug("Attribute value predicate [{}] could not match the [{}]", predicate, value);
                    }
                } catch (final Exception e) {
                    LOGGER.debug("Ignoring [{}] since no matching transition could be found", value);
                }
            });
            return events;
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
        LOGGER.debug("Provider [{}] is successfully verified", provider);
        final String id = provider.getId();
        final Event event = validateEventIdForMatchingTransitionInContext(id, context, buildEventAttributeMap(principal, service, provider));
        return CollectionUtils.wrapSet(event);
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
        for (final String attributeName : attributeNames) {
            final Object attributeValue = attributesToExamine.get(attributeName);
            if (attributeValue == null) {
                LOGGER.debug("Attribute value for [{}] to determine event is not configured for [{}]", attributeName, principal.getId());
                continue;
            }

            LOGGER.debug("Selecting a multifactor authentication provider out of [{}] for [{}] and service [{}]", providers, principal.getId(), service);
            final MultifactorAuthenticationProvider provider =
                this.multifactorAuthenticationProviderSelector.resolve(providers, service, principal);

            LOGGER.debug("Located attribute value [{}] for [{}]", attributeValue, attributeNames);

            Set<Event> results = resolveEventViaSingleAttribute(principal, attributeValue, service, context, provider, predicate);
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

        final Map<String, Object> attributes = getPrincipalAttributesForMultifactorAuthentication(principal);
        return resolveEventViaAttribute(principal, attributes, attributeNames, service, context, providers, predicate);
    }

    @Override
    public Set<Event> resolve(final RequestContext context) {
        LOGGER.debug("Attempting to resolve authentication event using resolver [{}]", getName());
        WebUtils.putWarnCookieIfRequestParameterPresent(this.warnCookieGenerator, context);
        WebUtils.putPublicWorkstationToFlowIfRequestParameterPresent(context);
        return resolveInternal(context);
    }

    @Override
    public Event resolveSingle(final RequestContext context) {
        final Set<Event> events = resolve(context);
        if (events == null || events.isEmpty()) {
            return null;
        }
        final Event event = events.iterator().next();
        LOGGER.debug("Resolved single event [{}] via [{}] for this context", event.getId(), event.getSource().getClass().getName());
        return event;
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
     * Put resolved events as attribute.
     *
     * @param context        the context
     * @param resolvedEvents the resolved events
     */
    protected void putResolvedEventsAsAttribute(final RequestContext context, final Set<Event> resolvedEvents) {
        context.getAttributes().put(RESOLVED_AUTHENTICATION_EVENTS, resolvedEvents);
    }

    /**
     * Resolve service from authentication request.
     *
     * @param service the service
     * @return the service
     */
    protected Service resolveServiceFromAuthenticationRequest(final Service service) {
        return this.authenticationRequestServiceSelectionStrategies.resolveService(service);
    }

    /**
     * Resolve service from authentication request service.
     *
     * @param context the context
     * @return the service
     */
    protected Service resolveServiceFromAuthenticationRequest(final RequestContext context) {
        final Service ctxService = WebUtils.getService(context);
        return resolveServiceFromAuthenticationRequest(ctxService);
    }

    /**
     * Gets resolved events as attribute.
     *
     * @param context the context
     * @return the resolved events as attribute
     */
    protected Set<Event> getResolvedEventsAsAttribute(final RequestContext context) {
        return context.getAttributes().get(RESOLVED_AUTHENTICATION_EVENTS, Set.class);
    }

    /**
     * Handle authentication transaction and grant ticket granting ticket.
     *
     * @param context the context
     * @return the set
     */
    protected Set<Event> handleAuthenticationTransactionAndGrantTicketGrantingTicket(final RequestContext context) {
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        try {
            final Credential credential = getCredentialFromContext(context);
            AuthenticationResultBuilder builder = WebUtils.getAuthenticationResultBuilder(context);
            LOGGER.debug("Handling authentication transaction for credential [{}]", credential);
            final Service service = WebUtils.getService(context);
            builder = this.authenticationSystemSupport.handleAuthenticationTransaction(service, builder, credential);
            LOGGER.debug("Issuing ticket-granting tickets for service [{}]", service);
            return CollectionUtils.wrapSet(grantTicketGrantingTicketToAuthenticationResult(context, builder, service));
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            final MessageContext messageContext = context.getMessageContext();
            messageContext.addMessage(new MessageBuilder()
                .error()
                .code(DEFAULT_MESSAGE_BUNDLE_PREFIX.concat(e.getClass().getSimpleName()))
                .build());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return CollectionUtils.wrapSet(new EventFactorySupport().error(this));
        }
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
