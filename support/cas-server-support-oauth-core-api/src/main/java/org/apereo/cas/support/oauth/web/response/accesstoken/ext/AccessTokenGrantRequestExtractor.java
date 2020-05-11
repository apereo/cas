package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
     */
    AccessTokenRequestDataHolder extract(HttpServletRequest request, HttpServletResponse response);

    /**
     * Supports grant type?
     *
     * @param context the context
     * @return true /false
     */
    boolean supports(HttpServletRequest context);

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
