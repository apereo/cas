package org.springframework.web.servlet.theme;

import org.springframework.web.util.WebUtils;
import lombok.val;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link SessionThemeResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class SessionThemeResolver extends AbstractThemeResolver {

    /**
     * Name of the session attribute that holds the theme name.
     * Only used internally by this implementation.
     * Use {@code RequestContext(Utils).getTheme()}
     * to retrieve the current theme in controllers or views.
     */
    public static final String THEME_SESSION_ATTRIBUTE_NAME = SessionThemeResolver.class.getName() + ".THEME";

    @Override
    public String resolveThemeName(final HttpServletRequest request) {
        val themeName = (String) WebUtils.getSessionAttribute(request, THEME_SESSION_ATTRIBUTE_NAME);
        return themeName != null ? themeName : getDefaultThemeName();
    }

    @Override
    public void setThemeName(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final String themeName) {
        WebUtils.setSessionAttribute(request, THEME_SESSION_ATTRIBUTE_NAME,
            StringUtils.hasText(themeName) ? themeName : null);
    }

}
