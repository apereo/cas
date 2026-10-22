package org.apereo.cas.web.support;

import module java.base;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.support.RequestContextUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

    /**
     * Extract the service from the request to look up its locale. A service the argument extractor refuses
     * carries no locale; the refusal is left to the flow that handles the request, which answers it with the
     * service error view. Throwing here would turn into an error response that cannot render a page.
     *
     * @param request the request
     * @return the service, or null when the request has none or it is refused
     */
    private @Nullable WebApplicationService extractService(final HttpServletRequest request) {
        try {
            return argumentExtractor.getObject().extractService(request);
        } catch (final UnauthorizedServiceException e) {
            return null;
        }
    }

    private static boolean isLocaleConfigured(final HttpServletRequest request) {
        return request.getAttribute(Locale.class.getName()) == null;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             @NonNull final HttpServletResponse response,
                             @NonNull final Object handler) {
        val requestUrl = request.getRequestURL().toString();
        if (casProperties.getObject().getLocale().isForceDefaultLocale()) {
            val locale = Locale.forLanguageTag(casProperties.getObject().getLocale().getDefaultValue());
            configureLocale(request, response, locale);
            return true;
        }
        val service = extractService(request);
        if (service != null) {
            val registeredService = servicesManager.getObject().findServiceBy(service);
            if (registeredService instanceof final WebBasedRegisteredService webRegisteredService
                && StringUtils.isNotBlank(webRegisteredService.getLocale())) {
                val locale = Locale.forLanguageTag(SpringExpressionLanguageValueResolver.getInstance()
                    .resolve(webRegisteredService.getLocale()));
                configureLocale(request, response, locale);
            }
        }

        var newLocale = request.getParameter(getParamName());
        if (newLocale != null) {
            newLocale = newLocale.replace('_', '-');
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
