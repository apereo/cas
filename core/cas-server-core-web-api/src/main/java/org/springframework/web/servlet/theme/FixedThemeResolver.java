package org.springframework.web.servlet.theme;

import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link FixedThemeResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class FixedThemeResolver extends AbstractThemeResolver {
    @Override
    public String resolveThemeName(final HttpServletRequest request) {
        return getDefaultThemeName();
    }
}
