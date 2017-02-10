package org.apereo.cas.oidc.web;

import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.oidc.util.OidcAuthorizationRequestSupport;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuthConstants;
import org.apereo.cas.support.oauth.web.views.OAuth20CallbackAuthorizeViewResolver;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link OidcCallbackAuthorizeViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OidcCallbackAuthorizeViewResolver implements OAuth20CallbackAuthorizeViewResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(OidcCallbackAuthorizeViewResolver.class);

    private final OidcAuthorizationRequestSupport authorizationRequestSupport;
    private final ServicesManager servicesManager;
    private final OidcServerDiscoverySettings oidcServerDiscoverySettings;

    public OidcCallbackAuthorizeViewResolver(final OidcAuthorizationRequestSupport authorizationRequestSupport,
                                             final ServicesManager servicesManager,
                                             final OidcServerDiscoverySettings oidcServerDiscoverySettings) {
        this.authorizationRequestSupport = authorizationRequestSupport;
        this.servicesManager = servicesManager;
        this.oidcServerDiscoverySettings = oidcServerDiscoverySettings;
    }

    @Override
    public ModelAndView resolve(final J2EContext ctx, final ProfileManager manager, final String url) {
        final Set<String> prompt = authorizationRequestSupport.getOidcPromptFromAuthorizationRequest(url);
        if (prompt.contains(OidcConstants.PROMPT_NONE)) {
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
