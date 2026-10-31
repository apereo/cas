package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import module java.base;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link AccessTokenGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public interface AccessTokenGrantRequestExtractor {

    /**
     * Extract access token into a request data holder.
     *
     * @param request  the request
     * @param response the response
     * @return the access token request data holder
     * @throws Throwable the throwable
     */
    AccessTokenRequestContext extract(HttpServletRequest request, HttpServletResponse response) throws Throwable;

    /**
     * Determines whether this extractor supports the grant type.
     *
     * @param request the request
     * @return true /false
     */
    boolean supports(HttpServletRequest request);

    /**
     * Gets grant type.
     *
     * @return the grant type
     */
    OAuth20GrantTypes getGrantType();

    /**
     * Gets response type.
     *
     * @return the grant type
     */
    OAuth20ResponseTypes getResponseType();

    /**
     * Request must be authenticated.
     *
     * @return true/false
     */
    default boolean requestMustBeAuthenticated() {
        return false;
    }
}
