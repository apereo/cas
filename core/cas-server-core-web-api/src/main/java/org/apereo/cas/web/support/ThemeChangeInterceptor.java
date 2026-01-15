package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.web.theme.ThemeResolver;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link ThemeChangeInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@RequiredArgsConstructor
@Getter
public class ThemeChangeInterceptor implements HandlerInterceptor {
    /**
     * Default parameter name to use for theme change requests.
     */
    public static final String DEFAULT_PARAM_NAME = "theme";

    private final ThemeResolver themeResolver;
    private final String paramName;

    @Override
    public boolean preHandle(final HttpServletRequest request, final @NonNull HttpServletResponse response,
                             final @NonNull Object handler) throws ServletException {

        val newTheme = request.getParameter(this.paramName);
        if (newTheme != null) {
            themeResolver.setThemeName(request, response, newTheme);
        }
        return true;
    }

}
