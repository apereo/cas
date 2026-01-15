package org.apereo.cas.authentication;

import module java.base;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.Ordered;

/**
 * This is {@link AuthenticationPostProcessor}. Authentication post processors
 * run as the very last step in CAS authentication where authentication event is internally
 * processed, validated, principal resolved and all attributes for principal and authentication
 * are collected.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface AuthenticationPostProcessor extends Ordered, DisposableBean {

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Process the authentication event.
     *
     * @param builder     Builder object that temporarily holds authentication metadata.
     * @param transaction The authentication transaction.
     * @throws Throwable the authn security exception
     */
    void process(AuthenticationBuilder builder, AuthenticationTransaction transaction) throws Throwable;

    /**
     * Determines whether the processor has the capability to perform tasks on the given credential.
     *
     * @param credential The credential to check.
     * @return True if processor supports the Credential, false otherwise.
     * @throws Throwable the throwable
     */
    default boolean supports(final Credential credential) throws Throwable {
        return true;
    }

    @Override
    default void destroy() {
    }

    /**
     * No authentication post processor.
     *
     * @return the authentication post processor
     */
    static AuthenticationPostProcessor none() {
        return (builder, transaction) -> {
        };
    }
}
