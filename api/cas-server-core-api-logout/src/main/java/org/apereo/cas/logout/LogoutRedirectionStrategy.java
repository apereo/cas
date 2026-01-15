package org.apereo.cas.logout;

import module java.base;
import org.apereo.cas.util.NamedObject;
import org.springframework.core.Ordered;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link LogoutRedirectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface LogoutRedirectionStrategy extends Ordered, NamedObject {
    /**
     * Default order value of th redirection strategy.
     */
    int DEFAULT_ORDER = 1_000;

    @Override
    default int getOrder() {
        return DEFAULT_ORDER;
    }

    /**
     * Whether this strategy supports the given context.
     *
     * @param request  the request
     * @param response the response
     * @return true /false
     */
    default boolean supports(final HttpServletRequest request, final HttpServletResponse response) {
        return true;
    }

    /**
     * Handle redirects.
     *
     * @param request  the request
     * @param response the response
     * @return the logout redirection response
     * @throws Exception the exception
     */
    LogoutRedirectionResponse handle(HttpServletRequest request, HttpServletResponse response) throws Exception;
}
