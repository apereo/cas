package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.util.Pac4jUtils;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.http.adapter.J2ENopHttpActionAdapter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * OAuth callback authorize controller based on the pac4j callback controller.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
public class OAuth20CallbackAuthorizeEndpointController extends BaseOAuth20Controller {
    public OAuth20CallbackAuthorizeEndpointController(final OAuth20ControllerConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     */
    @GetMapping(path = OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.CALLBACK_AUTHORIZE_URL)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) {
        val context = new J2EContext(request, response, getOAuthConfigurationContext().getOauthConfig().getSessionStore());
        val callback = new DefaultCallbackLogic();
        callback.perform(context, getOAuthConfigurationContext().getOauthConfig(), J2ENopHttpActionAdapter.INSTANCE,
            null, Boolean.TRUE, Boolean.FALSE,
            Boolean.FALSE, Authenticators.CAS_OAUTH_CLIENT);
        val url = StringUtils.remove(response.getHeader("Location"), "redirect:");
        val manager = Pac4jUtils.getPac4jProfileManager(request, response);
        return getOAuthConfigurationContext().getCallbackAuthorizeViewResolver().resolve(context, manager, url);
    }
}
