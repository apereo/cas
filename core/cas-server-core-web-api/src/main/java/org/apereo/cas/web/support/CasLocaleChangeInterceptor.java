package org.apereo.cas.web.support;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.annotation.Nonnull;
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
    protected final ObjectProvider<CasConfigurationProperties> casProperties;

    protected final ObjectProvider<ArgumentExtractor> argumentExtractor;

    protected final ObjectProvider<ServicesManager> servicesManager;

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

    private static boolean isLocaleConfigured(final HttpServletRequest request) {
        return request.getAttribute(Locale.class.getName()) == null;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             @Nonnull
                             final HttpServletResponse response,
                             @Nonnull
                             final Object handler) throws ServletException {
        val requestUrl = request.getRequestURL().toString();
        if (casProperties.getObject().getLocale().isForceDefaultLocale()) {
            val locale = Locale.forLanguageTag(casProperties.getObject().getLocale().getDefaultValue());
            configureLocale(request, response, locale);
            return true;
        }
        val service = argumentExtractor.getObject().extractService(request);
        if (service != null) {
            val registeredService = servicesManager.getObject().findServiceBy(service);
            if (registeredService instanceof WebBasedRegisteredService) {
                val webRegisteredService = (WebBasedRegisteredService) registeredService;
                if (StringUtils.isNotBlank(webRegisteredService.getLocale())) {
                    val locale = Locale.forLanguageTag(SpringExpressionLanguageValueResolver.getInstance()
                        .resolve(webRegisteredService.getLocale()));
                    configureLocale(request, response, locale);
                }
            }
        }

        val newLocale = request.getParameter(getParamName());
        if (newLocale != null) {
            val locale = Locale.forLanguageTag(newLocale);
            configureLocale(request, response, locale);
        }

        if (request.getLocale() != null && isLocaleConfigured(request)) {
            val match = supportedFlows.stream().anyMatch(flowId -> requestUrl.contains('/' + flowId));
            if (match) {
                val locale = RequestContextUtils.getLocale(request);
                configureLocale(request, response, locale);
            }
        }
        return true;
    }
}
