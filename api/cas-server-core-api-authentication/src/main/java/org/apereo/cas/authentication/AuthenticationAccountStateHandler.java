package org.apereo.cas.authentication;

import javax.security.auth.login.LoginException;
import java.util.List;

/**
 * This is {@link AuthenticationAccountStateHandler}.
 *
 * @param <AuthnResponse> the type parameter
 * @param <Configuration> the type parameter
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface AuthenticationAccountStateHandler<AuthnResponse, Configuration> {
    /**
     * Handle account state.
     *
     * @param response      the response
     * @param configuration the configuration
     * @return the list
     * @throws LoginException the login exception
     */
    List<MessageDescriptor> handle(AuthnResponse response, Configuration configuration) throws LoginException;
}

