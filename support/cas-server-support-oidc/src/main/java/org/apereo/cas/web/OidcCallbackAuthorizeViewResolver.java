package org.apereo.cas.web;

import org.apereo.cas.OidcConstants;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.web.OAuth20CallbackAuthorizeViewResolver;
import org.apereo.cas.util.OidcAuthorizationRequestSupport;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OidcCallbackAuthorizeViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcCallbackAuthorizeViewResolver implements OAuth20CallbackAuthorizeViewResolver {
    private final OidcAuthorizationRequestSupport authorizationRequestSupport;

    public OidcCallbackAuthorizeViewResolver(final OidcAuthorizationRequestSupport authorizationRequestSupport) {
        this.authorizationRequestSupport = authorizationRequestSupport;
    }

    @Override
    public ModelAndView resolve(final J2EContext ctx, final ProfileManager manager, final String url) {
        if (authorizationRequestSupport.getOidcPromptFromAuthorizationRequest(url)
                .contains(OidcConstants.PROMPT_NONE)) {
            if (manager.get(true) != null) {
                return new ModelAndView(url);
            }
            final Map<String, String> model = new HashMap<>();
            model.put(OAuthConstants.ERROR, OidcConstants.LOGIN_REQUIRED);
            return new ModelAndView(new MappingJackson2JsonView(), model);
        }
        return new ModelAndView(new RedirectView(url));
    }
}
