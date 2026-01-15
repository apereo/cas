package org.apereo.cas.authentication;

import module java.base;
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
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean process(AuthenticationTransaction transaction) throws Throwable;

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
}
