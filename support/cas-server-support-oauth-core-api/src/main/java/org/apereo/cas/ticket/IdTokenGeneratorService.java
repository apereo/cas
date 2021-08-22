package org.apereo.cas.ticket;

import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import org.pac4j.core.context.WebContext;

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
     * @param webContext        the web context
     * @param accessToken       the access token
     * @param timeoutInSeconds  the timeoutInSeconds
     * @param responseType      the response type
     * @param grantType         the grant type
     * @param registeredService the registered service
     * @return the string
     */
    String generate(WebContext webContext,
                    OAuth20AccessToken accessToken,
                    long timeoutInSeconds,
                    OAuth20ResponseTypes responseType,
                    OAuth20GrantTypes grantType,
                    OAuthRegisteredService registeredService);
}
