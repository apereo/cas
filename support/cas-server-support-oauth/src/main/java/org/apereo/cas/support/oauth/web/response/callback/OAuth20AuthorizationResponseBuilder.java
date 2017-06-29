package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.pac4j.core.context.J2EContext;
import org.springframework.web.servlet.View;

/**
 * This is {@link OAuth20AuthorizationResponseBuilder} that attempts to build the callback url
 * with the access token, refresh token, etc as part of the authorization phase.
 * Individual subclasses need to decide how to prepare the uri, and they are typically mapped
 * to response types.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface OAuth20AuthorizationResponseBuilder {

    /**
     * Build string.
     *
     * @param context  the context
     * @param clientId the client id
     * @param holder   the holder
     * @return the view response
     */
    View build(J2EContext context,
               String clientId,
               AccessTokenRequestDataHolder holder);

    /**
     * Supports request?
     *
     * @param context the context
     * @return the boolean
     */
    boolean supports(J2EContext context);
}
