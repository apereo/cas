package org.apereo.cas.authentication;

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.exceptions.UnresolvedPrincipalException;
import org.apereo.cas.authentication.policy.AnyAuthenticationPolicy;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationPrincipalResolvedEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionStartedEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionSuccessfulEvent;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.OrderComparator;
import org.springframework.util.Assert;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link PolicyBasedAuthenticationManager}, which provides common operations
 * around an authentication manager implementation.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PolicyBasedAuthenticationManager implements AuthenticationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyBasedAuthenticationManager.class);

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

    /**
     * Authentication security policy.
     */
    protected Collection<AuthenticationPolicy> authenticationPolicies;

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
     * @param authenticationPolicies           the authentication policies
     */
    protected PolicyBasedAuthenticationManager(final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
                                               final AuthenticationHandlerResolver authenticationHandlerResolver,
                                               final Collection<AuthenticationPolicy> authenticationPolicies) {
        this.authenticationPolicies = authenticationPolicies;
        this.authenticationEventExecutionPlan = authenticationEventExecutionPlan;
        this.authenticationHandlerResolver = authenticationHandlerResolver;
    }

    /**
     * Instantiates a new Policy based authentication manager.
     *
     * @param authenticationEventExecutionPlan the execution plan
     * @param authenticationHandlerResolver    the authentication handler resolver
     * @param authenticationPolicies           the authentication policy
     * @param principalResolutionFatal         the principal resolution fatal
     */
    public PolicyBasedAuthenticationManager(final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
                                            final AuthenticationHandlerResolver authenticationHandlerResolver,
                                            final Collection<AuthenticationPolicy> authenticationPolicies,
                                            final boolean principalResolutionFatal) {
        this(authenticationEventExecutionPlan, authenticationHandlerResolver, authenticationPolicies);
        this.principalResolutionFailureFatal = principalResolutionFatal;
    }

    /**
     * Instantiates a new Policy based authentication manager.
     *
     * @param authenticationEventExecutionPlan the authentication event execution plan
     * @param servicesManager                  the services manager
     * @param authenticationPolicy             the authentication policy
     */
    public PolicyBasedAuthenticationManager(final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
                                            final ServicesManager servicesManager,
                                            final Collection<AuthenticationPolicy> authenticationPolicy) {
        this(authenticationEventExecutionPlan, new RegisteredServiceAuthenticationHandlerResolver(servicesManager), authenticationPolicy);
    }

    /**
     * Instantiates a new Policy based authentication manager.
     *
     * @param authenticationEventExecutionPlan the authentication event execution plan
     * @param servicesManager                  the services manager
     */
    public PolicyBasedAuthenticationManager(final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
                                            final ServicesManager servicesManager) {
        this(authenticationEventExecutionPlan, servicesManager, CollectionUtils.wrap(new AnyAuthenticationPolicy(false)));
    }

    public PolicyBasedAuthenticationManager(final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
                                            final ServicesManager servicesManager,
                                            final AuthenticationPolicy policy) {
        this(authenticationEventExecutionPlan, servicesManager, CollectionUtils.wrap(policy));
    }

    /**
     * Populate authentication metadata attributes.
     *
     * @param builder     the builder
     * @param transaction the transaction
     */
    protected void invokeAuthenticationPostProcessors(final AuthenticationBuilder builder,
                                                      final AuthenticationTransaction transaction) {
        LOGGER.debug("Invoking authentication post processors for authentication transaction");
        final Collection<AuthenticationPostProcessor> pops = authenticationEventExecutionPlan.getAuthenticationPostProcessors(transaction);

        final Collection<AuthenticationPostProcessor> supported = pops.stream().filter(processor -> transaction.getCredentials()
                .stream()
                .filter(processor::supports)
                .findFirst()
                .isPresent())
                .collect(Collectors.toList());
        for (final AuthenticationPostProcessor p : supported) {
            p.process(builder, transaction);
        }
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
        AuthenticationCredentialsLocalBinder.bindCurrent(builder);
        
        final Authentication authentication = builder.build();
        addAuthenticationMethodAttribute(builder, authentication);
        populateAuthenticationMetadataAttributes(builder, transaction);
        invokeAuthenticationPostProcessors(builder, transaction);

        final Authentication auth = builder.build();
        final Principal principal = auth.getPrincipal();
        if (principal instanceof NullPrincipal) {
            throw new UnresolvedPrincipalException(auth);
        }
        LOGGER.info("Authenticated principal [{}] with attributes [{}] via credentials [{}].",
                principal.getId(), principal.getAttributes(), transaction.getCredentials());
        AuthenticationCredentialsLocalBinder.bindCurrent(auth);
        
        return auth;
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

    /**
     * Authenticate internal authentication builder.
     *
     * @param transaction the transaction
     * @return the authentication builder
     * @throws AuthenticationException the authentication exception
     */
    protected AuthenticationBuilder authenticateInternal(final AuthenticationTransaction transaction) throws AuthenticationException {
        final Collection<Credential> credentials = transaction.getCredentials();
        final AuthenticationBuilder builder = new DefaultAuthenticationBuilder(NullPrincipal.getInstance());
        credentials.stream().forEach(cred -> builder.addCredential(new BasicCredentialMetaData(cred)));

        final Set<AuthenticationHandler> handlerSet = getAuthenticationHandlersForThisTransaction(transaction);
        Assert.notNull(handlerSet, "Resolved authentication handlers for this transaction cannot be null");
        if (handlerSet.isEmpty()) {
            LOGGER.warn("Resolved authentication handlers for this transaction are empty");
        }

        final boolean success = credentials
                .stream()
                .anyMatch(credential -> {
                    final boolean isSatisfied = handlerSet
                            .stream()
                            .filter(handler -> handler.supports(credential))
                            .anyMatch(handler -> {
                                try {
                                    final PrincipalResolver resolver = getPrincipalResolverLinkedToHandlerIfAny(handler, transaction);
                                    authenticateAndResolvePrincipal(builder, credential, resolver, handler);
                                    final Pair<Boolean, Set<Throwable>> failures = evaluateAuthenticationPolicies(builder.build());
                                    return failures.getKey();
                                } catch (final Exception e) {
                                    handleAuthenticationException(e, handler.getName(), builder);
                                }
                                return false;
                            });

                    if (!isSatisfied) {
                        LOGGER.error("Authentication has failed. Credentials may be incorrect or CAS cannot "
                                + "find authentication handler that supports [{}] of type [{}]. Examine the configuration to "
                                + "ensure a method of authentication is defined and analyze CAS logs at DEBUG level to trace "
                                + "the authentication event.", credential, credential.getClass().getSimpleName());
                    }
                    return isSatisfied;
                });

        if (!success) {
            evaluateFinalAuthentication(builder, transaction);
        }

        return builder;
    }

    /**
     * Evaluate produced authentication context.
     * We apply an implicit security policy of at least one successful authentication.
     * Then, we apply the configured security policy.
     *
     * @param builder     the builder
     * @param transaction the transaction
     * @throws AuthenticationException the authentication exception
     */
    protected void evaluateFinalAuthentication(final AuthenticationBuilder builder,
                                               final AuthenticationTransaction transaction) throws AuthenticationException {
        if (builder.getSuccesses().isEmpty()) {
            publishEvent(new CasAuthenticationTransactionFailureEvent(this, builder.getFailures(),
                    transaction.getCredentials()));
            throw new AuthenticationException(builder.getFailures(), builder.getSuccesses());
        }

        final Authentication authentication = builder.build();
        final Pair<Boolean, Set<Throwable>> failures = evaluateAuthenticationPolicies(authentication);
        if (!failures.getKey()) {
            publishEvent(new CasAuthenticationPolicyFailureEvent(this, builder.getFailures(), transaction, authentication));
            failures.getValue().forEach(e -> handleAuthenticationException(e, e.getClass().getSimpleName(), builder));
            throw new AuthenticationException(builder.getFailures(), builder.getSuccesses());
        }
    }

    /**
     * Evaluate authentication policies.
     *
     * @param authentication the authentication
     * @return true /false
     */
    protected Pair<Boolean, Set<Throwable>> evaluateAuthenticationPolicies(final Authentication authentication) {
        final Set<Throwable> failures = new LinkedHashSet<>();
        final List<AuthenticationPolicy> policies = new ArrayList<>(this.authenticationPolicies);
        OrderComparator.sort(policies);
        policies
                .stream()
                .forEach(p -> {
                    try {
                        final String simpleName = p.getClass().getSimpleName();
                        LOGGER.debug("Executing authentication policy [{}]", simpleName);
                        if (!p.isSatisfiedBy(authentication)) {
                            failures.add(new AuthenticationException("Unable to satisfy authentication policy " + simpleName));
                        }
                    } catch (final GeneralSecurityException e) {
                        LOGGER.debug(e.getMessage(), e);
                        failures.add(e.getCause());
                    } catch (final Exception e) {
                        LOGGER.debug(e.getMessage(), e);
                        failures.add(e);
                    }
                });

        return Pair.of(failures.isEmpty(), failures);
    }

    /**
     * Handle authentication exception.
     *
     * @param e       the exception
     * @param name    the name
     * @param builder the builder
     */
    protected void handleAuthenticationException(final Throwable e, final String name,
                                                 final AuthenticationBuilder builder) {
        String msg = e.getMessage();
        if (e.getCause() != null) {
            msg += " / " + e.getCause().getMessage();
        }
        if (e instanceof GeneralSecurityException) {
            LOGGER.debug("[{}] exception details: [{}].", name, msg);
            builder.addFailure(name, e.getClass());
        } else {
            LOGGER.error("[{}]: [{}]", name, msg);
            builder.addFailure(name, e.getClass());
        }
    }
}
