package org.springframework.web.servlet.theme;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link ThemeChangeInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@Slf4j
public class ThemeChangeInterceptor implements HandlerInterceptor {
    /**
     * Default parameter name to use for theme change requests.
     */
    public static final String DEFAULT_PARAM_NAME = "theme";
    
    @Getter
    @Setter
    private String paramName = "theme";

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) throws ServletException {

        val newTheme = request.getParameter(this.paramName);
        if (newTheme != null) {
            val themeResolver = RequestContextUtils.getThemeResolver(request);
            if (themeResolver != null) {
                LOGGER.trace("Changing theme to [{}]", newTheme);
                themeResolver.setThemeName(request, response, newTheme);
            }
        }
        return true;
    }

}
