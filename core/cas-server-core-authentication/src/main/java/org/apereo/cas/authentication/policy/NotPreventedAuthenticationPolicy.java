package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.PreventedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication policy that defines success as at least one authentication success and no authentication attempts
 * that were prevented by system errors. This policy may be a desirable alternative to {@link AnyAuthenticationPolicy}
 * for cases where deployers wish to fail closed for indeterminate security events.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class NotPreventedAuthenticationPolicy extends AnyAuthenticationPolicy {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotPreventedAuthenticationPolicy.class);
    
    public NotPreventedAuthenticationPolicy() {
        super(true);
    }

    @Override
    public boolean isSatisfiedBy(final Authentication authentication) throws Exception {
        final boolean fail = authentication.getFailures().values().stream()
                .anyMatch(failure -> failure.isAssignableFrom(PreventedException.class));
        if (fail) {
            LOGGER.warn("Authentication policy has failed given at least one authentication failure is found to prevent authentication");
            return false;
        }
        return super.isSatisfiedBy(authentication);
    }
}
