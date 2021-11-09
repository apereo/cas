package org.apereo.cas.oidc.web;

import org.apereo.cas.configuration.model.core.web.LocaleProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.ArgumentExtractor;
import org.apereo.cas.web.support.CasLocaleChangeInterceptor;

import lombok.val;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * This is {@link OidcLocaleChangeInterceptor}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class OidcLocaleChangeInterceptor extends CasLocaleChangeInterceptor {
    public OidcLocaleChangeInterceptor(final LocaleProperties localeProperties,
                                       final ArgumentExtractor argumentExtractor,
                                       final ServicesManager servicesManager) {
        super(localeProperties, argumentExtractor, servicesManager);
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final Object handler) {
        resolveUiLocale(request, response);
        return true;
    }

    private void resolveUiLocale(final HttpServletRequest request,
                                 final HttpServletResponse response) {
        val service = argumentExtractor.extractService(request);
        if (service != null) {
            val newLocale = service.getAttributes().get(OidcConstants.UI_LOCALES);
            if (newLocale != null && !newLocale.isEmpty()) {
                configureLocale(request, response, new Locale((String) newLocale.get(0)));
            }
        }
    }
}
