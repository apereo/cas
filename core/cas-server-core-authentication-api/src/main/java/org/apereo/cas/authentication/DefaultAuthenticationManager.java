package org.apereo.cas.authentication;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.exceptions.UnresolvedPrincipalException;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationPrincipalResolvedEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionStartedEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionSuccessfulEvent;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jooq.lambda.Unchecked;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultAuthenticationManager}, which provides common operations
 * around an authentication manager implementation.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
@Monitorable
public class DefaultAuthenticationManager implements AuthenticationManager {

    private final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    private final ObjectProvider<AuthenticationSystemSupport> authenticationSystemSupport;

    private final boolean principalResolutionFailureFatal;

    private final ConfigurableApplicationContext applicationContext;

    @Override
    @Audit(
        action = AuditableActions.AUTHENTICATION,
        actionResolverName = AuditActionResolvers.AUTHENTICATION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.AUTHENTICATION_RESOURCE_RESOLVER)
    public Authentication authenticate(final AuthenticationTransaction transaction) throws Throwable {
        val result = invokeAuthenticationPreProcessors(transaction);
        if (!result) {
            LOGGER.warn("An authentication pre-processor could not successfully process the authentication transaction");
            throw new AuthenticationException("Authentication pre-processor has failed to process transaction");
        }
        val authenticationBuilder = authenticateInternal(transaction);
        val authentication = authenticationBuilder.build();
        addAuthenticationMethodAttribute(authenticationBuilder, authentication);
        populateAuthenticationMetadataAttributes(authenticationBuilder, transaction);
        invokeAuthenticationPostProcessors(authenticationBuilder, transaction);

        val auth = authenticationBuilder.build();
        val principal = auth.getPrincipal();
        if (principal instanceof NullPrincipal) {
            throw new UnresolvedPrincipalException(auth);
        }
        LOGGER.info("Authenticated principal [{}] with attributes [{}] via credentials [{}].",
            principal.getId(), principal.getAttributes(), transaction.getCredentials());
        return auth;
    }

    protected void invokeAuthenticationPostProcessors(final AuthenticationBuilder builder,
                                                      final AuthenticationTransaction transaction) {
        LOGGER.debug("Invoking authentication post processors for authentication transaction");
        val pops = authenticationEventExecutionPlan.getAuthenticationPostProcessors(transaction);
        pops.stream()
            .filter(processor -> transaction.getCredentials()
                .stream()
                .anyMatch(Unchecked.predicate(processor::supports)))
            .forEach(Unchecked.consumer(processor -> processor.process(builder, transaction)));
    }

    protected void populateAuthenticationMetadataAttributes(final AuthenticationBuilder builder,
                                                            final AuthenticationTransaction transaction) {
        LOGGER.debug("Invoking authentication metadata populators for authentication transaction");
        val pops = getAuthenticationMetadataPopulatorsForTransaction(transaction);
        pops.forEach(populator -> transaction.getCredentials()
            .stream()
            .filter(populator::supports)
            .forEach(credential -> populator.populateAttributes(builder, transaction)));
    }

    protected void addAuthenticationMethodAttribute(final AuthenticationBuilder builder,
                                                    final Authentication authentication) {
        authentication.getSuccesses().values()
            .forEach(result -> builder.addAttribute(AUTHENTICATION_METHOD_ATTRIBUTE, result.getHandlerName()));
    }

    protected Principal resolvePrincipal(final AuthenticationHandler handler, final PrincipalResolver resolver,
                                         final Credential credential, final Principal principal,
                                         final Service service) {
        if (resolver.supports(credential)) {
            try {
                val resolved = resolver.resolve(credential, Optional.ofNullable(principal),
                    Optional.ofNullable(handler), Optional.ofNullable(service));
                LOGGER.debug("[{}] resolved [{}] from [{}]", resolver, resolved, credential);
                return resolved;
            } catch (final Throwable e) {
                LOGGER.error("[{}] failed to resolve principal from [{}]", resolver, credential);
                LoggingUtils.error(LOGGER, e);
            }
        } else {
            LOGGER.warn("[{}] is configured to use [{}] but it does not support [{}], which suggests a configuration problem.",
                handler.getName(), resolver, credential);
        }
        return null;
    }

    protected boolean invokeAuthenticationPreProcessors(final AuthenticationTransaction transaction) throws Throwable {
        LOGGER.trace("Invoking authentication pre processors for authentication transaction");
        val pops = authenticationEventExecutionPlan.getAuthenticationPreProcessors(transaction);

        val supported = pops.stream()
            .filter(processor -> transaction.getCredentials()
                .stream()
                .anyMatch(Unchecked.predicate(processor::supports)))
            .toList();

        var processed = true;
        val it = supported.iterator();
        while (processed && it.hasNext()) {
            val processor = it.next();
            processed = processor.process(transaction);
        }
        return processed;
    }

    protected void authenticateAndResolvePrincipal(final AuthenticationBuilder authenticationBuilder,
                                                   final Credential credential,
                                                   final PrincipalResolver principalResolver,
                                                   final AuthenticationHandler handler,
                                                   final Service service) throws Throwable {
        val clientInfo = ClientInfoHolder.getClientInfo();
        publishEvent(new CasAuthenticationTransactionStartedEvent(this, credential, clientInfo));

        try {
            AuthenticationHolder.setCurrentAuthentication(authenticationBuilder.build());
            
            val handlerExecutionResult = handler.authenticate(credential, service);
            val authenticationHandlerName = handler.getName();
            authenticationBuilder.addSuccess(authenticationHandlerName, handlerExecutionResult);
            LOGGER.debug("Authentication handler [{}] successfully authenticated [{}]", authenticationHandlerName, credential);
            publishEvent(new CasAuthenticationTransactionSuccessfulEvent(this, credential, clientInfo));
            val principal = principalResolver != null
                ? resolvePrincipal(handler, principalResolver, credential, handlerExecutionResult.getPrincipal(), service)
                : handlerExecutionResult.getPrincipal();
            if (principal == null) {
                val resolverName = principalResolver == null ? authenticationHandlerName : principalResolver.getName();
                if (this.principalResolutionFailureFatal) {
                    LOGGER.warn("Principal resolution handled by [{}] produced a null principal for: [{}]"
                        + "CAS is configured to treat principal resolution failures as fatal.", resolverName, credential);
                    throw new UnresolvedPrincipalException();
                }
                LOGGER.warn("Principal resolution handled by [{}] produced a null principal. "
                    + "This is likely due to misconfiguration or missing attributes; CAS will attempt to use the principal "
                    + "produced by the authentication handler, if any.", resolverName);
            } else {
                authenticationBuilder.setPrincipal(principal);
            }
            LOGGER.debug("Final principal resolved for this authentication event is [{}]", principal);
            publishEvent(new CasAuthenticationPrincipalResolvedEvent(this, principal, clientInfo));
        } finally {
            AuthenticationHolder.clear();
        }
    }

    protected PrincipalResolver getPrincipalResolverLinkedToHandlerIfAny(final AuthenticationHandler handler,
                                                                         final AuthenticationTransaction transaction) {
        return this.authenticationEventExecutionPlan.getPrincipalResolver(handler, transaction);
    }

    protected Collection<AuthenticationMetaDataPopulator> getAuthenticationMetadataPopulatorsForTransaction(
        final AuthenticationTransaction transaction) {
        return authenticationEventExecutionPlan.getAuthenticationMetadataPopulators(transaction);
    }

    protected void publishEvent(final ApplicationEvent event) {
        if (applicationContext != null) {
            applicationContext.publishEvent(event);
        }
    }

    protected AuthenticationBuilder authenticateInternal(final AuthenticationTransaction transaction) throws Throwable {
        val credentials = transaction.getCredentials();
        LOGGER.debug("Authentication credentials provided for this transaction are [{}]", credentials);

        if (credentials.isEmpty()) {
            LOGGER.error("Resolved authentication handlers for this transaction are empty");
            throw new AuthenticationException("Resolved credentials for this transaction are empty");
        }

        val authenticationBuilder = new DefaultAuthenticationBuilder(NullPrincipal.getInstance());
        credentials.forEach(authenticationBuilder::addCredential);

        val handlerSet = authenticationEventExecutionPlan.resolveAuthenticationHandlers(transaction);
        LOGGER.debug("Candidate resolved authentication handlers for this transaction are [{}]", handlerSet);

        try {
            for (val credential : credentials) {
                LOGGER.debug("Attempting to authenticate credential [{}]", credential);

                val itHandlers = handlerSet.iterator();
                var proceedWithNextHandler = true;
                while (proceedWithNextHandler && itHandlers.hasNext()) {
                    val handler = itHandlers.next();
                    if (handler.supports(credential)) {
                        try {
                            val resolver = getPrincipalResolverLinkedToHandlerIfAny(handler, transaction);
                            LOGGER.debug("Attempting authentication of [{}] using [{}]", credential.getId(), handler.getName());
                            authenticateAndResolvePrincipal(authenticationBuilder, credential, resolver, handler, transaction.getService());

                            val authnResult = authenticationBuilder.build();
                            val executionResult = evaluateAuthenticationPolicies(authnResult, transaction, handlerSet);
                            proceedWithNextHandler = !executionResult.isSuccess();
                        } catch (final GeneralSecurityException e) {
                            handleAuthenticationException(e, handler.getName(), authenticationBuilder);
                            proceedWithNextHandler = shouldAuthenticationChainProceedOnFailure(transaction, e);
                        } catch (final Exception e) {
                            LOGGER.error("Authentication has failed. Credentials may be incorrect or CAS cannot "
                                + "find authentication handler that supports [{}] of type [{}]. Examine the configuration to "
                                + "ensure a method of authentication is defined and analyze CAS logs at DEBUG level to trace "
                                + "the authentication event.", credential, credential.getClass().getSimpleName());

                            handleAuthenticationException(e, handler.getName(), authenticationBuilder);
                            proceedWithNextHandler = shouldAuthenticationChainProceedOnFailure(transaction, e);
                        }
                    } else {
                        LOGGER.debug("Authentication handler [{}] does not support the credential type [{}].",
                            handler.getName(), credential);
                    }
                }
            }
            evaluateFinalAuthentication(authenticationBuilder, transaction, handlerSet);
            return authenticationBuilder;
        } finally {
            for (val handler : handlerSet) {
                if (handler.isDisposable() && handler instanceof final DisposableBean db) {
                    db.destroy();
                }
            }
        }
    }

    protected void evaluateFinalAuthentication(final AuthenticationBuilder builder,
                                               final AuthenticationTransaction transaction,
                                               final Set<AuthenticationHandler> authenticationHandlers) throws Throwable {
        val clientInfo = ClientInfoHolder.getClientInfo();
        if (builder.getSuccesses().isEmpty()) {
            if (builder.getFailures().isEmpty()) {
                LOGGER.warn("The resulting authentication attempt has not recorded any successes or failures. This typically means that no authentication handler "
                    + "could be found to support the authentication request or the credential types provided. The authentication handlers that were "
                    + "examined are: [{}]", authenticationHandlers.stream().map(AuthenticationHandler::getName).collect(Collectors.joining(", ")));
            }
            publishEvent(new CasAuthenticationTransactionFailureEvent(this, builder.getFailures(), transaction.getCredentials(), clientInfo));
            throw createAuthenticationException(builder);
        }

        val authentication = builder.build();
        val executionResult = evaluateAuthenticationPolicies(authentication, transaction, authenticationHandlers);
        if (!executionResult.isSuccess()) {
            publishEvent(new CasAuthenticationPolicyFailureEvent(this, builder.getFailures(), transaction, authentication, clientInfo));
            executionResult.getFailures().forEach(e -> handleAuthenticationException(e, e.getClass().getSimpleName(), builder));
            throw createAuthenticationException(builder);
        }
    }

    private static AuthenticationException createAuthenticationException(
        final AuthenticationBuilder builder) {
        val exception = new AuthenticationException(builder.getFailures(), builder.getSuccesses());
        if (!builder.getFailures().isEmpty()) {
            val firstCause = builder.getFailures().values().iterator().next();
            exception.initCause(firstCause);
        }
        return exception;
    }

    protected ChainingAuthenticationPolicyExecutionResult evaluateAuthenticationPolicies(final Authentication authentication,
                                                                                         final AuthenticationTransaction transaction,
                                                                                         final Set<AuthenticationHandler> authenticationHandlers) throws Throwable {
        val policies = authenticationEventExecutionPlan.getAuthenticationPolicies(transaction);
        val executionResult = new ChainingAuthenticationPolicyExecutionResult();

        val resultBuilder = authenticationSystemSupport.getObject().getAuthenticationResultBuilderFactory().newBuilder();
        resultBuilder.collect(transaction.getAuthentications());
        resultBuilder.collect(authentication);

        val resultAuthentication = resultBuilder.build().getAuthentication();
        LOGGER.trace("Final authentication used for authentication policy evaluation is [{}]", resultAuthentication);

        policies.forEach(policy -> {
            try {
                LOGGER.debug("Executing authentication policy [{}]", policy.getName());
                val supportingHandlers = authenticationHandlers
                    .stream()
                    .filter(handler -> transaction.getCredentials().stream().anyMatch(handler::supports))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
                val result = policy.isSatisfiedBy(resultAuthentication, supportingHandlers, applicationContext);
                executionResult.getResults().add(result);
                if (!result.isSuccess()) {
                    executionResult.getFailures()
                        .add(new AuthenticationException("Unable to satisfy authentication policy %s".formatted(policy.getName())));
                }
            } catch (final GeneralSecurityException e) {
                LOGGER.debug(e.getMessage(), e);
                FunctionUtils.doIfNotNull(e.getCause(), o -> executionResult.getFailures().add(e.getCause()));
            } catch (final Throwable e) {
                LOGGER.debug(e.getMessage(), e);
                executionResult.getFailures().add(e);
            }
        });
        return executionResult;
    }

    protected void handleAuthenticationException(final Throwable ex, final String name, final AuthenticationBuilder builder) {
        LOGGER.trace(ex.getMessage(), ex);
        val msg = new StringBuilder(StringUtils.defaultString(ex.getMessage()));
        if (ex.getCause() != null) {
            msg.append(" / ").append(ex.getCause().getMessage());
        }
        if (ex instanceof GeneralSecurityException) {
            LOGGER.info("[{}] exception details: [{}].", name, msg);
            builder.addFailure(name, ex);
        } else {
            LOGGER.error("[{}]: [{}]", name, msg);
            builder.addFailure(name, ex);
        }
    }

    private boolean shouldAuthenticationChainProceedOnFailure(final AuthenticationTransaction transaction,
                                                              final Throwable failure) {
        val policies = authenticationEventExecutionPlan.getAuthenticationPolicies(transaction);
        return policies.stream().anyMatch(policy -> policy.shouldResumeOnFailure(failure));
    }

    @Getter
    private static final class ChainingAuthenticationPolicyExecutionResult {
        private final List<AuthenticationPolicyExecutionResult> results = new ArrayList<>();

        private final Set<Throwable> failures = new HashSet<>();

        /**
         * Indicate success, if no failures are present.
         *
         * @return true/false
         */
        public boolean isSuccess() {
            return failures.isEmpty();
        }
    }
}
