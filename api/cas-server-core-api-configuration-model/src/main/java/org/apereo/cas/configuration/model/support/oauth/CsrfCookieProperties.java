package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;

/**
 * Properties for the Cross-Site Request Forgery (CSRF) cookie used in some Oauth flows.
 *
 * @author Hal Deadman
 * @since 6.4.0
 */
public class CsrfCookieProperties extends CookieProperties {

    /**
     * Setting default values to the pac4j defaults.
     * Pac4j uses an Integer for maxAge and the default is null.
     * CAS avoids setting the maxAge unless it is set to greater than -1.
     * The name of the cookie is not configurable.
     */
    public CsrfCookieProperties() {
        setSecure(false);
        setHttpOnly(false);
        setSameSitePolicy(null);
        setPath(null);
        setDomain(null);
    }
}
