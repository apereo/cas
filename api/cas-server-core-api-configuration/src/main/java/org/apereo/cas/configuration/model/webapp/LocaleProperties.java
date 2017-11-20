package org.apereo.cas.configuration.model.webapp;

import org.apereo.cas.configuration.support.RequiresModule;

import java.io.Serializable;

/**
 * Configuration properties class for locale.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-web")
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

    public String getParamName() {
        return paramName;
    }

    public void setParamName(final String paramName) {
        this.paramName = paramName;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(final String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
