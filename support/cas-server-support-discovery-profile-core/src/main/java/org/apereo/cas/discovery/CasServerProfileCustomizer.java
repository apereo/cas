package org.apereo.cas.discovery;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link CasServerProfileCustomizer}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@FunctionalInterface
public interface CasServerProfileCustomizer {
    /**
     * Customize.
     *
     * @param profile  the profile
     * @param request  the request
     * @param response the response
     */
    void customize(CasServerProfile profile, HttpServletRequest request, HttpServletResponse response);
}
