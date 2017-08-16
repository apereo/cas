package org.apereo.cas.authentication;

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
public interface AuthenticationPostProcessor extends Ordered {

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Process the authentication event.
     *
     * @param builder     Builder object that temporarily holds authentication metadata.
     * @param transaction The authentication transaction.
     * @throws AuthenticationException the authn security exception
     */
    void process(AuthenticationBuilder builder, AuthenticationTransaction transaction) throws AuthenticationException;

    /**
     * Determines whether the processor has the capability to perform tasks on the given credential.
     *
     * @param credential The credential to check.
     * @return True if processor supports the Credential, false otherwise.
     */
    default boolean supports(final Credential credential) {
        return true;
    }
}
