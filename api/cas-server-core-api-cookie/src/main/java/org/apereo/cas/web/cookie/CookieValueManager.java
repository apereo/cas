package org.apereo.cas.web.cookie;

import org.apereo.cas.multitenancy.TenantExtractor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

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

    /**
     * Gets tenant extractor.
     *
     * @return the tenant extractor
     */
    TenantExtractor getTenantExtractor();
}
