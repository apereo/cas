package org.apereo.cas.authentication.support;

import org.apereo.cas.authentication.MessageDescriptor;
import org.ldaptive.auth.AuthenticationResponse;

import javax.security.auth.login.LoginException;
import java.util.List;

/**
 * This is {@link LdapPasswordPolicyHandlingStrategy}.
 * Determine how to respond to password policy changes that may be indicated by the authentication response.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface LdapPasswordPolicyHandlingStrategy {

    /**
     * Handle.
     *
     * @param response      the response
     * @param configuration the configuration
     * @return the list
     * @throws LoginException the exception
     */
    List<MessageDescriptor> handle(AuthenticationResponse response, LdapPasswordPolicyConfiguration configuration) throws LoginException;

    /**
     * Decide if response is supported by this strategy.
     *
     * @param response the response
     * @return true /false
     */
    default boolean supports(final AuthenticationResponse response) {
        return response != null;
    }
}
