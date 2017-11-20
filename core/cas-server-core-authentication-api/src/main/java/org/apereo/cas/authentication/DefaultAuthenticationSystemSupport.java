package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Service;

/**
 * This is {@link DefaultAuthenticationSystemSupport}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 4.2.0
 */
public class DefaultAuthenticationSystemSupport implements AuthenticationSystemSupport {

    private final AuthenticationTransactionManager authenticationTransactionManager;
    private final PrincipalElectionStrategy principalElectionStrategy;

    public DefaultAuthenticationSystemSupport(final AuthenticationTransactionManager authenticationTransactionManager,
                                              final PrincipalElectionStrategy principalElectionStrategy) {
        this.authenticationTransactionManager = authenticationTransactionManager;
        this.principalElectionStrategy = principalElectionStrategy;
    }

    @Override
    public AuthenticationTransactionManager getAuthenticationTransactionManager() {
        return this.authenticationTransactionManager;
    }

    @Override
    public PrincipalElectionStrategy getPrincipalElectionStrategy() {
        return this.principalElectionStrategy;
    }

    @Override
    public AuthenticationResultBuilder handleInitialAuthenticationTransaction(final Service service,
                                                                              final Credential... credential) throws AuthenticationException {
        final DefaultAuthenticationResultBuilder builder = new DefaultAuthenticationResultBuilder(this.principalElectionStrategy);
        if (credential != null && credential.length > 0) {
            builder.collect(credential[0]);
        }

        return this.handleAuthenticationTransaction(service, builder, credential);
    }

    @Override
    public AuthenticationResultBuilder establishAuthenticationContextFromInitial(final Authentication authentication, final Credential credentials) {
        return new DefaultAuthenticationResultBuilder(this.principalElectionStrategy).collect(authentication).collect(credentials);
    }

    @Override
    public AuthenticationResultBuilder handleAuthenticationTransaction(final Service service,
                                                                       final AuthenticationResultBuilder authenticationResultBuilder,
                                                                       final Credential... credential) throws AuthenticationException {

        final AuthenticationTransaction transaction = AuthenticationTransaction.wrap(service, credential);
        this.authenticationTransactionManager.handle(transaction, authenticationResultBuilder);
        return authenticationResultBuilder;
    }

    @Override
    public AuthenticationResult finalizeAllAuthenticationTransactions(final AuthenticationResultBuilder authenticationResultBuilder,
                                                                      final Service service) {
        return authenticationResultBuilder.build(service);
    }

    @Override
    public AuthenticationResult handleAndFinalizeSingleAuthenticationTransaction(final Service service, final Credential... credential)
            throws AuthenticationException {

        return finalizeAllAuthenticationTransactions(handleInitialAuthenticationTransaction(service, credential), service);
    }
}
