package org.jasig.cas.web.flow.authentication;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
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
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceMultifactorAuthenticationProvider;
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
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link AbstractCasWebflowEventResolver}.
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

        final AuthenticationResult authenticationResult =
                authenticationSystemSupport.finalizeAllAuthenticationTransactions(authenticationResultBuilder, service);
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
    protected RegisteredServiceMultifactorAuthenticationProvider getAuthenticationProviderForService(final RegisteredService service) {
        final Set<String> providers = service.getAuthenticationPolicy().getMultifactorAuthenticationProviders();
        for (final String provider : providers) {
            final RegisteredServiceMultifactorAuthenticationProvider providerClass = loadMultifactorAuthenticationProvider(provider);
            if (providerClass != null) {
                return providerClass;
            }
        }
        return null;
    }


    /**
     * Validate event for transition.
     *
     * @param eventId the event id
     * @param context the context
     * @return the event
     * @throws Exception the exception
     */
    protected Event validateEventIdForMatchingTransitionInContext(final String eventId, final RequestContext context) throws Exception {
        final Event event = new Event(this, eventId);
        logger.debug("Resulting event id is [{}]. Locating transitions in the context for that event id...",
                event.getId());

        final TransitionDefinition def = context.getMatchingTransition(event.getId());
        if (def == null) {
            logger.warn("Transition definition cannot be found for event [{}]", event.getId());
            throw new AuthenticationException();
        }
        logger.debug("Found matching transition [{}] with target [{}] for event [{}].",
                def.getId(), def.getTargetStateId(), event.getId());
        return event;
    }

    /**
     * Resolve event via principal attribute set.
     *
     * @param principal     the principal
     * @param attributeName the attribute name
     * @param service       the service
     * @param context       the context
     * @param predicate     the predicate
     * @return the set
     */
    protected Set<Event> resolveEventViaPrincipalAttribute(final Principal principal,
                                                           final String attributeName,
                                                           final RegisteredService service,
                                                           final RequestContext context,
                                                           final Predicate predicate) {

        final RegisteredServiceMultifactorAuthenticationProvider provider = getAuthenticationProviderForService(service);
        if (provider == null) {
            logger.debug("No authentication provider is associated with this service");
            return null;
        }

        final Object attributeValue = principal.getAttributes().get(attributeName);
        if (attributeValue == null) {
            logger.debug("Attribute value for {} to determine event is not configured for {}", attributeName, principal.getId());
            return null;
        }

        try {
            if (attributeValue instanceof String) {
                if (predicate.apply(attributeValue)) {
                    final String id = provider.provide(service);
                    final Event event = validateEventIdForMatchingTransitionInContext(id, context);
                    return ImmutableSet.of(event);
                }
                return null;
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        final ImmutableSet.Builder<Event> builder = ImmutableSet.builder();
        if (attributeValue instanceof List) {
            final List<String> values = (List<String>) attributeValue;
            for (final String value : values) {
                try {
                    if (predicate.apply(value)) {
                        final String id = provider.provide(service);
                        final Event event = validateEventIdForMatchingTransitionInContext(id, context);
                        builder.add(event);
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
        return events.iterator().next();
    }

    public void setWarnCookieGenerator(final CookieGenerator warnCookieGenerator) {
        this.warnCookieGenerator = warnCookieGenerator;
    }


    /**
     * Resolve event per authentication provider event.
     *
     * @param context  the context
     * @param service  the service
     * @return the event
     */
    protected Set<Event> resolveEventPerAuthenticationProvider(final RequestContext context, final RegisteredService service) {

        try {
            final RegisteredServiceMultifactorAuthenticationProvider provider = getAuthenticationProviderForService(service);
            if (provider != null) {
                final String identifier = provider.provide(service);
                if (StringUtils.isBlank(identifier)) {
                    logger.warn("Multifactor authentication provider {} could not provide CAS with its identifier.", provider);
                    return null;
                }
                logger.debug("Attempting to build an event based on the authentication provider [{}] and service [{}]",
                        provider, service.getName());

                final Event event = validateEventIdForMatchingTransitionInContext(identifier, context);
                return ImmutableSet.of(event);
            }
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
    protected RegisteredServiceMultifactorAuthenticationProvider loadMultifactorAuthenticationProvider(final String provider) {
        try {
            return this.applicationContext.getBean(provider, RegisteredServiceMultifactorAuthenticationProvider.class);
        } catch (final Exception e) {
            logger.warn(e.getMessage(), e);
        }

        logger.warn("Could not locate [{}] bean id in the application context as an authentication provider. "
                + "Are you missing a dependency in your configuration?", provider);
        throw new IllegalArgumentException("Could not locate " + provider + " in the application configuration",
                new AuthenticationException());

    }
}
