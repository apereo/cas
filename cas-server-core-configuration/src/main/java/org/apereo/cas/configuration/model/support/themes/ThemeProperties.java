package org.apereo.cas.configuration.model.support.themes;

/**
 * This is {@link ThemeProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

public class ThemeProperties {

    private String defaultThemeName = "cas-theme-default";
    private String paramName = "theme";

    public String getParamName() {
        return paramName;
    }

    public void setParamName(final String paramName) {
        this.paramName = paramName;
    }

    public String getDefaultThemeName() {
        return defaultThemeName;
    }

    public void setDefaultThemeName(final String defaultThemeName) {
        this.defaultThemeName = defaultThemeName;
    }
}
