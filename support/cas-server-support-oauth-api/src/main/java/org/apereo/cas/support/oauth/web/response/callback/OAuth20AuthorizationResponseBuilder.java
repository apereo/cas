package org.apereo.cas.support.oauth.web.response.callback;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.response.OAuth20AuthorizationRequest;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
     * @param responseMode      the response mode
     * @param redirectUrl       the redirect url
     * @param parameters        the parameters
     * @return the model and view
     * @throws Throwable the throwable
     */
    ModelAndView build(OAuthRegisteredService registeredService,
                       OAuth20ResponseModeTypes responseMode,
                       String redirectUrl,
                       Map<String, String> parameters)
            throws Throwable;

    /**
     * Build.
     *
     * @param holder the holder
     * @return the view response
     * @throws Throwable the throwable
     */
    ModelAndView build(AccessTokenRequestContext holder) throws Throwable;

    /**
     * Determines whether this builder supports the request.
     *
     * @param context the context
     * @return true/false
     */
    boolean supports(OAuth20AuthorizationRequest context);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * To authorization request.
     *
     * @param request           the request
     * @param response          the response
     * @param authentication    the authentication
     * @param service           the service
     * @param registeredService the registered service
     * @return the oauth authorization request
     */
    Optional<OAuth20AuthorizationRequest.OAuth20AuthorizationRequestBuilder> toAuthorizationRequest(
        HttpServletRequest request, HttpServletResponse response, Authentication authentication,
        Service service, OAuthRegisteredService registeredService);
}
