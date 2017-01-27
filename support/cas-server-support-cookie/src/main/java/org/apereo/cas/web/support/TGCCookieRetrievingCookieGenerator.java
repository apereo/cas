package org.apereo.cas.web.support;

/**
 * Generates the tgc cookie.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public class TGCCookieRetrievingCookieGenerator extends CookieRetrievingCookieGenerator {

    /**
     * Instantiates a new TGC cookie retrieving cookie generator.
     *
     * @param name                  cookie name
     * @param path                  cookie path
     * @param maxAge                cookie max age
     * @param secure                if cookie is only for HTTPS
     * @param domain                cookie domain
     * @param casCookieValueManager the cookie manager
     * @param rememberMeMaxAge      cookie rememberMe max age
     */
    public TGCCookieRetrievingCookieGenerator(final CookieValueManager casCookieValueManager, final String name,
                                              final String path, final String domain,
                                              final int rememberMeMaxAge, final boolean secure, final int maxAge) {
        super(name, path, maxAge, secure, domain, casCookieValueManager, rememberMeMaxAge);
    }
}
