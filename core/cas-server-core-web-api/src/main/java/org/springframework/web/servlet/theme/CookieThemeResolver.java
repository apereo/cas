package org.springframework.web.servlet.theme;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.util.CookieGenerator;
import org.springframework.web.util.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@link ThemeResolver} implementation that uses a cookie sent back to the user
 * in case of a custom setting, with a fallback to the default theme.
 * This is particularly useful for stateless applications without user sessions.
 *
 * <p>Custom controllers can thus override the user's theme by calling
 * {@code setThemeName}, for example, responding to a certain theme change request.
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @see #setThemeName
 */
public class CookieThemeResolver extends CookieGenerator implements ThemeResolver {

    /**
     * The default theme name used if no alternative is provided.
     */
    public static final String ORIGINAL_DEFAULT_THEME_NAME = "theme";

    /**
     * Name of the request attribute that holds the theme name. Only used
     * for overriding a cookie value if the theme has been changed in the
     * course of the current request! Use RequestContext.getTheme() to
     * retrieve the current theme in controllers or views.
     */
    public static final String THEME_REQUEST_ATTRIBUTE_NAME = CookieThemeResolver.class.getName() + ".THEME";

    /**
     * The default name of the cookie that holds the theme name.
     */
    public static final String DEFAULT_COOKIE_NAME = CookieThemeResolver.class.getName() + ".THEME";

    @Setter
    @Getter
    private String defaultThemeName = ORIGINAL_DEFAULT_THEME_NAME;


    public CookieThemeResolver() {
        setCookieName(DEFAULT_COOKIE_NAME);
    }

    @Override
    public String resolveThemeName(final HttpServletRequest request) {
        var themeName = (String) request.getAttribute(THEME_REQUEST_ATTRIBUTE_NAME);
        if (themeName != null) {
            return themeName;
        }

        val cookieName = getCookieName();
        if (cookieName != null) {
            val cookie = WebUtils.getCookie(request, cookieName);
            if (cookie != null) {
                val value = cookie.getValue();
                if (StringUtils.hasText(value)) {
                    themeName = value;
                }
            }
        }

        if (themeName == null) {
            themeName = getDefaultThemeName();
        }
        request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, themeName);
        return themeName;
    }

    @Override
    public void setThemeName(
        final HttpServletRequest request,
        @Nullable
        final HttpServletResponse response,
        @Nullable
        final String themeName) {

        if (StringUtils.hasText(themeName)) {
            request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, themeName);
            addCookie(response, themeName);
        } else {
            request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, getDefaultThemeName());
            removeCookie(response);
        }
    }

}

