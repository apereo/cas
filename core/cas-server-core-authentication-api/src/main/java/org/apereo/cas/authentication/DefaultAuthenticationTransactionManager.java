package org.apereo.cas.authentication;

import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

/**
 * This is {@link DefaultAuthenticationTransactionManager}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
public class DefaultAuthenticationTransactionManager implements AuthenticationTransactionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthenticationTransactionManager.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final AuthenticationManager authenticationManager;

    public DefaultAuthenticationTransactionManager(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthenticationTransactionManager handle(final AuthenticationTransaction authenticationTransaction,
                                                   final AuthenticationResultBuilder authenticationResult)
            throws AuthenticationException {
        if (!authenticationTransaction.getCredentials().isEmpty()) {
            final Authentication authentication = this.authenticationManager.authenticate(authenticationTransaction);
            LOGGER.debug("Successful authentication; Collecting authentication result [{}]", authentication);
            publishEvent(new CasAuthenticationTransactionCompletedEvent(this, authentication));
            authenticationResult.collect(authentication);
        } else {
            LOGGER.debug("Transaction ignored since there are no credentials to authenticate");
        }
        return this;
    }

    @Override
    public AuthenticationManager getAuthenticationManager() {
        return this.authenticationManager;
    }

    private void publishEvent(final ApplicationEvent event) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(event);
        }
    }
}
