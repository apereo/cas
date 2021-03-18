package org.apereo.cas.configuration.model.support.pac4j;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Pac4jDelegatedAuthenticationCookieProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pac4j-webflow")
@Getter
@Setter
@Accessors(chain = true)
public class Pac4jDelegatedAuthenticationCookieProperties extends CookieProperties {
    private static final long serialVersionUID = -1460460726554772979L;

    /**
     * Decide if cookie paths should be automatically configured
     * based on the application context path, when the cookie
     * path is not configured.
     */
    private boolean autoConfigureCookiePath = true;
    
    /**
     * Determine whether cookie settings
     * should be enabled to track delgated authentication
     * choices and identity providers.
     */
    private boolean enabled;

    public Pac4jDelegatedAuthenticationCookieProperties() {
        setName("DelegatedAuthn");
    }
}
