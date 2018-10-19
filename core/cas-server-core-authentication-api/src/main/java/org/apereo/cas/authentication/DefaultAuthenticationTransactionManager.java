package org.apereo.cas.authentication;

import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionCompletedEvent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

/**
 * This is {@link DefaultAuthenticationTransactionManager}.
 *
 * @author Misagh Moayyed
 * @since 4.2.0
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class DefaultAuthenticationTransactionManager implements AuthenticationTransactionManager {

    private final ApplicationEventPublisher eventPublisher;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthenticationTransactionManager handle(final AuthenticationTransaction authenticationTransaction,
                                                   final AuthenticationResultBuilder authenticationResult)
        throws AuthenticationException {
        if (!authenticationTransaction.getCredentials().isEmpty()) {
            val authentication = this.authenticationManager.authenticate(authenticationTransaction);
            LOGGER.trace("Successful authentication; Collecting authentication result [{}]", authentication);
            publishEvent(new CasAuthenticationTransactionCompletedEvent(this, authentication));
            authenticationResult.collect(authentication);
        } else {
            LOGGER.debug("Transaction ignored since there are no credentials to authenticate");
        }
        return this;
    }

    private void publishEvent(final ApplicationEvent event) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(event);
        }
    }
}
