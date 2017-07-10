package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link AccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface AccessTokenResponseGenerator {

    /**
     * Generate.
     *
     * @param request           the request
     * @param response          the response
     * @param registeredService the registered service
     * @param service           the service
     * @param accessTokenId     the access token
     * @param refreshTokenId    the refresh token id
     * @param timeout           the timeout
     * @param responseType      the response type
     */
    void generate(HttpServletRequest request,
                  HttpServletResponse response,
                  OAuthRegisteredService registeredService,
                  Service service,
                  AccessToken accessTokenId,
                  RefreshToken refreshTokenId,
                  long timeout,
                  OAuth20ResponseTypes responseType);
}
