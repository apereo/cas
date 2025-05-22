package org.apereo.cas.support.oauth.web.views;

import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This is {@link OAuth20CallbackAuthorizeViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface OAuth20CallbackAuthorizeViewResolver {

    /**
     * Pre callback redirect.
     *
     * @param ctx     the ctx
     * @param manager the manager
     * @param url     the url
     * @return the model and view
     */
    ModelAndView resolve(WebContext ctx, ProfileManager manager, String url);

    /**
     * Default oauth callback authorize view resolver.
     *
     * @return the oauth callback authorize view resolver
     */
    static OAuth20CallbackAuthorizeViewResolver asDefault() {
        return (ctx, manager, url) -> new ModelAndView(new RedirectView(url));
    }
}
