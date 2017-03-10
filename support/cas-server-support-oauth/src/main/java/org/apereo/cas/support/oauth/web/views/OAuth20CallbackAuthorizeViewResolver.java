package org.apereo.cas.support.oauth.web.views;

import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This is {@link OAuth20CallbackAuthorizeViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface OAuth20CallbackAuthorizeViewResolver {
    
    /**
     * Pre callback redirect.
     *
     * @param ctx     the ctx
     * @param manager the manager
     * @param url     the url
     * @return true if the redirect should resume.
     */
    default ModelAndView resolve(final J2EContext ctx, final ProfileManager manager, final String url) {
        return new ModelAndView(new RedirectView(url));
    }
}
