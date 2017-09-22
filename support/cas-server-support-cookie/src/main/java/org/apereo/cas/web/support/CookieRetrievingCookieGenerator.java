package org.apereo.cas.web.support;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.RememberMeCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Extends CookieGenerator to allow you to retrieve a value from a request.
 * The cookie is automatically marked as httpOnly, if the servlet container has support for it.
 * Also has support for remember-me.
 *
 * @author Scott Battaglia
 * @author Misagh Moayyed
 * @since 3.1
 */
public class CookieRetrievingCookieGenerator extends CookieGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CookieRetrievingCookieGenerator.class);

    private static final int DEFAULT_REMEMBER_ME_MAX_AGE = 7889231;

    /**
     * The maximum age the cookie should be remembered for.
     * The default is three months ({@value} in seconds, according to Google)
     */
    private int rememberMeMaxAge = DEFAULT_REMEMBER_ME_MAX_AGE;

    /**
     * Responsible for manging and verifying the cookie value.
     **/
    private CookieValueManager casCookieValueManager;

    /**
     * Instantiates a new cookie retrieving cookie generator
     * with a default cipher of {@link NoOpCookieValueManager}.
     *
     * @param name     cookie name
     * @param path     cookie path
     * @param maxAge   cookie max age
     * @param secure   if cookie is only for HTTPS
     * @param domain   cookie domain
     * @param httpOnly the http only
     */
    public CookieRetrievingCookieGenerator(final String name, final String path, final int maxAge,
                                           final boolean secure, final String domain,
                                           final boolean httpOnly) {
        this(name, path, maxAge, secure, domain, new NoOpCookieValueManager(), DEFAULT_REMEMBER_ME_MAX_AGE, httpOnly);
    }

    /**
     * Instantiates a new Cookie retrieving cookie generator.
     *
     * @param name                  cookie name
     * @param path                  cookie path
     * @param maxAge                cookie max age
     * @param secure                if cookie is only for HTTPS
     * @param domain                cookie domain
     * @param casCookieValueManager the cookie manager
     * @param rememberMeMaxAge      cookie rememberMe max age
     * @param httpOnly              the http only
     */
    public CookieRetrievingCookieGenerator(final String name, final String path, final int maxAge,
                                           final boolean secure, final String domain,
                                           final CookieValueManager casCookieValueManager,
                                           final int rememberMeMaxAge,
                                           final boolean httpOnly) {
        super();
        super.setCookieName(name);
        super.setCookiePath(path);
        this.setCookieDomain(domain);
        super.setCookieMaxAge(maxAge);
        super.setCookieSecure(secure);
        super.setCookieHttpOnly(httpOnly);
        this.casCookieValueManager = casCookieValueManager;
        this.rememberMeMaxAge = rememberMeMaxAge;
    }

    /**
     * Adds the cookie, taking into account {@link RememberMeCredential#REQUEST_PARAMETER_REMEMBER_ME}
     * in the request.
     *
     * @param request     the request
     * @param response    the response
     * @param cookieValue the cookie value
     */
    public void addCookie(final HttpServletRequest request, final HttpServletResponse response, final String cookieValue) {
        final String theCookieValue = this.casCookieValueManager.buildCookieValue(cookieValue, request);

        if (StringUtils.isBlank(request.getParameter(RememberMeCredential.REQUEST_PARAMETER_REMEMBER_ME))) {
            super.addCookie(response, theCookieValue);
        } else {
            final Cookie cookie = createCookie(theCookieValue);
            cookie.setMaxAge(this.rememberMeMaxAge);
            cookie.setSecure(isCookieSecure());
            cookie.setHttpOnly(isCookieHttpOnly());
            response.addCookie(cookie);
        }
    }

    /**
     * Retrieve cookie value.
     *
     * @param request the request
     * @return the cookie value
     */
    public String retrieveCookieValue(final HttpServletRequest request) {
        try {
            final Cookie cookie = org.springframework.web.util.WebUtils.getCookie(request, getCookieName());
            return cookie == null ? null : this.casCookieValueManager.obtainCookieValue(cookie, request);
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void setCookieDomain(final String cookieDomain) {
        super.setCookieDomain(StringUtils.defaultIfEmpty(cookieDomain, null));
    }
}
