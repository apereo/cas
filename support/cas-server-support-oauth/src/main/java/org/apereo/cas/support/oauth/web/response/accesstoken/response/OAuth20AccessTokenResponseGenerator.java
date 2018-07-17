package org.apereo.cas.support.oauth.web.response.accesstoken.response;

import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
     * @param request  the request
     * @param response the response
     * @param result   the result
     * @return the model and view
     */
    ModelAndView generate(HttpServletRequest request, HttpServletResponse response, OAuth20AccessTokenResponseResult result);
}
