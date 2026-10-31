package org.apereo.cas.support.oauth.web.views;

import module java.base;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link ConsentApprovalViewResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface ConsentApprovalViewResolver {

    /**
     * Resolve model and view.
     *
     * @param request  the request
     * @param response the response
     * @param service  the service
     * @return the model and view. Could be an empty view which would indicate consent is not required.
     * @throws Exception the exception
     */
    ModelAndView resolve(HttpServletRequest request, HttpServletResponse response, OAuthRegisteredService service) throws Exception;
}
