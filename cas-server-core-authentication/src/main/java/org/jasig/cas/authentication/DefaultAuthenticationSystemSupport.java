package org.jasig.cas.authentication;

import org.jasig.cas.authentication.principal.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This is {@link DefaultAuthenticationSystemSupport}.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @see {@link AuthenticationSystemSupport}
 * @since 4.2.0
 */
@Component("defaultAuthenticationSystemSupport")
public final class DefaultAuthenticationSystemSupport implements AuthenticationSystemSupport {

    @Autowired(required = false)
    @Qualifier("defaultAuthenticationTransactionManager")
    private AuthenticationTransactionManager authenticationTransactionManager = new DefaultAuthenticationTransactionManager();

    @Autowired(required = false)
    @Qualifier("principalElectionStrategy")
    private PrincipalElectionStrategy principalElectionStrategy = new DefaultPrincipalElectionStrategy();

    @Override
    public AuthenticationTransactionManager getAuthenticationTransactionManager() {
        return this.authenticationTransactionManager;
    }

    @Override
    public PrincipalElectionStrategy getPrincipalElectionStrategy() {
        return this.principalElectionStrategy;
    }

    @Override
    public AuthenticationContextBuilder handleInitialAuthenticationTransaction(final Credential... credential) throws
            AuthenticationException {
        final AuthenticationContextBuilder builder = new DefaultAuthenticationContextBuilder(this.principalElectionStrategy);
        this.handleAuthenticationTransaction(builder, credential);
        return builder;
    }

    @Override
    public void handleAuthenticationTransaction(final AuthenticationContextBuilder authenticationContextBuilder,
                                                final Credential... credential)
            throws AuthenticationException {

        final AuthenticationTransaction tx = AuthenticationTransaction.wrap(credential);
        this.authenticationTransactionManager.handle(tx, authenticationContextBuilder);
    }

    @Override
    public AuthenticationContext finalizeAuthenticationAttempt(final AuthenticationContextBuilder authenticationContextBuilder,
                                                               final Service service) {
        return authenticationContextBuilder.build(service);
    }

    @Override
    public AuthenticationContext handleFinalizedAuthenticationAttempt(final Service service, final Credential... credential)
            throws AuthenticationException {

        final AuthenticationContextBuilder builder = this.handleInitialAuthenticationTransaction(credential);
        return this.finalizeAuthenticationAttempt(builder, service);
    }

    public void setAuthenticationTransactionManager(final AuthenticationTransactionManager authenticationTransactionManager) {
        this.authenticationTransactionManager = authenticationTransactionManager;
    }

    public void setPrincipalElectionStrategy(final PrincipalElectionStrategy principalElectionStrategy) {
        this.principalElectionStrategy = principalElectionStrategy;
    }
}
