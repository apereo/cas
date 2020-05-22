package org.apereo.cas.authentication;

import org.springframework.core.Ordered;

/**
 * This is {@link AuthenticationPreProcessor}. Authentication pre processors
 * run as the very first step in CAS authentication where credentials are about
 * to be authenticated.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface AuthenticationPreProcessor extends Ordered {

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Process the authentication event.
     *
     * @param transaction The authentication transaction.
     * @return true/false
     * @throws AuthenticationException the authn security exception
     */
    boolean process(AuthenticationTransaction transaction) throws AuthenticationException;

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
