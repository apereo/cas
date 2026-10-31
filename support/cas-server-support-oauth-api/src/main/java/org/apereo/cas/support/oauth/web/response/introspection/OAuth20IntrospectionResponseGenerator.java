package org.apereo.cas.support.oauth.web.response.introspection;

import module java.base;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.OAuth20Token;
import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20IntrospectionResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public interface OAuth20IntrospectionResponseGenerator extends Ordered {
    /**
     * Generate introspection access token response.
     *
     * @param accessTokenId     the access token id
     * @param accessToken       the access token
     * @param registeredService the registered service of the client the token was issued to
     * @return the response
     */
    OAuth20IntrospectionAccessTokenResponse generate(String accessTokenId, OAuth20Token accessToken,
                                                     OAuthRegisteredService registeredService);

    @Override
    default int getOrder() {
        return 0;
    }

    /**
     * Supports this token.
     *
     * @param accessToken the access token
     * @return true /false
     */
    default boolean supports(final OAuth20Token accessToken) {
        return true;
    }
}
