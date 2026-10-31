package org.apereo.cas.support.oauth.web.views;

import module java.base;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.jspecify.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
     * @param request  the request
     * @param response the response
     * @param profile  the authenticated profile of the request, if any
     * @param url      the url
     * @return the model and view
     */
    ModelAndView resolve(HttpServletRequest request, HttpServletResponse response, @Nullable OAuth20UserProfile profile, String url);

    /**
     * Default OAuth 2.0 callback authorize view resolver.
     *
     * @return the OAuth callback authorize view resolver
     */
    static OAuth20CallbackAuthorizeViewResolver asDefault() {
        return (request, response, profile, url) -> new ModelAndView(new RedirectView(url));
    }
}
