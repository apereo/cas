package org.apereo.cas.web.view;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;

import lombok.experimental.UtilityClass;
import lombok.val;

/**
 * This is {@link ThemeUtils}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@UtilityClass
public class ThemeUtils {
    /**
     * Gets current theme.
     *
     * @param casProperties the cas properties
     * @return the current theme
     */
    public static String getCurrentTheme(final CasConfigurationProperties casProperties) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext();
        if (request != null) {
            val session = request.getSession(false);
            val paramName = casProperties.getTheme().getParamName();
            if (session != null) {
                val theme = (String) session.getAttribute(paramName);
                if (theme != null) {
                    return theme;
                }
            }
            val theme = (String) request.getAttribute(paramName);
            if (theme != null) {
                return theme;
            }
        }
        return casProperties.getTheme().getDefaultThemeName();
    }
}
