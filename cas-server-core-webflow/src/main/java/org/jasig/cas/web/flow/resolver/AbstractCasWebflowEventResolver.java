package org.jasig.cas.web.flow.resolver;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.AuthenticationResult;
import org.jasig.cas.authentication.AuthenticationResultBuilder;
import org.jasig.cas.authentication.AuthenticationSystemSupport;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultAuthenticationSystemSupport;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.MessageDescriptor;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.MultifactorAuthenticationProvider;
import org.jasig.cas.services.MultifactorAuthenticationProviderSelector;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAuthenticationPolicy;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.flow.CasWebflowConstants;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.core.collection.AttributeMap;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link AbstractCasWebflowEventResolver} that provides parent
 * operations for all child event resolvers to handle core webflow changes.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public abstract class AbstractCasWebflowEventResolver implements CasWebflowEventResolver {
    /**
     * Authentication succeeded with warnings from authn subsystem that should be displayed to user.
     */
    private static final String SUCCESS_WITH_WARNINGS = "successWithWarnings";

    /**
     * The Logger.
     */
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * The Application context.
     */
    @Autowired
    protected WebApplicationContext applicationContext;

    /**
     * The Authentication system support.
     */
    @NotNull
    @Autowired(required = false)
    @Qualifier("defaultAuthenticationSystemSupport")
    protected AuthenticationSystemSupport authenticationSystemSupport = new DefaultAuthenticationSystemSupport();

    /**
     * The Services manager.
     */
    @NotNull
    @Autowired
    @Qualifier("servicesManager")
    protected ServicesManager servicesManager;

    /**
     * The Central authentication service.
     */
    @NotNull
    @Autowired
    @Qualifier("centralAuthenticationService")
    protected CentralAuthenticationService centralAuthenticationService;

    /**
     * Warn cookie generator.
     */
    @NotNull
    @Autowired
    @Qualifier("warnCookieGenerator")
    protected CookieGenerator warnCookieGenerator;

    /** The mfa selector. */
    @Autowired
    @Qualifier("multifactorAuthenticationProviderSelector")
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
                .args(warning.getParams());
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
    protected boolean addWarningMessagesToMessageContextIfNeeded(final TicketGrantingTicket tgtId, final MessageContext messageContext) {
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
        return new Event(this, id, new LocalAttributeMap("error", error));
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
                authenticationSystemSupport.finalizeAllAuthenticationTransactions(authenticationResultBuilder, service);
        logger.debug("Resulting final authentication is {} with principal {}", authenticationResult.getAuthentication(),
                authenticationResult.getPrincipal());
        final TicketGrantingTicket tgt = this.centralAuthenticationService.createTicketGrantingTicket(authenticationResult);
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);
        WebUtils.putAuthenticationResult(authenticationResult, context);

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
        final RegisteredServiceAuthenticationPolicy policy = service.getAuthenticationPolicy();
        if (policy != null) {
            final Set<String> providers = policy.getMultifactorAuthenticationProviders();
            final Set<MultifactorAuthenticationProvider> providersSet = new HashSet<>(providers.size());

            for (final String provider : providers) {
                final MultifactorAuthenticationProvider providerInst = getMultifactorAuthenticationProviderFromApplicationContext(provider);
                if (providerInst != null) {
                    logger.debug("Added multifactor authentication provider {} as an available provider for {}", providerInst, service);
                    providersSet.add(providerInst);
                }
            }
            return providersSet;
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
                                                                  final Map<String, Object> attributes)  {
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
            throw new RuntimeException(e);
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

    /**
     * Resolve event via principal attribute set.
     *
     * @param principal     the principal
     * @param attributeName the attribute name
     * @param service       the service
     * @param context       the context
     * @param providers     the providers
     * @param predicate     the predicate
     * @return the set of resolved events
     */
    protected Set<Event> resolveEventViaPrincipalAttribute(final Principal principal,
                                                           final String attributeName,
                                                           final RegisteredService service,
                                                           final RequestContext context,
                                                           final Set<MultifactorAuthenticationProvider> providers,
                                                           final Predicate predicate) {

        logger.debug("Locating principal attribute value for {}", attributeName);
        final Object attributeValue = principal.getAttributes().get(attributeName);
        if (attributeValue == null) {
            logger.debug("Attribute value for {} to determine event is not configured for {}", attributeName, principal.getId());
            return null;
        }

        if (providers == null || providers.isEmpty()) {
            logger.debug("No authentication provider is associated with this service");
            return null;
        }

        logger.debug("Selecting a multifactor authentication provider out of {} for {} and service {}",
                providers, principal.getId(), service);
        final MultifactorAuthenticationProvider provider = multifactorAuthenticationProviderSelector.resolve(providers, service, principal);
        try {
            if (attributeValue instanceof String) {
                logger.debug("Attribute value {} is a single-valued attribute", attributeValue);
                if (predicate.apply(attributeValue)) {
                    logger.debug("Attribute value predicate {} has successfully matched the [{}]", predicate, attributeValue);
                    if (provider.verify(service)) {
                        final String id = provider.getId();
                        final Event event = validateEventIdForMatchingTransitionInContext(id, context,
                                buildEventAttributeMap(principal, service, provider));
                        return ImmutableSet.of(event);
                    }
                }
                return null;
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        final ImmutableSet.Builder<Event> builder = ImmutableSet.builder();
        if (attributeValue instanceof List) {
            logger.debug("Attribute value {} is a multi-valued attribute", attributeValue);
            final List<String> values = (List<String>) attributeValue;
            for (final String value : values) {
                try {
                    if (predicate.apply(value)) {
                        logger.debug("Attribute value predicate {} has successfully matched the [{}]", predicate, value);
                        if (provider.verify(service)) {
                            final String id = provider.getId();
                            final Event event = validateEventIdForMatchingTransitionInContext(id, context,
                                    buildEventAttributeMap(principal, service, provider));
                            builder.add(event);
                        }
                    }
                } catch (final Exception e) {
                    logger.debug("Ignoring {} since no matching transition could be found", value);
                }
            }
            return builder.build();
        }
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
    protected abstract Set<Event> resolveInternal(final RequestContext context);

    @Override
    public Event resolveSingle(final RequestContext context) {
        final Set<Event> events = resolve(context);
        if (events == null || events.isEmpty()) {
            return null;
        }
        final Event event = events.iterator().next();
        logger.info("Resolved single event [{}] via [{}] for this context", event.getId(),
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
                        multifactorAuthenticationProviderSelector.resolve(providers, service, principal);

                logger.debug("Selected multifactor authentication provider for this transaction is {}", provider);

                if (!provider.verify(service)) {
                    logger.warn("Multifactor authentication provider {} could not be verified.", provider);
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
            throw new RuntimeException(e);
        }
    }

    /**
     * Load multifactor authentication provider registered service multifactor authentication provider.
     *
     * @param provider the provider
     * @return the registered service multifactor authentication provider
     */
    protected MultifactorAuthenticationProvider getMultifactorAuthenticationProviderFromApplicationContext(final String provider) {
        try {
            logger.debug("Locating bean definition for {}", provider);
            return this.applicationContext.getBean(provider, MultifactorAuthenticationProvider.class);
        } catch (final Exception e) {
            logger.debug("Could not locate [{}] bean id in the application context as an authentication provider.", provider);
        }
        return null;

    }

    /**
     * Gets all multifactor authentication providers from application context.
     *
     * @return the all multifactor authentication providers from application context
     */
    protected Map<String, MultifactorAuthenticationProvider> getAllMultifactorAuthenticationProvidersFromApplicationContext() {
        try {
            return this.applicationContext.getBeansOfType(MultifactorAuthenticationProvider.class);
        } catch (final Exception e) {
            logger.warn("Could not locate beans of type {} in the application context", MultifactorAuthenticationProvider.class);
        }
        return null;
    }
    /**
     * Put resolved events as attribute.
     *
     * @param context        the context
     * @param resolvedEvents the resolved events
     */
    protected void putResolvedEventsAsAttribute(final RequestContext context, final Set<Event> resolvedEvents) {
        context.getAttributes().put("resolvedAuthenticationEvents", resolvedEvents);
    }

    /**
     * Gets resolved events as attribute.
     *
     * @param context the context
     * @return the resolved events as attribute
     */
    protected Set<Event> getResolvedEventsAsAttribute(final RequestContext context) {
        return context.getAttributes().get("resolvedAuthenticationEvents", Set.class);
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
}
