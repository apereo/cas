package org.apereo.cas.authentication;

import org.apereo.cas.authentication.policy.AnyAuthenticationPolicy;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.events.authentication.CasAuthenticationPolicyFailureEvent;
import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionFailureEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.OrderComparator;
import org.springframework.util.Assert;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Provides an authentication manager that is inherently aware of multiple credentials and supports pluggable
 * security policy via the {@link AuthenticationPolicy} component. The authentication process is as follows:
 * <ul>
 * <li>For each given credential do the following:
 * <ul>
 * <li>Iterate over all configured authentication handlers.</li>
 * <li>Attempt to authenticate a credential if a handler supports it.</li>
 * <li>On success attempt to resolve a principal by doing the following:
 * <ul>
 * <li>Check whether a resolver is configured for the handler that authenticated the credential.</li>
 * <li>If a suitable resolver is found, attempt to resolve the principal.</li>
 * <li>If a suitable resolver is not found, use the principal resolved by the authentication handler.</li>
 * </ul>
 * </li>
 * <li>Check whether the security policy (e.g. any, all) is satisfied.
 * <ul>
 * <li>If security policy is met return immediately.</li>
 * <li>Continue if security policy is not met.</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * <li>
 * After all credentials have been attempted check security policy again.
 * Note there is an implicit security policy that requires at least one credential to be authenticated.
 * Then the security policy given by the {@link AuthenticationPolicy} is applied.
 * In all cases {@link AuthenticationException} is raised if security policy is not met.
 * </li>
 * </ul>
 * It is an error condition to fail to resolve a principal.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class PolicyBasedAuthenticationManager extends AbstractAuthenticationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyBasedAuthenticationManager.class);

    /**
     * Authentication security policy.
     */
    protected final Collection<AuthenticationPolicy> authenticationPolicies;

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
        super(authenticationEventExecutionPlan, authenticationHandlerResolver, principalResolutionFatal);
        this.authenticationPolicies = authenticationPolicies;
    }

    /**
     * Instantiates a new Policy based authentication manager.
     *
     * @param authenticationEventExecutionPlan the authentication event execution plan
     * @param servicesManager                  the services manager
     */
    public PolicyBasedAuthenticationManager(final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
                                            final ServicesManager servicesManager) {
        this(authenticationEventExecutionPlan, servicesManager, Arrays.asList(new AnyAuthenticationPolicy(false)));
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
        super(authenticationEventExecutionPlan, new RegisteredServiceAuthenticationHandlerResolver(servicesManager), false);
        this.authenticationPolicies = authenticationPolicy;
    }

    public PolicyBasedAuthenticationManager(final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
                                            final ServicesManager servicesManager,
                                            final AuthenticationPolicy authenticationPolicy) {
        super(authenticationEventExecutionPlan, new RegisteredServiceAuthenticationHandlerResolver(servicesManager), false);
        this.authenticationPolicies = Arrays.asList(authenticationPolicy);
    }

    @Override
    protected AuthenticationBuilder authenticateInternal(final AuthenticationTransaction transaction) throws AuthenticationException {
        final Collection<Credential> credentials = transaction.getCredentials();
        final AuthenticationBuilder builder = new DefaultAuthenticationBuilder(NullPrincipal.getInstance());
        credentials.stream().forEach(cred -> builder.addCredential(new BasicCredentialMetaData(cred)));

        final Set<AuthenticationHandler> handlerSet = getAuthenticationHandlersForThisTransaction(transaction);
        Assert.notNull(handlerSet, "Resolved authentication handlers for this transaction cannot be null");
        if (handlerSet.isEmpty()) {
            LOGGER.warn("Resolved authentication handlers for this transaction are empty");
        }

        final boolean success = credentials.stream().anyMatch(credential -> {
            final boolean isSatisfied = handlerSet.stream().filter(handler -> handler.supports(credential))
                    .anyMatch(handler -> {
                        try {
                            final PrincipalResolver resolver = getPrincipalResolverLinkedToHandlerIfAny(handler, transaction);
                            authenticateAndResolvePrincipal(builder, credential, resolver, handler);
                            return this.authenticationPolicies.stream().allMatch(p -> p.isSatisfiedBy(builder.build()));
                        } catch (final GeneralSecurityException e) {
                            LOGGER.info("[{}] failed authenticating [{}]", handler.getName(), credential);
                            LOGGER.debug("[{}] exception details: [{}]", handler.getName(), e.getMessage());
                            builder.addFailure(handler.getName(), e.getClass());
                        } catch (final PreventedException e) {
                            LOGGER.error("[{}]: [{}]  (Details: [{}])", handler.getName(), e.getMessage(), e.getCause().getMessage());
                            builder.addFailure(handler.getName(), e.getClass());
                        }
                        return false;
                    });

            if (isSatisfied) {
                return true;
            }

            LOGGER.warn("Authentication has failed. Credentials may be incorrect or CAS cannot find authentication handler that "
                            + "supports [{}] of type [{}], which suggests a configuration problem.",
                    credential, credential.getClass().getSimpleName());
            return false;
        });

        if (!success) {
            evaluateProducedAuthenticationContext(builder, transaction);
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
    protected void evaluateProducedAuthenticationContext(final AuthenticationBuilder builder,
                                                         final AuthenticationTransaction transaction) throws AuthenticationException {
        if (builder.getSuccesses().isEmpty()) {
            publishEvent(new CasAuthenticationTransactionFailureEvent(this, builder.getFailures(), transaction.getCredentials()));
            throw new AuthenticationException(builder.getFailures(), builder.getSuccesses());
        }

        final Authentication authentication = builder.build();
        final List<AuthenticationPolicy> policies = new ArrayList<>(this.authenticationPolicies);
        OrderComparator.sort(policies);
        final boolean result = policies.stream().allMatch(p -> {
            LOGGER.debug("Executing authentication policy [{}]", p.getClass().getSimpleName());
            return p.isSatisfiedBy(authentication);
        });

        if (!result) {
            publishEvent(new CasAuthenticationPolicyFailureEvent(this, builder.getFailures(), transaction, authentication));
            throw new AuthenticationException(builder.getFailures(), builder.getSuccesses());
        }
    }
}
