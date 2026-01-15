package org.apereo.cas.oidc.web;

import module java.base;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcRequestSupport;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20ResponseModeFactory;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.JacksonJsonView;

/**
 * This is {@link OidcCallbackAuthorizeViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcCallbackAuthorizeViewResolver implements OAuth20CallbackAuthorizeViewResolver {
    private final ServicesManager servicesManager;

    private final OAuth20AuthorizationModelAndViewBuilder authorizationModelAndViewBuilder;

    private final OAuth20RequestParameterResolver oauthRequestParameterResolver;

    @Override
    public ModelAndView resolve(final WebContext context, final ProfileManager manager, final String url) {
        val prompt = oauthRequestParameterResolver.resolveSupportedPromptValues(url);
        if (prompt.contains(OidcConstants.PROMPT_NONE)) {
            val result = manager.getProfile();
            if (result.isPresent()) {
                LOGGER.trace("Redirecting to URL [{}] without prompting for login", url);
                return OAuth20CallbackAuthorizeViewResolver.asDefault().resolve(context, manager, url);
            }
            val originalRedirectUrl = oauthRequestParameterResolver.resolveRequestParameter(context, OAuth20Constants.REDIRECT_URI);
            if (originalRedirectUrl.isEmpty()) {
                val model = new HashMap<String, String>();
                model.put(OAuth20Constants.ERROR, OidcConstants.LOGIN_REQUIRED);
                return new ModelAndView(new JacksonJsonView(), model);
            }
            val parameters = new LinkedHashMap<String, String>();
            parameters.put(OAuth20Constants.ERROR, OidcConstants.LOGIN_REQUIRED);
            oauthRequestParameterResolver.resolveRequestParameter(context, OAuth20Constants.STATE)
                .ifPresent(state -> parameters.put(OAuth20Constants.STATE, state));
            val clientId = oauthRequestParameterResolver.resolveRequestParameter(context, OAuth20Constants.CLIENT_ID).orElse(StringUtils.EMPTY);
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);

            OAuth20Utils.validateRedirectUri(originalRedirectUrl.get());
            val responseType = oauthRequestParameterResolver.resolveResponseModeType(context);
            val redirect = FunctionUtils.doIf(OAuth20ResponseModeFactory.isResponseModeTypeFormPost(registeredService, responseType),
                    originalRedirectUrl::get,
                    () -> OidcRequestSupport.getRedirectUrlWithError(originalRedirectUrl.get(), OidcConstants.LOGIN_REQUIRED, context))
                .get();
            return FunctionUtils.doUnchecked(() -> {
                LOGGER.warn("Unable to detect authenticated user profile for prompt-less login attempts. Redirecting to URL [{}]", redirect);
                return authorizationModelAndViewBuilder.build(registeredService, responseType, redirect, parameters);
            });
        }
        if (prompt.contains(OidcConstants.PROMPT_LOGIN)) {
            LOGGER.trace("Removing login prompt from URL [{}]", url);
            val newUrl = OidcRequestSupport.removeOidcPromptFromAuthorizationRequest(url, OidcConstants.PROMPT_LOGIN);
            LOGGER.trace("Redirecting to URL [{}]", newUrl);
            return OAuth20CallbackAuthorizeViewResolver.asDefault().resolve(context, manager, newUrl);
        }
        LOGGER.trace("Redirecting to URL [{}]", url);
        return OAuth20CallbackAuthorizeViewResolver.asDefault().resolve(context, manager, url);
    }

}
