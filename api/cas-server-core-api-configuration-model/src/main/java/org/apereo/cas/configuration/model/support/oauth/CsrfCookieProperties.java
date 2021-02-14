package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;

/**
 * Setting default values to the pac4j defaults.
 * The pac4j library does not allow for customizing the name or the pinToSession.
 * Pac4j uses an Integer for maxAge and the default is null. CAS avoids setting the maxAge unless it is set to greater
 * than -1.
 * @since 6.4.0
 */
public class CsrfCookieProperties extends CookieProperties {

    public CsrfCookieProperties() {
        setSecure(false);
        setHttpOnly(false);
        setSameSitePolicy(null);
        setPath(null);
        setDomain(null);
    }
}
