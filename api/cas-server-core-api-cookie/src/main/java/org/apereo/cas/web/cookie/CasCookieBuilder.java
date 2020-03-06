package org.apereo.cas.web.cookie;

import org.apereo.cas.authentication.RememberMeCredential;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link CasCookieBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface CasCookieBuilder {
    /**
     * Adds the cookie, taking into account {@link RememberMeCredential#REQUEST_PARAMETER_REMEMBER_ME}
     * in the request.
     *
     * @param request     the request
     * @param response    the response
     * @param rememberMe  the remember me
     * @param cookieValue the cookie value
     */
    void addCookie(HttpServletRequest request, HttpServletResponse response,
                   boolean rememberMe, String cookieValue);

    /**
     * Add cookie.
     *
     * @param request     the request
     * @param response    the response
     * @param cookieValue the cookie value
     */
    void addCookie(HttpServletRequest request, HttpServletResponse response, String cookieValue);

    /**
     * Add cookie.
     *
     * @param response    the response
     * @param cookieValue the cookie value
     */
    void addCookie(HttpServletResponse response, String cookieValue);

    /**
     * Retrieve cookie value.
     *
     * @param request the request
     * @return the cookie value
     */
    String retrieveCookieValue(HttpServletRequest request);

    /**
     * Remove cookie.
     *
     * @param response the response
     */
    void removeCookie(HttpServletResponse response);

    /**
     * Gets cookie path.
     *
     * @return the cookie path
     */
    String getCookiePath();

    /**
     * Sets cookie path.
     *
     * @param path the path
     */
    void setCookiePath(String path);

    /**
     * Gets cookie domain.
     *
     * @return the cookie domain
     */
    String getCookieDomain();

    /**
     * Get cookie name.
     *
     * @return the string
     */
    String getCookieName();
}
