package org.apereo.cas.ticket;

import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link IdTokenGeneratorService}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface IdTokenGeneratorService {
    /**
     * Generate string.
     *
     * @param request           the request
     * @param response          the response
     * @param accessToken       the access token
     * @param timeoutInSeconds  the timeoutInSeconds
     * @param responseType      the response type
     * @param registeredService the registered service
     * @return the string
     */
    String generate(HttpServletRequest request,
                    HttpServletResponse response,
                    OAuth20AccessToken accessToken,
                    long timeoutInSeconds,
                    OAuth20ResponseTypes responseType,
                    OAuthRegisteredService registeredService);
}
