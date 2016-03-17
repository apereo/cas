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
 * @since 4.2.0
 */
@Component("defaultAuthenticationSystemSupport")
public class DefaultAuthenticationSystemSupport implements AuthenticationSystemSupport {

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
    public AuthenticationResultBuilder handleInitialAuthenticationTransaction(final Credential... credential) throws
            AuthenticationException {

        return this.handleAuthenticationTransaction(new DefaultAuthenticationResultBuilder(this.principalElectionStrategy), credential);
    }

    @Override
    public AuthenticationResultBuilder handleAuthenticationTransaction(final AuthenticationResultBuilder authenticationResultBuilder,
                                                                       final Credential... credential)
            throws AuthenticationException {

        this.authenticationTransactionManager.handle(AuthenticationTransaction.wrap(credential), authenticationResultBuilder);
        return authenticationResultBuilder;
    }

    @Override
    public AuthenticationResult finalizeAllAuthenticationTransactions(final AuthenticationResultBuilder authenticationResultBuilder,
                                                                      final Service service) {
        return authenticationResultBuilder.build(service);
    }

    @Override
    public AuthenticationResultBuilder establishAuthenticationContextFromInitial(final Authentication authentication){
        final AuthenticationResultBuilder builder =
                new DefaultAuthenticationResultBuilder(this.principalElectionStrategy).collect(authentication);
        return builder;

    }

    @Override
    public AuthenticationResult handleAndFinalizeSingleAuthenticationTransaction(final Service service, final Credential... credential)
            throws AuthenticationException {

        return this.finalizeAllAuthenticationTransactions(this.handleInitialAuthenticationTransaction(credential), service);
    }

    public void setAuthenticationTransactionManager(final AuthenticationTransactionManager authenticationTransactionManager) {
        this.authenticationTransactionManager = authenticationTransactionManager;
    }

    public void setPrincipalElectionStrategy(final PrincipalElectionStrategy principalElectionStrategy) {
        this.principalElectionStrategy = principalElectionStrategy;
    }
}
