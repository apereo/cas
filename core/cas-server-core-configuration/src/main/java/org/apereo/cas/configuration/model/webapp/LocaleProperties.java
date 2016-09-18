package org.apereo.cas.configuration.model.webapp;

/**
 * Configuration properties class for locale.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */

public class LocaleProperties {

    private String paramName = "locale";

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
