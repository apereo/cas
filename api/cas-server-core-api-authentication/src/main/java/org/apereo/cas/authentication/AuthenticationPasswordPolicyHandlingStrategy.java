package org.apereo.cas.authentication;

/**
 * This is {@link AuthenticationPasswordPolicyHandlingStrategy}.
 *
 * @author Misagh Moayyed
 * @param <AuthnResponse> the type parameter
 * @param <Configuration> the type parameter
 * @since 5.3.0
 */
@FunctionalInterface
public interface AuthenticationPasswordPolicyHandlingStrategy<AuthnResponse, Configuration> extends AuthenticationAccountStateHandler<AuthnResponse, Configuration> {
    /**
     * Decide if response is supported by this strategy.
     *
     * @param response the response
     * @return true /false
     */
    default boolean supports(final AuthnResponse response) {
        return response != null;
    }
}
