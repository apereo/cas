package org.apereo.cas.authentication;

import com.google.common.collect.Lists;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.support.events.CasAuthenticationPrincipalResolvedEvent;
import org.apereo.cas.support.events.CasAuthenticationTransactionStartedEvent;
import org.apereo.cas.support.events.CasAuthenticationTransactionSuccessfulEvent;
import org.apereo.inspektr.audit.annotation.Audit;

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.Assert;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link AbstractAuthenticationManager}, which provides common operations
 * around an authentication manager implementation.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractAuthenticationManager implements AuthenticationManager {
    private static final String MESSAGE = "At least one authentication handler is required";
    
    /**
     * Log instance for logging events, errors, warnings, etc.
     */
    protected transient Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * An array of AuthenticationAttributesPopulators.
     */
    protected List<AuthenticationMetaDataPopulator> authenticationMetaDataPopulators =
            new ArrayList<>();

    /**
     * Map of authentication handlers to resolvers to be used when handler does not resolve a principal.
     */
    protected Map<AuthenticationHandler, PrincipalResolver> handlerResolverMap;

    /**
     * The Authentication handler resolver.
     */
    protected AuthenticationHandlerResolver authenticationHandlerResolver =
            new RegisteredServiceAuthenticationHandlerResolver();

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Instantiates a new Policy based authentication manager.
     */
    protected AbstractAuthenticationManager() {
    }

    /**
     * Creates a new authentication manager with a varargs array of authentication handlers that are attempted in the
     * listed order for supported credentials. This form may only be used by authentication handlers that
     * resolve principals during the authentication process.
     *
     * @param handlers One or more authentication handlers.
     */
    protected AbstractAuthenticationManager(final AuthenticationHandler... handlers) {
        this(Lists.newArrayList(handlers));
    }

    /**
     * Creates a new authentication manager with a list of authentication handlers that are attempted in the
     * listed order for supported credentials. This form may only be used by authentication handlers that
     * resolve principals during the authentication process.
     *
     * @param handlers Non-null list of authentication handlers containing at least one entry.
     */
    protected AbstractAuthenticationManager(final List<AuthenticationHandler> handlers) {
        Assert.notEmpty(handlers, MESSAGE);
        this.handlerResolverMap = new LinkedHashMap<>(handlers.size());
        for (final AuthenticationHandler handler : handlers) {
            this.handlerResolverMap.put(handler, null);
        }
    }

    /**
     * Creates a new authentication manager with a map of authentication handlers to the principal resolvers that
     * should be used upon successful authentication if no principal is resolved by the authentication handler. If
     * the order of evaluation of authentication handlers is important, a map that preserves insertion order
     * (e.g. {@link LinkedHashMap}) should be used.
     *
     * @param map Non-null map of authentication handler to principal resolver containing at least one entry.
     */
    protected AbstractAuthenticationManager(final Map<AuthenticationHandler, PrincipalResolver> map) {
        Assert.notEmpty(map, MESSAGE);
        this.handlerResolverMap = map;
    }

    /**
     * Populate authentication metadata attributes.
     *
     * @param builder     the builder
     * @param credentials the credentials
     */
    protected void populateAuthenticationMetadataAttributes(final AuthenticationBuilder builder,
                                                            final Collection<Credential> credentials) {
        for (final AuthenticationMetaDataPopulator populator : this.authenticationMetaDataPopulators) {
            credentials.stream().filter(populator::supports).forEach(credential -> populator.populateAttributes(builder, credential));
        }
    }

    /**
     * Add authentication method attribute.
     *
     * @param builder        the builder
     * @param authentication the authentication
     */
    protected void addAuthenticationMethodAttribute(final AuthenticationBuilder builder,
                                                    final Authentication authentication) {
        for (final HandlerResult result : authentication.getSuccesses().values()) {
            builder.addAttribute(AUTHENTICATION_METHOD_ATTRIBUTE, result.getHandlerName());
        }
    }

    /**
     * Resolve principal.
     *
     * @param handlerName the handler name
     * @param resolver    the resolver
     * @param credential  the credential
     * @return the principal
     */
    protected Principal resolvePrincipal(
            final String handlerName, final PrincipalResolver resolver, final Credential credential) {
        if (resolver.supports(credential)) {
            try {
                final Principal p = resolver.resolve(credential);
                logger.debug("{} resolved {} from {}", resolver, p, credential);
                return p;
            } catch (final Exception e) {
                logger.error("{} failed to resolve principal from {}", resolver, credential, e);
            }
        } else {
            logger.warn(
                    "{} is configured to use {} but it does not support {}, which suggests a configuration problem.",
                    handlerName,
                    resolver,
                    credential);
        }
        return null;
    }

    @Override
    @Audit(
            action = "AUTHENTICATION",
            actionResolverName = "AUTHENTICATION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_RESOURCE_RESOLVER")
    @Timed(name = "AUTHENTICATE_TIMER")
    @Metered(name = "AUTHENTICATE_METER")
    @Counted(name = "AUTHENTICATE_COUNT", monotonic = true)
    public Authentication authenticate(final AuthenticationTransaction transaction) throws AuthenticationException {
        CurrentCredentialsAndAuthentication.bindCurrent(transaction.getCredentials());
        final AuthenticationBuilder builder = authenticateInternal(transaction);
        final Authentication authentication = builder.build();
        final Principal principal = authentication.getPrincipal();
        if (principal instanceof NullPrincipal) {
            throw new UnresolvedPrincipalException(authentication);
        }

        addAuthenticationMethodAttribute(builder, authentication);

        logger.info("Authenticated principal [{}] and attributes {} with credentials {}.", 
                principal.getId(), principal.getAttributes(), transaction.getCredentials());
        populateAuthenticationMetadataAttributes(builder, transaction.getCredentials());

        final Authentication a = builder.build();
        CurrentCredentialsAndAuthentication.bindCurrent(a);
        return a;
    }

    /**
     * Authenticate and resolve principal.
     *
     * @param builder    the builder
     * @param credential the credential
     * @param resolver   the resolver
     * @param handler    the handler
     * @throws GeneralSecurityException the general security exception
     * @throws PreventedException       the prevented exception
     */
    protected void authenticateAndResolvePrincipal(final AuthenticationBuilder builder, final Credential credential,
                                                   final PrincipalResolver resolver, final AuthenticationHandler handler)
            throws GeneralSecurityException, PreventedException {

        Principal principal;
        
        publishEvent(new CasAuthenticationTransactionStartedEvent(this, credential));
        
        final HandlerResult result = handler.authenticate(credential);
        builder.addSuccess(handler.getName(), result);
        logger.info("{} successfully authenticated {}", handler.getName(), credential);

        publishEvent(new CasAuthenticationTransactionSuccessfulEvent(this, credential));
        
        if (resolver == null) {
            principal = result.getPrincipal();
            logger.debug(
                    "No resolver configured for {}. Falling back to handler principal {}",
                    handler.getName(),
                    principal);
        } else {
            principal = resolvePrincipal(handler.getName(), resolver, credential);
            if (principal == null) {
                logger.warn("Principal resolution handled by {} produced a null principal. "
                        + "This is likely due to misconfiguration or missing attributes; CAS will attempt to use the principal "
                        + "produced by the authentication handler, if any.", resolver.getClass().getSimpleName());
                principal = result.getPrincipal();
            }
        }
        if (principal != null) {
            builder.setPrincipal(principal);
        }
        logger.debug("Final principal resolved for this authentication event is {}", principal);
        publishEvent(new CasAuthenticationPrincipalResolvedEvent(this, principal));
    }

    /**
     * Follows the same contract as {@link AuthenticationManager#authenticate(AuthenticationTransaction)}.
     *
     * @param transaction the authentication transaction
     * @return An authentication containing a resolved principal and metadata about successful and failed authentications.
     * There SHOULD be a record of each attempted authentication, whether success or failure.
     * @throws AuthenticationException When one or more credentials failed authentication such that security policy was not satisfied.
     */
    protected abstract AuthenticationBuilder authenticateInternal(AuthenticationTransaction transaction)
            throws AuthenticationException;

    /**
     * Sets the authentication metadata populators that will be applied to every successful authentication event.
     *
     * @param populators Non-null list of metadata populators.
     */
    public void setAuthenticationMetaDataPopulators(final List<AuthenticationMetaDataPopulator> populators) {
        this.authenticationMetaDataPopulators = populators;
    }

    public void setAuthenticationHandlerResolver(final AuthenticationHandlerResolver authenticationHandlerResolver) {
        this.authenticationHandlerResolver = authenticationHandlerResolver;
    }

    public void setHandlerResolverMap(final Map<AuthenticationHandler, PrincipalResolver> handlerResolverMap) {
        this.handlerResolverMap = handlerResolverMap;
    }
    
    private void publishEvent(final ApplicationEvent event) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(event);
        }
    }
}
