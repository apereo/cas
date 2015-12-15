package org.jasig.cas.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This is {@link DefaultAuthenticationObjectsRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("defaultAuthenticationObjectsRepository")
public final class DefaultAuthenticationObjectsRepository implements AuthenticationObjectsRepository {

    @Autowired
    @Qualifier("defaultAuthenticationTransactionFactory")
    private AuthenticationTransactionFactory authenticationTransactionFactory = new DefaultAuthenticationTransactionFactory();

    @Autowired
    @Qualifier("defaultAuthenticationTransactionManager")
    private AuthenticationTransactionManager authenticationTransactionManager = new DefaultAuthenticationTransactionManager();

    @Autowired
    @Qualifier("principalElectionStrategy")
    private PrincipalElectionStrategy principalElectionStrategy = new DefaultPrincipalElectionStrategy();

    @Override
    public AuthenticationTransactionManager getAuthenticationTransactionManager() {
        return this.authenticationTransactionManager;
    }

    @Override
    public AuthenticationTransactionFactory getAuthenticationTransactionFactory() {
        return this.authenticationTransactionFactory;
    }

    @Override
    public PrincipalElectionStrategy getPrincipalElectionStrategy() {
        return this.principalElectionStrategy;
    }

    public void setAuthenticationTransactionFactory(final AuthenticationTransactionFactory authenticationTransactionFactory) {
        this.authenticationTransactionFactory = authenticationTransactionFactory;
    }

    public void setAuthenticationTransactionManager(final AuthenticationTransactionManager authenticationTransactionManager) {
        this.authenticationTransactionManager = authenticationTransactionManager;
    }

    public void setPrincipalElectionStrategy(final PrincipalElectionStrategy principalElectionStrategy) {
        this.principalElectionStrategy = principalElectionStrategy;
    }
}
