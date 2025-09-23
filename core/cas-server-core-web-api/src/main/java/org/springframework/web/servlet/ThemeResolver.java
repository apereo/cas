package org.springframework.web.servlet;

import org.springframework.lang.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link ThemeResolver}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@FunctionalInterface
public interface ThemeResolver {
    /**
     * Resolve the current theme name via the given request.
     * Should return a default theme as fallback in any case.
     *
     * @param request the request to be used for resolution
     * @return the current theme name
     */
    String resolveThemeName(
        @Nullable HttpServletRequest request);

    /**
     * Set the current theme name to the given one.
     *
     * @param request   the request to be used for theme name modification
     * @param response  the response to be used for theme name modification
     * @param themeName the new theme name ({@code null} or empty to reset it)
     * @throws UnsupportedOperationException if the ThemeResolver implementation
     *                                       does not support dynamic changing of the theme
     */
    default void setThemeName(
        final HttpServletRequest request,
        final HttpServletResponse response,
        final String themeName) {
    }
}
