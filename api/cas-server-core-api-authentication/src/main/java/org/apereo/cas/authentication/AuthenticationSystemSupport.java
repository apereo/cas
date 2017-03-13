package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

/**
 * This is {@link AuthenticationSystemSupport} - a facade that exposes a high level authentication system API to CAS core.
 * <p>
 * This component is to be injected into others where authentication subsystem interaction needs to happen - like performing single
 * authentication transaction, performing a finalized authentication transaction, or finalizing an all authentication transactions
 * that might have been processed and collected.
 * <p>
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
     * Initiate potential multi-transaction authentication event by handling the initial authentication transaction.
     *
     * @param authentication a pre-established authentication object in a multi-legged authentication flow.
     * @param credential     a credential for the authentication transaction.
     * @return authentication result builder used to accumulate authentication transactions in this authentication event.
     * @since 5.0.0
     */
    AuthenticationResultBuilder establishAuthenticationContextFromInitial(Authentication authentication, Credential credential);

    /**
     * Initiate potential multi-transaction authentication event by handling the initial authentication transaction.
     *
     * @param service    the service
     * @param credential a credential for the initial authentication transaction.
     * @return authentication result builder used to accumulate authentication transactions in this authentication event.
     * @throws AuthenticationException exception to indicate authentication processing failure.
     * @since 5.0.0
     */
    AuthenticationResultBuilder handleInitialAuthenticationTransaction(Service service, Credential... credential) throws AuthenticationException;

    /**
     * Handle single authentication transaction within potential multi-transaction authentication event.
     *
     * @param service                     the service
     * @param authenticationResultBuilder builder used to accumulate authentication transactions in this authentication event.
     * @param credential                  a credential used for this authentication transaction.
     * @return authentication result builder used to accumulate authentication transactions in this authentication event.
     * @throws AuthenticationException exception to indicate authentication processing failure.
     * @since 5.0.0
     */
    AuthenticationResultBuilder handleAuthenticationTransaction(Service service,
                                                                AuthenticationResultBuilder authenticationResultBuilder,
                                                                Credential... credential) throws AuthenticationException;

    /**
     * Finalize all authentication transactions processed and collected for this authentication event.
     *
     * @param authenticationResultBuilder builder used to accumulate authentication transactions in this authentication event.
     * @param service                     a service for this authentication event.
     * @return authentication result representing a final outcome of the authentication event.
     * @since 5.0.0
     */
    AuthenticationResult finalizeAllAuthenticationTransactions(AuthenticationResultBuilder authenticationResultBuilder, Service service);

    /**
     * Handle a single-transaction authentication event and immediately produce a finalized {@link AuthenticationResult}.
     *
     * @param service    a service for this authentication event.
     * @param credential a credential used for this single-transaction authentication event.
     * @return authentication result representing a final outcome of the authentication event.
     * @throws AuthenticationException exception to indicate authentication processing failure.
     * @since 5.0.0
     */
    AuthenticationResult handleAndFinalizeSingleAuthenticationTransaction(Service service, Credential... credential)
            throws AuthenticationException;
}
