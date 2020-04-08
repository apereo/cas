package org.apereo.cas.configuration.model.core.web;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * Configuration properties class for locale.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class LocaleProperties implements Serializable {

    private static final long serialVersionUID = -1644471820900213781L;

    /**
     * Parameter name to use when switching locales.
     */
    private String paramName = "locale";

    /**
     * Default locale.
     */
    private String defaultValue = "en";

    /**
     * Control the properties of the cookie created to hold language changes.
     */
    private LocaleCookieProperties cookie = new LocaleCookieProperties();
    
    @RequiresModule(name = "cas-server-core-web", automated = true)
    @Getter
    @Setter
    @Accessors(chain = true)
    public static class LocaleCookieProperties extends CookieProperties implements Serializable {
        private static final long serialVersionUID = 158577966798914031L;
    }
}
