package org.apereo.cas.support.oauth.web.response.accesstoken.response;

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
     * @param result the result
     * @return the model and view
     */
    ModelAndView generate(OAuth20AccessTokenResponseResult result);
}
