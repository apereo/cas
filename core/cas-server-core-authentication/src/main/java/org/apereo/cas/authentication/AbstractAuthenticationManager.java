package org.apereo.cas.authentication;

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import org.apereo.cas.authentication.exceptions.UnresolvedPrincipalException;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.support.events.authentication.CasAuthenticationPrincipalResolvedEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionStartedEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionSuccessfulEvent;
import org.apereo.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.Assert;

import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * This is {@link AbstractAuthenticationManager}, which provides common operations
 * around an authentication manager implementation.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractAuthenticationManager implements AuthenticationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAuthenticationManager.class);

    /**
     * Plan to execute the authentication transaction.
     */
    protected final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    /**
     * The Authentication handler resolver.
     */
    protected final AuthenticationHandlerResolver authenticationHandlerResolver;

    /**
     * Indicate if principal resolution should totally fail
     * and no fall back onto principal that is produced by the
     * authentication handler.
     */
    protected boolean principalResolutionFailureFatal;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * Creates a new authentication manager with a map of authentication handlers to the principal resolvers that
     * should be used upon successful authentication if no principal is resolved by the authentication handler. If
     * the order of evaluation of authentication handlers is important, a map that preserves insertion order
     * (e.g. {@link LinkedHashMap}) should be used.
     *
     * @param authenticationEventExecutionPlan Describe the execution plan for this manager
     * @param authenticationHandlerResolver    the authentication handler resolver
     * @param principalResolutionFatal         the principal resolution fatal
     */
    protected AbstractAuthenticationManager(final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
                                            final AuthenticationHandlerResolver authenticationHandlerResolver,
                                            final boolean principalResolutionFatal) {
        Assert.notNull(authenticationEventExecutionPlan);
        Assert.notNull(authenticationHandlerResolver);
        Assert.notNull(principalResolutionFatal);

        this.authenticationEventExecutionPlan = authenticationEventExecutionPlan;
        this.authenticationHandlerResolver = authenticationHandlerResolver;
        this.principalResolutionFailureFatal = principalResolutionFatal;
    }

    /**
     * Populate authentication metadata attributes.
     *
     * @param builder     the builder
     * @param transaction the transaction
     */
    protected void populateAuthenticationMetadataAttributes(final AuthenticationBuilder builder,
                                                            final AuthenticationTransaction transaction) {
        LOGGER.debug("Invoking authentication metadata populators for authentication transaction");
        final Collection<AuthenticationMetaDataPopulator> pops = getAuthenticationMetadataPopulatorsForTransaction(transaction);
        pops.forEach(populator -> transaction.getCredentials().stream().filter(populator::supports)
                .forEach(credential -> populator.populateAttributes(builder, transaction)));
    }

    /**
     * Add authentication method attribute.
     *
     * @param builder        the builder
     * @param authentication the authentication
     */
    protected void addAuthenticationMethodAttribute(final AuthenticationBuilder builder,
                                                    final Authentication authentication) {
        authentication.getSuccesses().values().forEach(result -> builder.addAttribute(AUTHENTICATION_METHOD_ATTRIBUTE, result.getHandlerName()));
    }

    /**
     * Resolve principal.
     *
     * @param handler    the handler name
     * @param resolver   the resolver
     * @param credential the credential
     * @param principal  the current authenticated principal from a handler, if any.
     * @return the principal
     */
    protected Principal resolvePrincipal(final AuthenticationHandler handler, final PrincipalResolver resolver,
                                         final Credential credential, final Principal principal) {
        if (resolver.supports(credential)) {
            try {
                final Principal p = resolver.resolve(credential, principal, handler);
                LOGGER.debug("[{}] resolved [{}] from [{}]", resolver, p, credential);
                return p;
            } catch (final Exception e) {
                LOGGER.error("[{}] failed to resolve principal from [{}]", resolver, credential, e);
            }
        } else {
            LOGGER.warn(
                    "[{}] is configured to use [{}] but it does not support [{}], which suggests a configuration problem.",
                    handler.getName(), resolver, credential);
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
        AuthenticationCredentialsLocalBinder.bindCurrent(transaction.getCredentials());
        final AuthenticationBuilder builder = authenticateInternal(transaction);
        final Authentication authentication = builder.build();
        final Principal principal = authentication.getPrincipal();
        if (principal instanceof NullPrincipal) {
            throw new UnresolvedPrincipalException(authentication);
        }
        addAuthenticationMethodAttribute(builder, authentication);
        LOGGER.info("Authenticated principal [{}] with attributes [{}] via credentials [{}].",
                principal.getId(), principal.getAttributes(), transaction.getCredentials());
        populateAuthenticationMetadataAttributes(builder, transaction);
        final Authentication a = builder.build();
        AuthenticationCredentialsLocalBinder.bindCurrent(a);
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
    protected void authenticateAndResolvePrincipal(final AuthenticationBuilder builder,
                                                   final Credential credential,
                                                   final PrincipalResolver resolver,
                                                   final AuthenticationHandler handler) throws GeneralSecurityException, PreventedException {

        Principal principal;

        publishEvent(new CasAuthenticationTransactionStartedEvent(this, credential));

        final HandlerResult result = handler.authenticate(credential);
        builder.addSuccess(handler.getName(), result);
        LOGGER.debug("Authentication handler [{}] successfully authenticated [{}]", handler.getName(), credential);

        publishEvent(new CasAuthenticationTransactionSuccessfulEvent(this, credential));
        principal = result.getPrincipal();

        if (resolver == null) {
            LOGGER.debug("No principal resolution is configured for [{}]. Falling back to handler principal [{}]",
                    handler.getName(),
                    principal);
        } else {
            principal = resolvePrincipal(handler, resolver, credential, principal);
            if (principal == null) {
                if (this.principalResolutionFailureFatal) {
                    LOGGER.warn("Principal resolution handled by [{}] produced a null principal for: [{}]"
                                    + "CAS is configured to treat principal resolution failures as fatal.",
                            resolver.getClass().getSimpleName(), credential);
                    throw new UnresolvedPrincipalException();
                }
                LOGGER.warn("Principal resolution handled by [{}] produced a null principal. "
                        + "This is likely due to misconfiguration or missing attributes; CAS will attempt to use the principal "
                        + "produced by the authentication handler, if any.", resolver.getClass().getSimpleName());
            }
        }
        if (principal != null) {
            builder.setPrincipal(principal);
        }
        LOGGER.debug("Final principal resolved for this authentication event is [{}]", principal);
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
    protected abstract AuthenticationBuilder authenticateInternal(AuthenticationTransaction transaction) throws AuthenticationException;

    /**
     * Gets authentication handlers for this transaction.
     *
     * @param transaction the transaction
     * @return the authentication handlers for this transaction
     */
    protected Set<AuthenticationHandler> getAuthenticationHandlersForThisTransaction(final AuthenticationTransaction transaction) {
        final Set<AuthenticationHandler> handlers = this.authenticationEventExecutionPlan.getAuthenticationHandlersForTransaction(transaction);
        return this.authenticationHandlerResolver.resolve(handlers, transaction);
    }

    /**
     * Gets principal resolver linked to the handler if any.
     *
     * @param handler     the handler
     * @param transaction the transaction
     * @return the principal resolver linked to handler if any, or null.
     */
    protected PrincipalResolver getPrincipalResolverLinkedToHandlerIfAny(final AuthenticationHandler handler, final AuthenticationTransaction transaction) {
        return this.authenticationEventExecutionPlan.getPrincipalResolverForAuthenticationTransaction(handler, transaction);
    }

    /**
     * Gets authentication metadata populators for transaction.
     *
     * @param transaction the transaction
     * @return the authentication metadata populators for transaction
     */
    protected Collection<AuthenticationMetaDataPopulator> getAuthenticationMetadataPopulatorsForTransaction(
            final AuthenticationTransaction transaction) {
        return this.authenticationEventExecutionPlan.getAuthenticationMetadataPopulators(transaction);
    }

    /**
     * Publish event.
     *
     * @param event the event
     */
    protected void publishEvent(final ApplicationEvent event) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(event);
        }
    }
}
