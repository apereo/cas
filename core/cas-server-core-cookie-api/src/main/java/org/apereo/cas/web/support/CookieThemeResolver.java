package org.apereo.cas.web.support;

import org.apereo.cas.configuration.model.support.cookie.CookieProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.util.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * {@link ThemeResolver} implementation that uses a cookie sent back to the user
 * in case of a custom setting, with a fallback to the default theme.
 * This is particularly useful for stateless applications without user sessions.
 *
 * <p>Custom controllers can thus override the user's theme by calling
 * {@link #setThemeName}, for example, responding to a certain theme change request.
 *
 * @author Jean-Pierre Pawlak
 * @author Juergen Hoeller
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
public class CookieThemeResolver implements ThemeResolver {

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

    private final CookieProperties cookieProperties;

    @Override
    public String resolveThemeName(final HttpServletRequest request) {
        var themeName = (String) request.getAttribute(THEME_REQUEST_ATTRIBUTE_NAME);
        if (StringUtils.isNotBlank(themeName)) {
            return themeName;
        }

        val cookie = WebUtils.getCookie(request, DEFAULT_COOKIE_NAME);
        if (cookie != null) {
            val value = cookie.getValue();
            if (StringUtils.isNotBlank(value)) {
                themeName = value;
            }
        }
        if (StringUtils.isBlank(themeName)) {
            themeName = getDefaultThemeName();
        }
        request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, themeName);
        return themeName;
    }

    @Override
    public void setThemeName(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final String themeName) {

        Objects.requireNonNull(cookieProperties, "CookieProperties must not be null");
        if (StringUtils.isNotBlank(themeName)) {
            request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, themeName);
            val cookie = CookieUtils.createSetCookieHeader(themeName,
                cookieProperties.withName(DEFAULT_COOKIE_NAME));
            response.addHeader(HttpHeaders.SET_COOKIE, cookie);
        } else {
            request.setAttribute(THEME_REQUEST_ATTRIBUTE_NAME, getDefaultThemeName());
            val cookie = CookieUtils.createSetCookieHeader(StringUtils.EMPTY,
                cookieProperties.withName(DEFAULT_COOKIE_NAME).withMaxAge("0"));
            response.addHeader(HttpHeaders.SET_COOKIE, cookie);
        }
    }
}

