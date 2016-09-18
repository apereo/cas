package org.apereo.cas.web.support;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * The {@link CookieValueManager} is responsible for
 * managing all cookies and their value structure for CAS. Implementations
 * may choose to encode and sign the cookie value and optionally perform
 * additional checks to ensure the integrity of the cookie.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface CookieValueManager {

    /**
     * Build cookie value.
     *
     * @param givenCookieValue the given cookie value
     * @param request the request
     * @return the original cookie value
     */
    String buildCookieValue(String givenCookieValue, HttpServletRequest request);

    /**
     * Obtain cookie value.
     *
     * @param cookie the cookie
     * @param request the request
     * @return the cookie value or null
     */
    String obtainCookieValue(Cookie cookie, HttpServletRequest request);
}
