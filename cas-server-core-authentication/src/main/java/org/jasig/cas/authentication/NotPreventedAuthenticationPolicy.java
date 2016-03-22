package org.jasig.cas.authentication;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Authentication policy that defines success as at least one authentication success and no authentication attempts
 * that were prevented by system errors. This policy may be a desirable alternative to {@link AnyAuthenticationPolicy}
 * for cases where deployers wish to fail closed for indeterminate security events.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RefreshScope
@Component("notPreventedAuthenticationPolicy")
public class NotPreventedAuthenticationPolicy extends AnyAuthenticationPolicy {

    @Override
    public boolean isSatisfiedBy(final Authentication authentication) {
        final boolean fail  = authentication.getFailures().values().stream()
                .anyMatch(failure -> failure.isAssignableFrom(PreventedException.class));
        if (fail) {
            return false;
        }
        return super.isSatisfiedBy(authentication);
    }
}
