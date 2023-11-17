package org.apereo.cas.ticket;

import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.pac4j.core.profile.UserProfile;

/**
 * This is {@link IdTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface IdTokenGeneratorService {
    /**
     * Generate id token.
     *
     * @param accessToken       the access token
     * @param userProfile       the user profile
     * @param responseType      the response type
     * @param grantType         the grant type
     * @param registeredService the registered service
     * @return the string
     * @throws Throwable the throwable
     */
    OidcIdToken generate(OAuth20AccessToken accessToken,
                              UserProfile userProfile,
                              OAuth20ResponseTypes responseType,
                              OAuth20GrantTypes grantType,
                              OAuthRegisteredService registeredService) throws Throwable;
}
