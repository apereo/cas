package org.apereo.cas.configuration.model.support.themes;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * This is {@link ThemeProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@ConfigurationProperties(prefix = "cas.themeResolver", ignoreUnknownFields = false)
public class ThemeProperties {

    private String defaultThemeName = "cas-theme-default";

    public String getDefaultThemeName() {
        return defaultThemeName;
    }

    public void setDefaultThemeName(final String defaultThemeName) {
        this.defaultThemeName = defaultThemeName;
    }
}
