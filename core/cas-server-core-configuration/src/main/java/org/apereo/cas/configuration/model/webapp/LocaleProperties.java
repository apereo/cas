package org.apereo.cas.configuration.model.webapp;

import java.io.Serializable;

/**
 * Configuration properties class for locale.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

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
