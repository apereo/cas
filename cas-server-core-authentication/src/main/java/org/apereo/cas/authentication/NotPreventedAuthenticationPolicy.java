package org.apereo.cas.authentication;

/**
 * Authentication policy that defines success as at least one authentication success and no authentication attempts
 * that were prevented by system errors. This policy may be a desirable alternative to {@link AnyAuthenticationPolicy}
 * for cases where deployers wish to fail closed for indeterminate security events.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class NotPreventedAuthenticationPolicy extends AnyAuthenticationPolicy {

    public NotPreventedAuthenticationPolicy() {
        super(true);
    }

    @Override
    public boolean isSatisfiedBy(final Authentication authentication) {
        final boolean fail = authentication.getFailures().values().stream()
                .anyMatch(failure -> failure.isAssignableFrom(PreventedException.class));
        if (fail) {
            return false;
        }
        return super.isSatisfiedBy(authentication);
    }
}
