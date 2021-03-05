package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

import java.io.Serializable;
import java.util.Optional;
import java.util.Set;

/**
 * Strategy interface for pluggable authentication security policies.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@FunctionalInterface
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface AuthenticationPolicy extends Ordered, Serializable {
    /**
     * Determines whether an authentication event is satisfied by arbitrary security policy.
     *
     * @param authentication         Authentication event to examine for compliance with security policy.
     * @param authenticationHandlers the authentication handlers selected for this transaction.
     * @param applicationContext     the application context
     * @param assertion              the assertion
     * @return Authentication policy execution result
     * @throws Exception the exception
     */
    AuthenticationPolicyExecutionResult isSatisfiedBy(Authentication authentication,
                                                      Set<AuthenticationHandler> authenticationHandlers,
                                                      ConfigurableApplicationContext applicationContext,
                                                      Optional<Serializable> assertion) throws Exception;

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Define the name of this even resolver.
     *
     * @return name of the resolver.
     */
    default String getName() {
        return this.getClass().getSimpleName();
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
