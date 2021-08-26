package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.pac4j.core.context.WebContext;
import org.springframework.web.servlet.ModelAndView;

/**
 * This is {@link OAuth20AccessTokenResponseGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface OAuth20AccessTokenResponseGenerator {

    /**
     * Generate.
     *
     * @param webContext the web context
     * @param result     the result
     * @return the model and view
     */
    ModelAndView generate(WebContext webContext, OAuth20AccessTokenResponseResult result);
}
