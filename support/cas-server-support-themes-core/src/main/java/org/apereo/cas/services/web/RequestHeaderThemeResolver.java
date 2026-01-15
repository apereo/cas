package org.apereo.cas.services.web;

import module java.base;
import org.apereo.cas.web.theme.AbstractThemeResolver;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link RequestHeaderThemeResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
public class RequestHeaderThemeResolver extends AbstractThemeResolver {
    private final String themeHeaderName;

    @NonNull
    @Override
    public String resolveThemeName(final HttpServletRequest request) {
        val theme = request.getHeader(this.themeHeaderName);
        return StringUtils.defaultIfBlank(theme, getDefaultThemeName());
    }
}
