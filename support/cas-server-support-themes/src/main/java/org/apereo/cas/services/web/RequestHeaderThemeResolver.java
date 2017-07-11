package org.apereo.cas.services.web;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.theme.AbstractThemeResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link RequestHeaderThemeResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class RequestHeaderThemeResolver extends AbstractThemeResolver {
    @Override
    public String resolveThemeName(final HttpServletRequest request) {
        final String theme = request.getHeader("theme");
        return StringUtils.defaultIfBlank(theme, getDefaultThemeName());
    }

    @Override
    public void setThemeName(final HttpServletRequest httpServletRequest,
                             final HttpServletResponse httpServletResponse,
                             final String theme) {
    }
}
