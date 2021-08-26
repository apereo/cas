package org.apereo.cas.configuration.model.core.web;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

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
@JsonFilter("LocaleProperties")
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
     * When set to true, locale resolution via request parameters
     * and such is ignored and the locale default value is always enforced.
     */
    private boolean forceDefaultLocale;

    /**
     * Control the properties of the cookie created to hold language changes.
     */
    @NestedConfigurationProperty
    private LocaleCookieProperties cookie = new LocaleCookieProperties();

}
