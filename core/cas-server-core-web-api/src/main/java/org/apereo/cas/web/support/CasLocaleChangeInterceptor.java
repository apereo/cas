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

    /**
     * Configure locale.
     *
     * @param request  the request
     * @param response the response
     * @param locale   the locale
     */
    protected static void configureLocale(final HttpServletRequest request,
                                          final HttpServletResponse response,
                                          final Locale locale) {
        val localeResolver = RequestContextUtils.getLocaleResolver(request);
        if (localeResolver != null) {
            localeResolver.setLocale(request, response, locale);
            request.setAttribute(Locale.class.getName(), locale);
        }
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response,
                             final Object handler) throws ServletException {
        return handleForcedLocale(request, response) || super.preHandle(request, response, handler);
    }

    /**
     * Handle forced locale.
     *
     * @param request  the request
     * @param response the response
     * @return the boolean
     */
    protected boolean handleForcedLocale(final HttpServletRequest request, final HttpServletResponse response) {
        if (localeProperties.isForceDefaultLocale()) {
            val locale = new Locale(localeProperties.getDefaultValue());
            configureLocale(request, response, locale);
        }
        return localeProperties.isForceDefaultLocale();
    }
}
