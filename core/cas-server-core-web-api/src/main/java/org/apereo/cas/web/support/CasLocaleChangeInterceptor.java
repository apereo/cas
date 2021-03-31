package org.apereo.cas.web.support;

import org.apereo.cas.configuration.model.core.web.LocaleProperties;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * This is {@link CasLocaleChangeInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class CasLocaleChangeInterceptor extends LocaleChangeInterceptor {
    private final LocaleProperties localeProperties;

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) throws ServletException {
        if (localeProperties.isForceDefaultLocale()) {
            val localeResolver = RequestContextUtils.getLocaleResolver(request);
            if (localeResolver != null) {
                val locale = new Locale(localeProperties.getDefaultValue());
                localeResolver.setLocale(request, response, locale);
            }
        }
        return localeProperties.isForceDefaultLocale() || super.preHandle(request, response, handler);
    }
}
