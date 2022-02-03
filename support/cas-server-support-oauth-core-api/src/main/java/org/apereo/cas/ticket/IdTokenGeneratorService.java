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
     * @param timeoutInSeconds  the timeoutInSeconds
     * @param userProfile       the user profile
     * @param responseType      the response type
     * @param grantType         the grant type
     * @param registeredService the registered service
     * @return the string
     * @throws Exception the exception
     */
    String generate(OAuth20AccessToken accessToken,
                    long timeoutInSeconds,
                    UserProfile userProfile,
                    OAuth20ResponseTypes responseType,
                    OAuth20GrantTypes grantType,
                    OAuthRegisteredService registeredService) throws Exception;
}
