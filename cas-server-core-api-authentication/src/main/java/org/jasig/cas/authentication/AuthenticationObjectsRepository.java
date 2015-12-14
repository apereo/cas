package org.jasig.cas.authentication;

/**
 * This is {@link AuthenticationObjectsRepository}, that holds the authentication machinery objects.
 * This component is to be injected into others where access to authentication object is required, and
 * simply serves as a holder.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public interface AuthenticationObjectsRepository {

    /**
     * Gets authentication transaction manager.
     *
     * @return the authentication transaction manager
     */
    AuthenticationTransactionManager getAuthenticationTransactionManager();

    /**
     * Gets authentication transaction factory.
     *
     * @return the authentication transaction factory
     */
    AuthenticationTransactionFactory getAuthenticationTransactionFactory();

    /**
     * Gets principal election strategy.
     *
     * @return the principal election strategy
     */
    PrincipalElectionStrategy getPrincipalElectionStrategy();
}
