package org.apereo.cas.support.oauth.validator;

import module java.base;
import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link OAuth20RequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public interface OAuth20RequestValidator extends Ordered {

    /**
     * Validate request.
     *
     * @param request  the request
     * @param response the response
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean validate(HttpServletRequest request, HttpServletResponse response) throws Throwable;

    /**
     * Determines whether this validator supports the request.
     *
     * @param request  the request
     * @param response the response
     * @return true /false
     * @throws Throwable the throwable
     */
    boolean supports(HttpServletRequest request, HttpServletResponse response) throws Throwable;
}
