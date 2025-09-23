package org.apereo.cas.authentication;

import org.apereo.cas.util.NamedObject;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Strategy interface for pluggable authentication security policies.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface AuthenticationPolicy extends Ordered, Serializable, NamedObject {

    /**
     * Always satisfied authentication policy.
     *
     * @return the authentication policy
     */
    static AuthenticationPolicy alwaysSatisfied() {
        return (authentication, handlers, applicationContext, assertion) -> AuthenticationPolicyExecutionResult.success();
    }

    /**
     * Never satisfied authentication policy.
     *
     * @return the authentication policy
     */
    static AuthenticationPolicy neverSatisfied() {
        return (authentication, handlers, applicationContext, assertion) -> AuthenticationPolicyExecutionResult.failure();
    }

    /**
     * Determines whether an authentication event is satisfied by arbitrary security policy.
     *
     * @param authentication         Authentication event to examine for compliance with security policy.
     * @param authenticationHandlers the authentication handlers selected for this transaction.
     * @param applicationContext     the application context
     * @param context                the context
     * @return Authentication policy execution result
     * @throws Throwable the throwable
     */
    AuthenticationPolicyExecutionResult isSatisfiedBy(Authentication authentication,
                                                      Set<AuthenticationHandler> authenticationHandlers,
                                                      ConfigurableApplicationContext applicationContext,
                                                      Map<String, ? extends Serializable> context) throws Throwable;

    /**
     * Is satisfied by authentication policy.
     *
     * @param authentication         the authentication
     * @param authenticationHandlers the authentication handlers
     * @param applicationContext     the application context
     * @return the authentication policy execution result
     * @throws Throwable the throwable
     */
    default AuthenticationPolicyExecutionResult isSatisfiedBy(
        final Authentication authentication,
        final Set<AuthenticationHandler> authenticationHandlers,
        final ConfigurableApplicationContext applicationContext) throws Throwable {
        return isSatisfiedBy(authentication, authenticationHandlers, applicationContext, Map.of());
    }

    /**
     * Is satisfied by authentication policy.
     *
     * @param authentication     the authentication
     * @param applicationContext the application context
     * @param context            the context
     * @return the authentication policy execution result
     * @throws Throwable the throwable
     */
    default AuthenticationPolicyExecutionResult isSatisfiedBy(
        final Authentication authentication,
        final ConfigurableApplicationContext applicationContext,
        final Map<String, ? extends Serializable> context) throws Throwable {
        return isSatisfiedBy(authentication, Set.of(), applicationContext, context);
    }

    /**
     * Is satisfied by authentication policy.
     *
     * @param authentication     the authentication
     * @param applicationContext the application context
     * @return the authentication policy execution result
     * @throws Throwable the throwable
     */
    default AuthenticationPolicyExecutionResult isSatisfiedBy(
        final Authentication authentication,
        final ConfigurableApplicationContext applicationContext) throws Throwable {
        return isSatisfiedBy(authentication, Set.of(), applicationContext, Map.of());
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Should authentication chain resume on failure?
     *
     * @param failure the failure
     * @return resume, or block
     */
    default boolean shouldResumeOnFailure(final Throwable failure) {
        return failure != null;
    }
}
