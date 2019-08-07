package org.apereo.cas.web.support.mgmr;

import org.apereo.cas.web.cookie.CookieValueManager;

import javax.servlet.http.HttpServletRequest;

/**
 * Default cookie value builder that simply returns the given cookie value
 * and does not perform any additional checks.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class NoOpCookieValueManager implements CookieValueManager {

    /**
     * Static instance.
     */
    public static final CookieValueManager INSTANCE = new NoOpCookieValueManager();

    private static final long serialVersionUID = -8464839674747772197L;

    @Override
    public String buildCookieValue(final String givenCookieValue, final HttpServletRequest request) {
        return givenCookieValue;
    }

    @Override
    public String obtainCookieValue(final String cookie, final HttpServletRequest request) {
        return cookie;
    }
}
