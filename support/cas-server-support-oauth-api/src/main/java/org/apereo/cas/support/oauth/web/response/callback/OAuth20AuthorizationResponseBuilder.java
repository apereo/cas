package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;

import org.pac4j.core.context.WebContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
@Order
public interface OAuth20AuthorizationResponseBuilder extends Ordered {
    /**
     * Build response model and view.
     *
     * @param registeredService the registered service
     * @param redirectUrl       the redirect url
     * @param parameters        the parameters
     * @return the model and view
     * @throws Exception the exception
     */
    ModelAndView build(OAuthRegisteredService registeredService,
                       OAuth20ResponseModeTypes responseMode,
                       String redirectUrl,
                       Map<String, String> parameters) throws Exception;

    /**
     * Build.
     *
     * @param clientId the client id
     * @param holder   the holder
     * @return the view response
     * @throws Exception the exception
     */
    ModelAndView build(String clientId,
                       AccessTokenRequestDataHolder holder) throws Exception;

    /**
     * Supports request?
     *
     * @param context the context
     * @return true/false
     */
    boolean supports(WebContext context);

    /**
     * Is single sign on session required for this builder?
     * This geneerally forces the presence of a ticket-granting ticket
     * to be found before this builder can operate further.
     * Some builders may be able to work without a session initially,
     * such as those that operate on PAR requests.
     *
     * @return the boolean
     */
    default boolean isSingleSignOnSessionRequired() {
        return true;
    }

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
