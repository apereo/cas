package org.apereo.cas.authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

import java.io.Serializable;
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
     * @return True if authentication isSatisfiedBy security policy, false otherwise.
     * @throws Exception the exception
     */
    boolean isSatisfiedBy(Authentication authentication, Set<AuthenticationHandler> authenticationHandlers,
                          ConfigurableApplicationContext applicationContext) throws Exception;

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
}
