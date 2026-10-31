package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.springframework.security.core.AuthenticationException;

/**
 * Verifies the credentials of a {@link OAuth20ClientAuthenticationToken} and builds the profile of the authenticated
 * client, or of the user for the password grant. It is deliberately not a Spring Security
 * {@link org.springframework.security.authentication.AuthenticationProvider}, so that authenticators registered as beans
 * do not change how Spring Boot configures the authentication of the rest of the application;
 * {@link OAuth20ClientAuthenticationProvider} adapts it.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@FunctionalInterface
public interface OAuth20ClientCredentialsAuthenticator {

    /**
     * Authenticate the credentials.
     *
     * @param token the token with the credentials read from the request
     * @return the profile, or empty when the credentials are not for this authenticator or cannot be verified
     * @throws AuthenticationException when the credentials are rejected; the next client authentication method is tried,
     *                                 unless it is a {@link OAuth20ClientAuthenticationResponseException}
     */
    Optional<OAuth20UserProfile> authenticate(OAuth20ClientAuthenticationToken token) throws AuthenticationException;
}
