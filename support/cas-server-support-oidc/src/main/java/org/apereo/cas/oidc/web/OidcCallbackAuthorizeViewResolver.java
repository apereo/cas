package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupport;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.pac4j.core.context.J2EContext;
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
public class OidcCallbackAuthorizeViewResolver implements OAuth20CallbackAuthorizeViewResolver {
    private final OidcAuthorizationRequestSupport authorizationRequestSupport;

    @Override
    public ModelAndView resolve(final J2EContext ctx, final ProfileManager manager, final String url) {
        val prompt = authorizationRequestSupport.getOidcPromptFromAuthorizationRequest(url);
        if (prompt.contains(OidcConstants.PROMPT_NONE)) {
            val result = manager.get(true);
            if (result.isPresent()) {
                return new ModelAndView(url);
            }
            val model = new HashMap<String, String>();
            model.put(OAuth20Constants.ERROR, OidcConstants.LOGIN_REQUIRED);
            return new ModelAndView(new MappingJackson2JsonView(), model);
        }
        return new ModelAndView(new RedirectView(url));
    }

}
