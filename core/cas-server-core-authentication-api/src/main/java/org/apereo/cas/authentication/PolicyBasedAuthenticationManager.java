package org.apereo.cas.authentication;

import com.codahale.metrics.annotation.Counted;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.exceptions.UnresolvedPrincipalException;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationPrincipalResolvedEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionStartedEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionSuccessfulEvent;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.UndeclaredThrowableException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link PolicyBasedAuthenticationManager}, which provides common operations
 * around an authentication manager implementation.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class PolicyBasedAuthenticationManager implements AuthenticationManager {

    private final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    private final boolean principalResolutionFailureFatal;

    private final ApplicationEventPublisher eventPublisher;

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
        pops.forEach(populator -> transaction.getCredentials()
            .stream()
            .filter(populator::supports)
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
                final Principal p = resolver.resolve(credential, Optional.ofNullable(principal), Optional.ofNullable(handler));
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
        final boolean result = invokeAuthenticationPreProcessors(transaction);
        if (!result) {
            LOGGER.warn("An authentication pre-processor could not successfully process the authentication transaction");
            throw new AuthenticationException("Authentication pre-processor has failed to process transaction");
        }
        AuthenticationCredentialsThreadLocalBinder.bindCurrent(transaction.getCredentials());
        final AuthenticationBuilder builder = authenticateInternal(transaction);
        AuthenticationCredentialsThreadLocalBinder.bindCurrent(builder);

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
        AuthenticationCredentialsThreadLocalBinder.bindCurrent(auth);

        return auth;
    }

    /**
     * Invoke authentication pre processors.
     *
     * @param transaction the transaction
     * @return the boolean
     */
    protected boolean invokeAuthenticationPreProcessors(final AuthenticationTransaction transaction) {
        LOGGER.debug("Invoking authentication pre processors for authentication transaction");
        final Collection<AuthenticationPreProcessor> pops = authenticationEventExecutionPlan.getAuthenticationPreProcessors(transaction);

        final Collection<AuthenticationPreProcessor> supported = pops.stream()
            .filter(processor -> transaction.getCredentials()
            .stream()
            .filter(processor::supports)
            .findFirst()
            .isPresent())
            .collect(Collectors.toList());

        boolean processed = true;
        final Iterator<AuthenticationPreProcessor> it = supported.iterator();
        while (processed && it.hasNext()) {
            final AuthenticationPreProcessor processor = it.next();
            processed = processor.process(transaction);
        }
        return processed;
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

        publishEvent(new CasAuthenticationTransactionStartedEvent(this, credential));

        final AuthenticationHandlerExecutionResult result = handler.authenticate(credential);
        final String authenticationHandlerName = handler.getName();
        builder.addSuccess(authenticationHandlerName, result);
        LOGGER.debug("Authentication handler [{}] successfully authenticated [{}]", authenticationHandlerName, credential);

        publishEvent(new CasAuthenticationTransactionSuccessfulEvent(this, credential));
        Principal principal = result.getPrincipal();

        final String resolverName = resolver != null ? resolver.getClass().getSimpleName() : "N/A";
        if (resolver == null) {
            LOGGER.debug("No principal resolution is configured for [{}]. Falling back to handler principal [{}]", authenticationHandlerName, principal);
        } else {
            principal = resolvePrincipal(handler, resolver, credential, principal);
            if (principal == null) {
                if (this.principalResolutionFailureFatal) {
                    LOGGER.warn("Principal resolution handled by [{}] produced a null principal for: [{}]"
                        + "CAS is configured to treat principal resolution failures as fatal.", resolverName, credential);
                    throw new UnresolvedPrincipalException();
                }
                LOGGER.warn("Principal resolution handled by [{}] produced a null principal. "
                    + "This is likely due to misconfiguration or missing attributes; CAS will attempt to use the principal "
                    + "produced by the authentication handler, if any.", resolver.getClass().getSimpleName());
            }
        }

        if (principal == null) {
            LOGGER.warn("Principal resolution for authentication by [{}] produced a null principal.", authenticationHandlerName);
        } else {
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
    @SneakyThrows
    protected Set<AuthenticationHandler> getAuthenticationHandlersForThisTransaction(final AuthenticationTransaction transaction) {
        final Set<AuthenticationHandler> handlers = authenticationEventExecutionPlan.getAuthenticationHandlersForTransaction(transaction);
        LOGGER.debug("Candidate/Registered authentication handlers for this transaction are [{}]", handlers);
        final Collection<AuthenticationHandlerResolver> handlerResolvers = authenticationEventExecutionPlan.getAuthenticationHandlerResolvers(transaction);
        LOGGER.debug("Authentication handler resolvers for this transaction are [{}]", handlerResolvers);

        final Set<AuthenticationHandler> resolvedHandlers = handlerResolvers.stream()
            .filter(r -> r.supports(handlers, transaction))
            .map(r -> r.resolve(handlers, transaction))
            .flatMap(Set::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (resolvedHandlers.isEmpty()) {
            LOGGER.debug("Authentication handler resolvers produced no candidate authentication handler. Using the default handler resolver instead...");
            final DefaultAuthenticationHandlerResolver defaultHandlerResolver = new DefaultAuthenticationHandlerResolver();
            if (defaultHandlerResolver.supports(handlers, transaction)) {
                resolvedHandlers.addAll(defaultHandlerResolver.resolve(handlers, transaction));
            }
        }

        if (resolvedHandlers.isEmpty()) {
            throw new GeneralSecurityException("No authentication handlers could be resolved to support the authentication transaction");
        }
        LOGGER.debug("Resolved and finalized authentication handlers to carry out this authentication transaction are [{}]", resolvedHandlers);
        return resolvedHandlers;
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
        LOGGER.debug("Authentication credentials provided for this transaction are [{}]", credentials);

        if (credentials.isEmpty()) {
            LOGGER.error("Resolved authentication handlers for this transaction are empty");
            throw new AuthenticationException("Resolved credentials for this transaction are empty");
        }

        final AuthenticationBuilder builder = new DefaultAuthenticationBuilder(NullPrincipal.getInstance());
        credentials.forEach(cred -> builder.addCredential(new BasicCredentialMetaData(cred)));

        @NonNull
        final Set<AuthenticationHandler> handlerSet = getAuthenticationHandlersForThisTransaction(transaction);
        LOGGER.debug("Candidate resolved authentication handlers for this transaction are [{}]", handlerSet);

        if (handlerSet.isEmpty()) {
            LOGGER.error("Resolved authentication handlers for this transaction are empty");
            throw new AuthenticationException(builder.getFailures(), builder.getSuccesses());
        }

        try {
            final Iterator<Credential> it = credentials.iterator();
            AuthenticationCredentialsThreadLocalBinder.clearInProgressAuthentication();
            while (it.hasNext()) {
                final Credential credential = it.next();
                LOGGER.debug("Attempting to authenticate credential [{}]", credential);

                final Iterator<AuthenticationHandler> itHandlers = handlerSet.iterator();
                boolean proceedWithNextHandler = true;
                while (proceedWithNextHandler && itHandlers.hasNext()) {
                    final AuthenticationHandler handler = itHandlers.next();
                    if (handler.supports(credential)) {
                        try {
                            final PrincipalResolver resolver = getPrincipalResolverLinkedToHandlerIfAny(handler, transaction);
                            LOGGER.debug("Attempting authentication of [{}] using [{}]", credential.getId(), handler.getName());
                            authenticateAndResolvePrincipal(builder, credential, resolver, handler);
                            AuthenticationCredentialsThreadLocalBinder.bindInProgress(builder.build());

                            final Pair<Boolean, Set<Throwable>> failures = evaluateAuthenticationPolicies(builder.build(), transaction);
                            proceedWithNextHandler = !failures.getKey();
                        } catch (final Exception e) {
                            LOGGER.error("Authentication has failed. Credentials may be incorrect or CAS cannot "
                                + "find authentication handler that supports [{}] of type [{}]. Examine the configuration to "
                                + "ensure a method of authentication is defined and analyze CAS logs at DEBUG level to trace "
                                + "the authentication event.", credential, credential.getClass().getSimpleName());

                            handleAuthenticationException(e, handler.getName(), builder);
                            proceedWithNextHandler = true;
                        }
                    } else {
                        LOGGER.debug("Authentication handler [{}] does not support the credential type [{}]. Trying next...", handler.getName(), credential);
                    }
                }
            }
            evaluateFinalAuthentication(builder, transaction);
            return builder;
        } finally {
            AuthenticationCredentialsThreadLocalBinder.clearInProgressAuthentication();
        }
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
            publishEvent(new CasAuthenticationTransactionFailureEvent(this, builder.getFailures(), transaction.getCredentials()));
            throw new AuthenticationException(builder.getFailures(), builder.getSuccesses());
        }

        final Authentication authentication = builder.build();
        final Pair<Boolean, Set<Throwable>> failures = evaluateAuthenticationPolicies(authentication, transaction);
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
     * @param transaction    the transaction
     * @return true /false
     */
    protected Pair<Boolean, Set<Throwable>> evaluateAuthenticationPolicies(final Authentication authentication,
                                                                           final AuthenticationTransaction transaction) {
        final Set<Throwable> failures = new LinkedHashSet<>();
        final Collection<AuthenticationPolicy> policies = authenticationEventExecutionPlan.getAuthenticationPolicies(transaction);

        policies
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
     * @param ex      the exception
     * @param name    the name
     * @param builder the builder
     */
    protected void handleAuthenticationException(final Throwable ex, final String name, final AuthenticationBuilder builder) {
        Throwable e = ex;
        if (ex instanceof UndeclaredThrowableException) {
            e = ((UndeclaredThrowableException) ex).getUndeclaredThrowable();
        }
        final StringBuilder msg = new StringBuilder(StringUtils.defaultString(e.getMessage()));
        if (e.getCause() != null) {
            msg.append(" / ").append(e.getCause().getMessage());
        }
        if (e instanceof GeneralSecurityException) {
            LOGGER.debug("[{}] exception details: [{}].", name, msg);
            builder.addFailure(name, e);
        } else {
            LOGGER.error("[{}]: [{}]", name, msg);
            builder.addFailure(name, e);
        }
    }
}
