package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.support.oauth.util.OAuth20Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.engine.DefaultCallbackLogic;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.WithLocationAction;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.jee.context.JEEFrameworkParameters;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * OAuth callback authorize controller based on the pac4j callback controller.
 *
 * @author Jerome Leleu
 * @since 3.5.0
 */
@Slf4j
@Tag(name = "OAuth")
public class OAuth20CallbackAuthorizeEndpointController extends BaseOAuth20Controller<OAuth20ConfigurationContext> {

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
    @Operation(summary = "Handle OAuth callback authorize request", hidden = true)
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) {
        ensureSessionReplicationIsAutoconfiguredIfNeedBe(request);

        val callback = new OAuth20CallbackLogic();
        val context = new JEEContext(request, response);
        String defaultUrl = null;
        val clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID);
        val redirectUri = context.getRequestParameter(OAuth20Constants.REDIRECT_URI);
        if (clientId.isPresent() && redirectUri.isPresent()) {
            val servicesManager = getConfigurationContext().getServicesManager();
            val serviceClient = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId.get());
            if (serviceClient != null && serviceClient.matches(redirectUri.get())) {
                defaultUrl = redirectUri.get();
            }
        }
        callback.perform(getConfigurationContext().getOauthConfig(),
            defaultUrl, Boolean.FALSE, Authenticators.CAS_OAUTH_CLIENT,
            new JEEFrameworkParameters(request, response));
        val url = callback.getRedirectUrl();
        val manager = new ProfileManager(context, getConfigurationContext().getSessionStore());
        LOGGER.trace("OAuth callback URL is [{}]", url);
        return getConfigurationContext().getCallbackAuthorizeViewResolver().resolve(context, manager, url);
    }

    @Getter
    private static final class OAuth20CallbackLogic extends DefaultCallbackLogic {
        private String redirectUrl;

        @Override
        protected HttpAction redirectToOriginallyRequestedUrl(final CallContext callContext,
                                                              final String defaultUrl) {
            val result = getSavedRequestHandler().restore(callContext, defaultUrl);
            if (result instanceof final WithLocationAction locationAction) {
                redirectUrl = locationAction.getLocation();
            }
            return result;
        }
    }
}
