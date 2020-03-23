package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupport;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.HashMap;

/**
 * This is {@link OidcCallbackAuthorizeViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class OidcCallbackAuthorizeViewResolver implements OAuth20CallbackAuthorizeViewResolver {
    @Override
    public ModelAndView resolve(final JEEContext ctx, final ProfileManager manager, final String url) {
        val prompt = OidcAuthorizationRequestSupport.getOidcPromptFromAuthorizationRequest(url);
        if (prompt.contains(OidcConstants.PROMPT_NONE)) {
            val result = manager.get(true);
            if (result.isPresent()) {
                LOGGER.trace("Redirecting to URL [{}] without prompting for login", url);
                return new ModelAndView(new RedirectView(url));
            }
            val originalRedirectUrl = ctx.getRequestParameter(OAuth20Constants.REDIRECT_URI);

            if (originalRedirectUrl.isEmpty()) {
                val model = new HashMap<String, String>();
                model.put(OAuth20Constants.ERROR, OidcConstants.LOGIN_REQUIRED);
                return new ModelAndView(new MappingJackson2JsonView(), model);
            }
            val redirectUrlWithErrorCode = OidcAuthorizationRequestSupport.getRedirectUrlWithError(originalRedirectUrl.get(), OidcConstants.LOGIN_REQUIRED);
            LOGGER.warn("Unable to detect an authenticated user profile for prompt-less login attempts. Redirecting to URL [{}]", redirectUrlWithErrorCode);
            return new ModelAndView(new RedirectView(redirectUrlWithErrorCode));
        }
        if (prompt.contains(OidcConstants.PROMPT_LOGIN)) {
            LOGGER.trace("Removing login prompt from URL [{}]", url);
            val newUrl = OidcAuthorizationRequestSupport.removeOidcPromptFromAuthorizationRequest(url, OidcConstants.PROMPT_LOGIN);
            LOGGER.trace("Redirecting to URL [{}]", newUrl);
            return new ModelAndView(new RedirectView(newUrl));
        }
        LOGGER.trace("Redirecting to URL [{}]", url);
        return new ModelAndView(new RedirectView(url));
    }

}
