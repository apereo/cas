package org.apereo.cas.web.support;

import org.apereo.cas.configuration.model.core.web.LocaleProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * This is {@link CasLocaleChangeInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class CasLocaleChangeInterceptor extends LocaleChangeInterceptor {
    /**
     * The Locale properties.
     */
    protected final LocaleProperties localeProperties;

    /**
     * The Argument extractor.
     */
    protected final ArgumentExtractor argumentExtractor;

    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    @Setter
    private List<String> supportedFlows = new ArrayList<>();

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
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) throws ServletException {
        val requestUrl = request.getRequestURL().toString();
        if (localeProperties.isForceDefaultLocale()) {
            val locale = new Locale(localeProperties.getDefaultValue());
            configureLocale(request, response, locale);
            return true;
        }
        val service = this.argumentExtractor.extractService(request);
        if (service != null) {
            val registeredService = servicesManager.findServiceBy(service);
            if (registeredService != null && StringUtils.isNotBlank(registeredService.getLocale())) {
                val locale = new Locale(SpringExpressionLanguageValueResolver.getInstance().resolve(registeredService.getLocale()));
                configureLocale(request, response, locale);
            }
        }

        val newLocale = request.getParameter(getParamName());
        if (newLocale != null) {
            val locale = new Locale(newLocale);
            configureLocale(request, response, locale);
        }

        if (request.getLocale() != null && isLocaleConfigured(request)) {
            val match = supportedFlows.stream().anyMatch(flowId -> requestUrl.contains('/' + flowId));
            if (match) {
                configureLocale(request, response, request.getLocale());
            }
        }
        return true;
    }

    private static boolean isLocaleConfigured(final HttpServletRequest request) {
        return request.getAttribute(Locale.class.getName()) == null;
    }
}
