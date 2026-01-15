package org.apereo.cas.authentication;

import module java.base;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.multitenancy.TenantsManager;
import org.jspecify.annotations.Nullable;

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
     * Default impl bean name.
     */
    String BEAN_NAME = "defaultAuthenticationSystemSupport";

    /**
     * Gets principal factory.
     *
     * @return the principal factory
     */
    PrincipalFactory getPrincipalFactory();

    /**
     * Gets tenant manager.
     *
     * @return the tenant manager
     */
    TenantsManager getTenantsManager();

    /**
     * Gets tenant extractor.
     *
     * @return the tenant extractor
     */
    TenantExtractor getTenantExtractor();

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
     * Gets principal resolver.
     *
     * @return the principal resolver
     */
    PrincipalResolver getPrincipalResolver();

    /**
     * Gets authentication transaction factory.
     *
     * @return the authentication transaction factory
     */
    AuthenticationTransactionFactory getAuthenticationTransactionFactory();

    /**
     * Gets authentication result builder factory.
     *
     * @return the authentication result builder factory
     */
    AuthenticationResultBuilderFactory getAuthenticationResultBuilderFactory();

    /**
     * Initiate potential multi-transaction authentication event by handling the initial authentication transaction.
     *
     * @param authentication a pre-established authentication object in a multi-legged authentication flow.
     * @param credential     a credential for the authentication transaction.
     * @return authentication result builder used to accumulate authentication transactions in this authentication event.
     * @since 5.0.0
     */
    AuthenticationResultBuilder establishAuthenticationContextFromInitial(Authentication authentication, Credential... credential);

    /**
     * Establish authentication context from initial authentication result builder.
     *
     * @param authentication the authentication
     * @return the authentication result builder
     */
    AuthenticationResultBuilder establishAuthenticationContextFromInitial(Authentication authentication);

    /**
     * Initiate potential multi-transaction authentication event by handling the initial authentication transaction.
     *
     * @param service    the service
     * @param credential a credential for the initial authentication transaction.
     * @return authentication result builder used to accumulate authentication transactions in this authentication event.
     * @throws AuthenticationException exception to indicate authentication processing failure.
     * @since 5.0.0
     */
    AuthenticationResultBuilder handleInitialAuthenticationTransaction(@Nullable Service service, Credential... credential) throws Throwable;

    /**
     * Handle single authentication transaction within potential multi-transaction authentication event.
     *
     * @param service                     the service
     * @param authenticationResultBuilder builder used to accumulate authentication transactions in this authentication event.
     * @param credential                  a credential used for this authentication transaction.
     * @return authentication result builder used to accumulate authentication transactions in this authentication event.
     * @throws Throwable the throwable
     * @since 5.0.0
     */
    AuthenticationResultBuilder handleAuthenticationTransaction(@Nullable Service service,
                                                                AuthenticationResultBuilder authenticationResultBuilder,
                                                                Credential... credential) throws Throwable;

    /**
     * Finalize all authentication transactions processed and collected for this authentication event.
     *
     * @param authenticationResultBuilder builder used to accumulate authentication transactions in this authentication event.
     * @param service                     a service for this authentication event.
     * @return authentication result representing a final outcome of the authentication event.
     * @throws Throwable the throwable
     * @since 5.0.0
     */
    @Nullable AuthenticationResult finalizeAllAuthenticationTransactions(AuthenticationResultBuilder authenticationResultBuilder, @Nullable Service service) throws Throwable;

    /**
     * Handle a single-transaction authentication event and immediately produce a finalized {@link AuthenticationResult}.
     *
     * @param service    a service for this authentication event.
     * @param credential a credential used for this single-transaction authentication event.
     * @return authentication result representing a final outcome of the authentication event.
     * @throws AuthenticationException exception to indicate authentication processing failure.
     * @since 5.0.0
     */
    @Nullable AuthenticationResult finalizeAuthenticationTransaction(@Nullable Service service, Credential... credential) throws Throwable;

    /**
     * Handle a single-transaction authentication event and immediately produce a finalized {@link AuthenticationResult}.
     *
     * @param service     a service for this authentication event.
     * @param credentials credentials used for this single-transaction authentication event.
     * @return authentication result representing a final outcome of the authentication event.
     * @throws Throwable the throwable
     * @since 5.3.0
     */
    default @Nullable AuthenticationResult finalizeAuthenticationTransaction(@Nullable final Service service,
                                                                   final Collection<Credential> credentials) throws Throwable {
        return finalizeAuthenticationTransaction(service, credentials.toArray(Credential[]::new));
    }

    /**
     * Finalize authentication transaction.
     *
     * @param credentials the credentials
     * @return the authentication result
     * @throws Throwable the throwable
     */
    default @Nullable AuthenticationResult finalizeAuthenticationTransaction(final Credential... credentials) throws Throwable {
        return finalizeAuthenticationTransaction(null, credentials);
    }
}
