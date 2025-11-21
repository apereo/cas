package org.apereo.cas.web.cookie;

import org.apereo.cas.authentication.RememberMeCredential;

import org.jspecify.annotations.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link CasCookieBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface CasCookieBuilder {
    /**
     * Bean name that generates the cookie for the ticket-granting cookie.
     */
    String BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER = "ticketGrantingTicketCookieGenerator";
    /**
     * Bean name that generates the warning cookie.
     */
    String BEAN_NAME_WARN_COOKIE_BUILDER = "warnCookieGenerator";

    /**
     * Adds the cookie, taking into account {@link RememberMeCredential#REQUEST_PARAMETER_REMEMBER_ME}
     * in the request.
     *
     * @param request     the request
     * @param response    the response
     * @param rememberMe  the remember me
     * @param cookieValue the cookie value
     * @return the cookie
     */
    Cookie addCookie(HttpServletRequest request, HttpServletResponse response,
                     boolean rememberMe, String cookieValue);

    /**
     * Add cookie.
     *
     * @param request     the request
     * @param response    the response
     * @param cookieValue the cookie value
     * @return the cookie
     */
    Cookie addCookie(HttpServletRequest request, HttpServletResponse response, String cookieValue);

    /**
     * Retrieve cookie value.
     *
     * @param request the request
     * @return the cookie value
     */
    @Nullable String retrieveCookieValue(HttpServletRequest request);

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

    /**
     * Remove all cookies by the same name.
     * Attempts to ensure all variations of the same cookie
     * that may have been issued under root, or those with a lingering {@code /}
     * are removed from the response.
     *
     * @param request  the request
     * @param response the response
     */
    void removeAll(HttpServletRequest request, HttpServletResponse response);

    /**
     * Gets cas cookie value manager.
     *
     * @return the cas cookie value manager
     */
    CookieValueManager getCasCookieValueManager();

    /**
     * Contains cookie.
     *
     * @param request the request
     * @return true/false
     */
    boolean containsCookie(HttpServletRequest request);
}
