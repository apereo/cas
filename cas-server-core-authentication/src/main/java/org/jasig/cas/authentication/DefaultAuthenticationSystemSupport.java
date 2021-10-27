package org.jasig.cas.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * This is {@link DefaultAuthenticationSystemSupport}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Component("defaultAuthenticationSystemSupport")
public final class DefaultAuthenticationSystemSupport implements AuthenticationSystemSupport {

    @Autowired(required=false)
    @Qualifier("defaultAuthenticationTransactionManager")
    private AuthenticationTransactionManager authenticationTransactionManager = new DefaultAuthenticationTransactionManager();

    @Autowired(required=false)
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

    public void setAuthenticationTransactionManager(final AuthenticationTransactionManager authenticationTransactionManager) {
        this.authenticationTransactionManager = authenticationTransactionManager;
    }

    public void setPrincipalElectionStrategy(final PrincipalElectionStrategy principalElectionStrategy) {
        this.principalElectionStrategy = principalElectionStrategy;
    }
}
