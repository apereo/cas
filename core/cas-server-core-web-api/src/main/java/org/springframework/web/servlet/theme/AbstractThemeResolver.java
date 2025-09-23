package org.springframework.web.servlet.theme;

import org.springframework.web.servlet.ThemeResolver;
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
