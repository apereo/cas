package org.apereo.cas.web.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.io.Serial;
import java.io.Serializable;

/**
 * The {@link CookieValueManager} is responsible for
 * managing all cookies and their value structure for CAS. Implementations
 * may choose to encode and sign the cookie value and optionally perform
 * additional checks to ensure the integrity of the cookie.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public interface CookieValueManager extends Serializable {

    /**
     * Default bean name.
     */
    String BEAN_NAME = "cookieValueManager";

    /**
     * No op cookie value manager.
     *
     * @return the cookie value manager
     */
    static CookieValueManager noOp() {
        return new NoOpCookieValueManager();
    }

    /**
     * Build cookie value.
     *
     * @param givenCookieValue the given cookie value
     * @param request          the request
     * @return the original cookie value
     */
    String buildCookieValue(String givenCookieValue, HttpServletRequest request);

    /**
     * Obtain cookie value.
     *
     * @param cookie  the cookie
     * @param request the request
     * @return the string
     */
    default String obtainCookieValue(final Cookie cookie, final HttpServletRequest request) {
        return obtainCookieValue(cookie.getValue(), request);
    }

    /**
     * Obtain cookie value.
     *
     * @param cookie  the cookie
     * @param request the request
     * @return the string
     */
    String obtainCookieValue(String cookie, HttpServletRequest request);

    /**
     * Gets cookie same site policy.
     *
     * @return the cookie same site policy
     */
    CookieSameSitePolicy getCookieSameSitePolicy();

    class NoOpCookieValueManager implements CookieValueManager {
        @Serial
        private static final long serialVersionUID = 5776311151053397600L;

        @Override
        public String buildCookieValue(final String givenCookieValue, final HttpServletRequest request) {
            return givenCookieValue;
        }

        @Override
        public String obtainCookieValue(final String cookie, final HttpServletRequest request) {
            return cookie;
        }

        @Override
        public CookieSameSitePolicy getCookieSameSitePolicy() {
            return CookieSameSitePolicy.off();
        }
    }
}
