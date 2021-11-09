package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;

import org.pac4j.core.context.WebContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

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
     * Build response model and view.
     *
     * @param context           the context
     * @param registeredService the registered service
     * @param redirectUrl       the redirect url
     * @param parameters        the parameters
     * @return the model and view
     * @throws Exception the exception
     */
    ModelAndView build(WebContext context,
                       OAuthRegisteredService registeredService,
                       String redirectUrl,
                       Map<String, String> parameters) throws Exception;

    /**
     * Build.
     *
     * @param context  the context
     * @param clientId the client id
     * @param holder   the holder
     * @return the view response
     */
    ModelAndView build(WebContext context,
                       String clientId,
                       AccessTokenRequestDataHolder holder);

    /**
     * Supports request?
     *
     * @param context the context
     * @return true/false
     */
    boolean supports(WebContext context);
    
}
