package org.apereo.cas.authentication;

import org.apereo.cas.support.events.authentication.CasAuthenticationTransactionCompletedEvent;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.NonNull;
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
public record DefaultAuthenticationTransactionManager(ApplicationEventPublisher eventPublisher, AuthenticationManager authenticationManager) implements AuthenticationTransactionManager {

    @Override
    @CanIgnoreReturnValue
    public AuthenticationTransactionManager handle(
        @NonNull
        final AuthenticationTransaction authenticationTransaction,
        @NonNull
        final AuthenticationResultBuilder authenticationResult)
        throws AuthenticationException {
        if (authenticationTransaction.getCredentials().isEmpty()) {
            LOGGER.debug("Transaction ignored since there are no credentials to authenticate");
        } else {
            val authentication = authenticationManager.authenticate(authenticationTransaction);
            LOGGER.trace("Successful authentication; Collecting authentication result [{}]", authentication);
            publishEvent(new CasAuthenticationTransactionCompletedEvent(this, authentication));
            authenticationResult.collect(authentication);
        }
        return this;
    }

    private void publishEvent(final ApplicationEvent event) {
        if (this.eventPublisher != null) {
            this.eventPublisher.publishEvent(event);
        }
    }
}
