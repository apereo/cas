package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.PreventedException;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Set;

/**
 * Authentication policy that defines success as at least one authentication success and no authentication attempts
 * that were prevented by system errors. This policy may be a desirable alternative to {@link AtLeastOneCredentialValidatedAuthenticationPolicy}
 * for cases where deployers wish to fail closed for indeterminate security events.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
public class NotPreventedAuthenticationPolicy extends AtLeastOneCredentialValidatedAuthenticationPolicy {


    public NotPreventedAuthenticationPolicy() {
        super(true);
    }

    @Override
    public boolean isSatisfiedBy(final Authentication authentication, final Set<AuthenticationHandler> authenticationHandlers) throws Exception {
        val fail = authentication.getFailures().values()
            .stream()
            .anyMatch(failure -> failure.getClass().isAssignableFrom(PreventedException.class));
        if (fail) {
            LOGGER.warn("Authentication policy has failed given at least one authentication failure is found to prevent authentication");
            return false;
        }
        return super.isSatisfiedBy(authentication, authenticationHandlers);
    }
}
