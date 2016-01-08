package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Service;

/**
 * This is {@link AuthenticationSystemSupport} - a facade that exposes a high level authentication system API to CAS core.
 *
 * This component is to be injected into others where authentication subsystem interaction needs to happen -like performing single
 * authentication transaction, performing a finalized authentication attempt, or finalizing an authentication attempt consisting of
 * multiple authentication transaction steps.
 *
 * This facade also exposes lower level components that implementations use to perform necessary authentication steps, so that clients of
 * this API have the ability to use those components directly if they choose so.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
public interface AuthenticationSystemSupport {

    /**
     * Gets authentication transaction manager.
     *
     * @return the authentication transaction manager
     */
    AuthenticationTransactionManager getAuthenticationTransactionManager();

    /**
     * Gets principal election strategy.
     *
     * @return the principal election strategy
     */
    PrincipalElectionStrategy getPrincipalElectionStrategy();

    /**
     * Initiate potential multi-transaction authentication attempt by handling the initial authentication transaction.
     *
     * @param credential a credential for the initial authentication transaction.
     *
     * @return authentication context builder used to accumulate authentication transactions in this authentication attempt.
     *
     * @throws AuthenticationException exception to indicate authentication processing failure.
     * @since 4.3.0
     */
    AuthenticationContextBuilder handleInitialAuthenticationTransaction(Credential... credential) throws AuthenticationException;

    /**
     * Handle single authentication transaction within potential multi-transaction authentication attempt.
     *
     * @param authenticationContextBuilder builder used to accumulate authentication transactions in this authentication attempt.
     * @param credential a credential used for this authentication transaction.
     *
     * @throws AuthenticationException exception to indicate authentication processing failure.
     * @since 4.3.0
     */
    void handleAuthenticationTransaction(AuthenticationContextBuilder authenticationContextBuilder, Credential... credential)
            throws AuthenticationException;

    /**
     * Finalize a potential multi-transaction authentication attempt.
     *
     * @param authenticationContextBuilder builder used to accumulate authentication transactions in this authentication attempt.
     * @param service a service for which this authentication attempt is performed.
     *
     * @return authentication context representing a final outcome of the authentication attempt.
     *
     * @since 4.3.0
     */
    AuthenticationContext finalizeAuthenticationAttempt(AuthenticationContextBuilder authenticationContextBuilder, Service service);

    /**
     * Handle a single-transaction authentication attempt.
     *
     * @param credential a credential used for this single-transaction authentication attempt.
     * @param service a service for which this single-transaction authentication attempt is performed.
     *
     * @return authentication context representing a final outcome of the authentication attempt.
     *
     * @throws AuthenticationException exception to indicate authentication processing failure.
     * @since 4.3.0
     */
    AuthenticationContext handleFinalizedAuthenticationAttempt(Service service, Credential... credential) throws AuthenticationException;
}
