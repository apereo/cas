package org.apereo.cas.support.oauth.web.response.callback;

import org.apereo.cas.support.oauth.services.OAuthRegisteredService;

import org.pac4j.core.context.WebContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * This is {@link OAuth20AuthorizationModelAndViewBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@FunctionalInterface
public interface OAuth20AuthorizationModelAndViewBuilder {
    /**
     * Customize.
     *
     * @param context           the context
     * @param registeredService the registered service
     * @param redirectUrl       the redirect url
     * @param parameters        the parameters
     * @return the model and view
     */
    ModelAndView build(WebContext context,
                       OAuthRegisteredService registeredService,
                       String redirectUrl,
                       Map<String, String> parameters);
}
