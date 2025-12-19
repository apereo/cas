package org.apereo.cas.web.theme;

import module java.base;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link AbstractThemeResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public abstract class AbstractThemeResolver implements ThemeResolver {

    /**
     * Out-of-the-box value for the default theme name: "theme".
     */
    public static final String ORIGINAL_DEFAULT_THEME_NAME = "theme";

    @Getter
    @Setter
    private String defaultThemeName = ORIGINAL_DEFAULT_THEME_NAME;

}
