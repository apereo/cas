package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcRequestSupport;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.response.callback.OAuth20AuthorizationModelAndViewBuilder;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.HashMap;
import java.util.LinkedHashMap;

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

    @Override
    @SneakyThrows
    public ModelAndView resolve(final JEEContext context, final ProfileManager manager, final String url) {
        val prompt = OidcRequestSupport.getOidcPromptFromAuthorizationRequest(url);
        if (prompt.contains(OidcConstants.PROMPT_NONE)) {
            val result = manager.getProfile();
            if (result.isPresent()) {
                LOGGER.trace("Redirecting to URL [{}] without prompting for login", url);
                return new ModelAndView(new RedirectView(url));
            }
            val originalRedirectUrl = OAuth20Utils.getRequestParameter(context, OAuth20Constants.REDIRECT_URI);
            if (originalRedirectUrl.isEmpty()) {
                val model = new HashMap<String, String>();
                model.put(OAuth20Constants.ERROR, OidcConstants.LOGIN_REQUIRED);
                return new ModelAndView(new MappingJackson2JsonView(), model);
            }
            val parameters = new LinkedHashMap<String, String>();
            parameters.put(OAuth20Constants.ERROR, OidcConstants.LOGIN_REQUIRED);
            OAuth20Utils.getRequestParameter(context, OAuth20Constants.STATE)
                .ifPresent(state -> parameters.put(OAuth20Constants.STATE, state));
            val clientId = OAuth20Utils.getRequestParameter(context, OAuth20Constants.CLIENT_ID).orElse(StringUtils.EMPTY);
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);

            val responseType = OAuth20Utils.getResponseModeType(context);
            val redirect = FunctionUtils.doIf(OAuth20Utils.isResponseModeTypeFormPost(registeredService, responseType),
                    originalRedirectUrl::get,
                    () -> OidcRequestSupport.getRedirectUrlWithError(originalRedirectUrl.get(), OidcConstants.LOGIN_REQUIRED, context))
                .get();
            LOGGER.warn("Unable to detect authenticated user profile for prompt-less login attempts. Redirecting to URL [{}]", redirect);
            return authorizationModelAndViewBuilder.build(context, registeredService, redirect, parameters);
        }
        if (prompt.contains(OidcConstants.PROMPT_LOGIN)) {
            LOGGER.trace("Removing login prompt from URL [{}]", url);
            val newUrl = OidcRequestSupport.removeOidcPromptFromAuthorizationRequest(url, OidcConstants.PROMPT_LOGIN);
            LOGGER.trace("Redirecting to URL [{}]", newUrl);
            return new ModelAndView(new RedirectView(newUrl));
        }
        LOGGER.trace("Redirecting to URL [{}]", url);
        return new ModelAndView(new RedirectView(url));
    }

}
