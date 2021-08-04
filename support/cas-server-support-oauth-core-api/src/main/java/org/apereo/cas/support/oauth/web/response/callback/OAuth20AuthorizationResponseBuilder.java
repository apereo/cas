package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;

import lombok.val;
import org.pac4j.core.context.JEEContext;
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
     * Build string.
     *
     * @param context  the context
     * @param clientId the client id
     * @param holder   the holder
     * @return the view response
     */
    ModelAndView build(JEEContext context,
                       String clientId,
                       AccessTokenRequestDataHolder holder);

    /**
     * Supports request?
     *
     * @param context the context
     * @return true/false
     */
    boolean supports(JEEContext context);

    /**
     * Build response model and view.
     *
     * @param context         the context
     * @param servicesManager the services manager
     * @param clientId        the client id
     * @param redirectUrl     the redirect url
     * @param parameters      the parameters
     * @return the model and view
     */
    default ModelAndView buildResponseModelAndView(final JEEContext context, final ServicesManager servicesManager,
                                                   final String clientId, final String redirectUrl,
                                                   final Map<String, String> parameters) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
        return OAuth20Utils.buildResponseModelAndView(context, registeredService, redirectUrl, parameters);
    }
}
