package org.apereo.cas.services.web;

import lombok.RequiredArgsConstructor;
import lombok.val;
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
@RequiredArgsConstructor
public class RequestHeaderThemeResolver extends AbstractThemeResolver {
    private final String themeHeaderName;

    @Override
    public String resolveThemeName(final HttpServletRequest request) {
        val theme = request.getHeader(this.themeHeaderName);
        return StringUtils.defaultIfBlank(theme, getDefaultThemeName());
    }

    @Override
    public void setThemeName(final HttpServletRequest httpServletRequest,
                             final HttpServletResponse httpServletResponse,
                             final String theme) {
    }
}
