package org.apereo.cas.web.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ThemeResolver;
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
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) throws ServletException {

        val newTheme = request.getParameter(this.paramName);
        if (newTheme != null) {
            themeResolver.setThemeName(request, response, newTheme);
        }
        return true;
    }

}
