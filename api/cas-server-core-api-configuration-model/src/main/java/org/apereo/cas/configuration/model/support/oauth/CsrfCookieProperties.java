package org.apereo.cas.configuration.model.support.oauth;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Properties for the Cross-Site Request Forgery (CSRF) cookie used in some Oauth flows.
 *
 * @author Hal Deadman
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-cookie", automated = true)
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("CsrfCookieProperties")
public class CsrfCookieProperties extends CookieProperties {

    private static final long serialVersionUID = 5298598088218873282L;

    /**
     * Setting default values to the pac4j defaults.
     * Pac4j uses an Integer for {@code maxAge} and the default is null.
     * CAS avoids setting the {@code maxAge} unless it is set to greater than -1.
     * The name of the cookie is not configurable.
     */
    public CsrfCookieProperties() {
        setSecure(false);
        setHttpOnly(false);
    }
}
