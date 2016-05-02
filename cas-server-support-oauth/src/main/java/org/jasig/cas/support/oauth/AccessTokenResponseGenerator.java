package org.jasig.cas.support.oauth;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.oauth.services.OAuthRegisteredService;
import org.jasig.cas.support.oauth.ticket.accesstoken.AccessToken;
import org.jasig.cas.support.oauth.ticket.refreshtoken.RefreshToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link AccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
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
     */
    void generate(HttpServletRequest request, 
                  HttpServletResponse response, 
                  OAuthRegisteredService registeredService,
                  Service service,
                  AccessToken accessTokenId, 
                  RefreshToken refreshTokenId, 
                  long timeout);
}
