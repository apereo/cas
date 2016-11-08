package org.apereo.cas.web.flow.resolver.impl;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationResultBuilder;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.MessageDescriptor;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link AbstractCasWebflowEventResolver} that provides parent
 * operations for all child event resolvers to handle core webflow changes.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractCasWebflowEventResolver implements CasWebflowEventResolver {
    /**
     * Authentication succeeded with warnings from authn subsystem that should be displayed to user.
     */
    private static final String SUCCESS_WITH_WARNINGS = "successWithWarnings";
    private static final String RESOLVED_AUTHENTICATION_EVENTS = "resolvedAuthenticationEvents";

    protected transient Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The Application context.
     */
    @Autowired
    protected ConfigurableApplicationContext applicationContext;

    /**
     * The Authentication system support.
     */
    protected AuthenticationSystemSupport authenticationSystemSupport;


    /**
     * Ticket registry support.
     */
    protected TicketRegistrySupport ticketRegistrySupport;

    /**
     * The Services manager.
     */
    protected ServicesManager servicesManager;

    /**
     * The Central authentication service.
     */
    protected CentralAuthenticationService centralAuthenticationService;

    /**
     * Warn cookie generator.
     */
    protected CookieGenerator warnCookieGenerator;

    /**
     * The mfa selector.
     */
    protected MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    /**
     * Adds a warning message to the message context.
     *
     * @param context Message context.
     * @param warning Warning message.
     */
    protected static void addMessageDescriptorToMessageContext(final MessageContext context, final MessageDescriptor warning) {
        final MessageBuilder builder = new MessageBuilder()
                .warning()
                .code(warning.getCode())
                .defaultText(warning.getDefaultMessage())
                .args((Object[]) warning.getParams());
        context.addMessage(builder.build());
    }

    /**
     * New event based on the given id.
     *
     * @param id the id
     * @return the event
     */
    protected Event newEvent(final String id) {
        return new Event(this, id);
    }

    /**
     * Add warning messages to message context if needed.
     *
     * @param tgtId          the tgt id
     * @param messageContext the message context
     * @return true if warnings were found and added, false otherwise.
     * @since 4.1.0
     */
    private static boolean addWarningMessagesToMessageContextIfNeeded(final TicketGrantingTicket tgtId,
                                                                 final MessageContext messageContext) {
        boolean foundAndAddedWarnings = false;
        for (final Map.Entry<String, HandlerResult> entry : tgtId.getAuthentication().getSuccesses().entrySet()) {
            for (final MessageDescriptor message : entry.getValue().getWarnings()) {
                addMessageDescriptorToMessageContext(messageContext, message);
                foundAndAddedWarnings = true;
            }
        }
        return foundAndAddedWarnings;

    }

    /**
     * New event based on the id, which contains an error attribute referring to the exception occurred.
     *
     * @param id    the id
     * @param error the error
     * @return the event
     */
    protected Event newEvent(final String id, final Exception error) {
        return new Event(this, id, new LocalAttributeMap(CasWebflowConstants.TRANSITION_ID_ERROR, error));
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
     * @throws Exception the exception
     */
    protected Event grantTicketGrantingTicketToAuthenticationResult(final RequestContext context,
                                                                    final AuthenticationResultBuilder authenticationResultBuilder,
                                                                    final Service service) throws Exception {

        logger.debug("Finalizing authentication transactions and issuing ticket-granting ticket");
        final AuthenticationResult authenticationResult =
                this.authenticationSystemSupport.finalizeAllAuthenticationTransactions(authenticationResultBuilder, service);

        boolean issueTicketGrantingTicket = true;
        final Authentication authentication = authenticationResult.getAuthentication();
        final String ticketGrantingTicket = WebUtils.getTicketGrantingTicketId(context);
        if (StringUtils.isNotBlank(ticketGrantingTicket)) {
            logger.debug("Located ticket-granting ticket in the context. Retrieving associated authentication");
            final Authentication authenticationFromTgt = this.ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicket);
            if (authenticationFromTgt == null) {
                logger.debug("Authentication session associated with {} is no longer valid", ticketGrantingTicket);
                this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicket);
            } else if (authentication.getPrincipal().equals(authenticationFromTgt.getPrincipal())) {
                logger.debug("Resulting authentication matches the authentication from context");
                issueTicketGrantingTicket = false;
            } else {
                logger.debug("Resulting authentication is different from the context");
            }
        }

        final TicketGrantingTicket tgt;
        if (issueTicketGrantingTicket) {
            tgt = this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult);

        } else {
            tgt = this.centralAuthenticationService.getTicket(ticketGrantingTicket, TicketGrantingTicket.class);
            tgt.getAuthentication().update(authentication);
            this.centralAuthenticationService.updateTicket(tgt);
        }


        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putAuthenticationResult(authenticationResult, context);
        WebUtils.putAuthentication(tgt.getAuthentication(), context);

        if (addWarningMessagesToMessageContextIfNeeded(tgt, context.getMessageContext())) {
            return newEvent(SUCCESS_WITH_WARNINGS);
        }

        return newEvent(CasWebflowConstants.TRANSITION_ID_SUCCESS);
    }

    /**
     * Gets authentication provider for service.
     *
     * @param service the service
     * @return the authentication provider for service
     */
    protected Set<MultifactorAuthenticationProvider> getAuthenticationProviderForService(final RegisteredService service) {
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
    protected Event validateEventIdForMatchingTransitionInContext(final String eventId, final RequestContext context,
                                                                  final Map<String, Object> attributes) {
        try {
            final AttributeMap<Object> attributesMap = new LocalAttributeMap<>(attributes);
            final Event event = new Event(this, eventId, attributesMap);

            logger.debug("Resulting event id is [{}]. Locating transitions in the context for that event id...",
                    event.getId());

            final TransitionDefinition def = context.getMatchingTransition(event.getId());
            if (def == null) {
                logger.warn("Transition definition cannot be found for event [{}]", event.getId());
                throw new AuthenticationException();
            }
            logger.debug("Found matching transition [{}] with target [{}] for event [{}] with attributes {}.",
                    def.getId(), def.getTargetStateId(), event.getId(), event.getAttributes());
            return event;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
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
        map.put(RegisteredService.class.getName(), service);
        map.put(MultifactorAuthenticationProvider.class.getName(), provider);
        return map;
    }

    private Set<Event> resolveEventViaMultivaluedPrincipalAttribute(final Principal principal,
                                                                    final Object attributeValue,
                                                                    final RegisteredService service,
                                                                    final RequestContext context,
                                                                    final MultifactorAuthenticationProvider provider,
                                                                    final Predicate<Object> predicate) {
        final ImmutableSet.Builder<Event> builder = ImmutableSet.builder();
        if (attributeValue instanceof Collection) {
            logger.debug("Attribute value {} is a multi-valued attribute", attributeValue);
            final Collection<String> values = (Collection<String>) attributeValue;
            for (final String value : values) {
                try {
                    if (predicate.apply(value)) {
                        logger.debug("Attribute value predicate {} has successfully matched the [{}]", predicate, value);

                        logger.debug("Attempting to verify multifactor authentication provider {} for {}",
                                provider, service);
                        if (provider.isAvailable(service)) {
                            logger.debug("Provider {} is successfully verified", provider);

                            final String id = provider.getId();
                            final Event event = validateEventIdForMatchingTransitionInContext(id, context,
                                    buildEventAttributeMap(principal, service, provider));
                            builder.add(event);
                        }
                    } else {
                        logger.debug("Attribute value predicate {} could not match the [{}]", predicate, value);
                    }
                } catch (final Exception e) {
                    logger.debug("Ignoring {} since no matching transition could be found", value);
                }
            }
            return builder.build();
        }
        logger.debug("Attribute value {} of type {} is not a multi-valued attribute", 
                attributeValue, attributeValue.getClass());
        return null;
    }

    private Set<Event> resolveEventViaSinglePrincipalAttribute(final Principal principal,
                                                               final Object attributeValue,
                                                               final RegisteredService service,
                                                               final RequestContext context,
                                                               final MultifactorAuthenticationProvider provider,
                                                               final Predicate predicate) {

        try {
            if (attributeValue instanceof String) {
                logger.debug("Attribute value {} is a single-valued attribute", attributeValue);
                if (predicate.apply(attributeValue)) {
                    logger.debug("Attribute value predicate {} has matched the [{}]", predicate, attributeValue);

                    logger.debug("Attempting to isAvailable multifactor authentication provider {} for {}",
                            provider, service);

                    if (provider.isAvailable(service)) {
                        logger.debug("Provider {} is successfully verified", provider);
                        final String id = provider.getId();
                        final Event event = validateEventIdForMatchingTransitionInContext(id, context,
                                buildEventAttributeMap(principal, service, provider));
                        return ImmutableSet.of(event);
                    } else {
                        logger.debug("Provider {} could not be verified", provider);
                    }
                } else {
                    logger.debug("Attribute value predicate {} could not match the [{}]", predicate, attributeValue);
                }
            }
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
        logger.debug("Attribute value {} is not a single-valued attribute", attributeValue);
        return null;
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
                                                           final Predicate predicate) {

        if (providers == null || providers.isEmpty()) {
            logger.debug("No authentication provider is associated with this service");
            return null;
        }

        logger.debug("Locating principal attribute value for attribute(s): {}", attributeNames);
        for (final String attributeName : attributeNames) {
            final Object attributeValue = principal.getAttributes().get(attributeName);
            if (attributeValue == null) {
                logger.debug("Attribute value for {} to determine event is not configured for {}",
                        attributeName, principal.getId());
                continue;
            }

            logger.debug("Selecting a multifactor authentication provider out of {} for {} and service {}",
                    providers, principal.getId(), service);
            final MultifactorAuthenticationProvider provider =
                    this.multifactorAuthenticationProviderSelector.resolve(providers, service, principal);

            logger.debug("Located principal attribute value {} for {}", attributeValue, attributeNames);

            Set<Event> results = resolveEventViaSinglePrincipalAttribute(principal, attributeValue,
                    service, context, provider, predicate);
            if (results == null || results.isEmpty()) {
                results = resolveEventViaMultivaluedPrincipalAttribute(principal, attributeValue,
                        service, context, provider, predicate);
            }
            if (results != null && !results.isEmpty()) {
                logger.debug("Resolved set of events based the principal attribute {} are {}",
                        attributeName, results);
                return results;
            }
        }
        logger.debug("No set of events based the principal attribute(s) {} could be matched",
                attributeNames);
        return null;
    }

    @Override
    public Set<Event> resolve(final RequestContext context) {
        WebUtils.putWarnCookieIfRequestParameterPresent(this.warnCookieGenerator, context);
        WebUtils.putPublicWorkstationToFlowIfRequestParameterPresent(context);
        return resolveInternal(context);
    }

    /**
     * Resolve internal event.
     *
     * @param context the context
     * @return the event
     */
    protected abstract Set<Event> resolveInternal(RequestContext context);

    @Override
    public Event resolveSingle(final RequestContext context) {
        final Set<Event> events = resolve(context);
        if (events == null || events.isEmpty()) {
            return null;
        }
        final Event event = events.iterator().next();
        logger.debug("Resolved single event [{}] via [{}] for this context", event.getId(),
                event.getSource().getClass().getName());
        return event;
    }

    public void setWarnCookieGenerator(final CookieGenerator warnCookieGenerator) {
        this.warnCookieGenerator = warnCookieGenerator;
    }


    /**
     * Resolve event per authentication provider event.
     *
     * @param principal the principal
     * @param context   the context
     * @param service   the service
     * @return the event
     */
    protected Set<Event> resolveEventPerAuthenticationProvider(final Principal principal,
                                                               final RequestContext context,
                                                               final RegisteredService service) {

        try {
            final Set<MultifactorAuthenticationProvider> providers = getAuthenticationProviderForService(service);
            if (providers != null && !providers.isEmpty()) {
                final MultifactorAuthenticationProvider provider =
                        this.multifactorAuthenticationProviderSelector.resolve(providers, service, principal);

                logger.debug("Selected multifactor authentication provider for this transaction is {}", provider);

                if (!provider.isAvailable(service)) {
                    logger.warn("Multifactor authentication provider {} could not be verified/reached.", provider);
                    return null;
                }
                final String identifier = provider.getId();
                logger.debug("Attempting to build an event based on the authentication provider [{}] and service [{}]",
                        provider, service.getName());

                final Event event = validateEventIdForMatchingTransitionInContext(identifier, context,
                        buildEventAttributeMap(principal, service, provider));
                return ImmutableSet.of(event);
            }

            logger.debug("No multifactor authentication providers could be located for {}", service);
            return null;

        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Find the MultifactorAuthenticationProvider in the application contact that matches the specified providerId (e.g. "mfa-duo").
     *
     * @param providerId the provider id
     * @return the registered service multifactor authentication provider
     */
    protected Optional<MultifactorAuthenticationProvider> getMultifactorAuthenticationProviderFromApplicationContext(
            final String providerId) {
        try {
            logger.debug("Locating bean definition for {}", providerId);
            return this.applicationContext.getBeansOfType(MultifactorAuthenticationProvider.class, false, true).values().stream()
                    .filter(p -> p.getId().equals(providerId))
                    .findFirst();
        } catch (final Exception e) {
            logger.debug("Could not locate [{}] bean id in the application context as an authentication provider.", providerId);
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
     * Gets resolved events as attribute.
     *
     * @param context the context
     * @return the resolved events as attribute
     */
    protected Set<Event> getResolvedEventsAsAttribute(final RequestContext context) {
        return context.getAttributes().get(RESOLVED_AUTHENTICATION_EVENTS, Set.class);
    }

    /**
     * Handle authentication transaction and grant ticket granting ticket set.
     *
     * @param context the context
     * @return the set
     */
    protected Set<Event> handleAuthenticationTransactionAndGrantTicketGrantingTicket(final RequestContext context) {
        try {
            final Credential credential = getCredentialFromContext(context);
            AuthenticationResultBuilder builder = WebUtils.getAuthenticationResultBuilder(context);

            logger.debug("Handling authentication transaction for credential {}", credential);
            builder = this.authenticationSystemSupport.handleAuthenticationTransaction(builder, credential);
            final Service service = WebUtils.getService(context);

            logger.debug("Issuing ticket-granting tickets for service {}", service);
            return ImmutableSet.of(grantTicketGrantingTicketToAuthenticationResult(context, builder, service));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return ImmutableSet.of(new Event(this, "error"));
        }
    }

    public void setAuthenticationSystemSupport(final AuthenticationSystemSupport authenticationSystemSupport) {
        this.authenticationSystemSupport = authenticationSystemSupport;
    }

    public void setTicketRegistrySupport(final TicketRegistrySupport ticketRegistrySupport) {
        this.ticketRegistrySupport = ticketRegistrySupport;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setCentralAuthenticationService(final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public void setMultifactorAuthenticationProviderSelector(final MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector) {
        this.multifactorAuthenticationProviderSelector = multifactorAuthenticationProviderSelector;
    }
}
