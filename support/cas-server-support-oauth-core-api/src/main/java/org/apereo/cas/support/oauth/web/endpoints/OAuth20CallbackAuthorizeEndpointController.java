package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.session.OAuth20SessionStoreMismatchException;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.context.Pac4jConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.exception.HttpAction;
import org.pac4j.core.http.adapter.J2ENopHttpActionAdapter;
import org.pac4j.core.profile.ProfileManager;
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
@Slf4j
public class OAuth20CallbackAuthorizeEndpointController extends BaseOAuth20Controller {
    public OAuth20CallbackAuthorizeEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
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
        val context = new J2EContext(request, response, getOAuthConfigurationContext().getSessionStore());
        val callback = new OAuth20CallbackLogic();
        callback.perform(context, getOAuthConfigurationContext().getOauthConfig(), J2ENopHttpActionAdapter.INSTANCE,
            context.getFullRequestURL(), Boolean.TRUE, Boolean.FALSE,
            Boolean.FALSE, Authenticators.CAS_OAUTH_CLIENT);
        var url = callback.getRedirectUrl();
        if (StringUtils.isBlank(url)) {
            val msg = "Unable to locate OAuth redirect URL from the session store, which usually is indicative of a stale or mismatched session request.";
            LOGGER.error(msg);
            return WebUtils.produceErrorView(OAuth20Constants.SESSION_STALE_MISMATCH, new OAuth20SessionStoreMismatchException(msg));
        }
        val manager = new ProfileManager<>(context, context.getSessionStore());
        LOGGER.trace("OAuth callback URL is [{}]", url);
        return getOAuthConfigurationContext().getCallbackAuthorizeViewResolver().resolve(context, manager, url);
    }

    @Getter
    private static class OAuth20CallbackLogic extends DefaultCallbackLogic {
        private String redirectUrl;

        @Override
        protected HttpAction redirectToOriginallyRequestedUrl(final WebContext context, final String defaultUrl) {
            val requestedUrl = (String) context.getSessionStore().get(context, Pac4jConstants.REQUESTED_URL);
            context.getSessionStore().set(context, Pac4jConstants.REQUESTED_URL, null);
            this.redirectUrl = requestedUrl;
            return HttpAction.status(HttpConstants.OK, context);
        }
    }
}
