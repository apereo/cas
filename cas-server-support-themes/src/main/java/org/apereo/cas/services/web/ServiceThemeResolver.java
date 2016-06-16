package org.apereo.cas.services.web;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.theme.AbstractThemeResolver;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * ThemeResolver to determine the theme for CAS based on the service provided.
 * The theme resolver will extract the service parameter from the Request object
 * and attempt to match the URL provided to a Service Id. If the service is
 * found, the theme associated with it will be used. If not, these is associated
 * with the service or the service was not found, a default theme will be used.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class ServiceThemeResolver extends AbstractThemeResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceThemeResolver.class);

    private ServicesManager servicesManager;

    private Map<Pattern, String> overrides = new HashMap<>();

    @Override
    public String resolveThemeName(final HttpServletRequest request) {
        if (this.servicesManager == null) {
            return getDefaultThemeName();
        }
        // retrieve the user agent string from the request
        final String userAgent = WebUtils.getHttpServletRequestUserAgent(request);

        if (StringUtils.isBlank(userAgent)) {
            return getDefaultThemeName();
        }

        for (final Map.Entry<Pattern, String> entry : this.overrides.entrySet()) {
            if (entry.getKey().matcher(userAgent).matches()) {
                request.setAttribute("isMobile", "true");
                request.setAttribute("browserType", entry.getValue());
                break;
            }
        }

        final RequestContext context = RequestContextHolder.getRequestContext();
        final Service service = WebUtils.getService(context);
        if (service != null) {
            final RegisteredService rService = this.servicesManager.findServiceBy(service);
            if (rService != null && rService.getAccessStrategy().isServiceAccessAllowed()
                    && StringUtils.isNotBlank(rService.getTheme())) {
                LOGGER.debug("Service [{}] is configured to use a custom theme [{}]", rService, rService.getTheme());
                final CasThemeResourceBundleMessageSource messageSource = new CasThemeResourceBundleMessageSource();
                messageSource.setBasename(rService.getTheme());
                if (messageSource.doGetBundle(rService.getTheme(), request.getLocale()) != null) {
                    LOGGER.debug("Found custom theme [{}] for service [{}]", rService.getTheme(), rService);
                    return rService.getTheme();
                } else {
                    LOGGER.warn("Custom theme {} for service {} cannot be located. Falling back to default theme...",
                            rService.getTheme(), rService);
                }
            }
        }
        return getDefaultThemeName();
    }

    @Override
    public void setThemeName(final HttpServletRequest request, final HttpServletResponse response, final String themeName) {
        // nothing to do here
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
    public void setDefaultThemeName(final String defaultThemeName) {
        super.setDefaultThemeName(defaultThemeName);
    }

    /**
     * Sets the map of mobile browsers.  This sets a flag on the request called "isMobile" and also
     * provides the custom flag called browserType which can be mapped into the theme.
     * <p>
     * Themes that understand isMobile should provide an alternative stylesheet.
     *
     * @param mobileOverrides the list of mobile browsers.
     */
    @Autowired
    @Qualifier("serviceThemeResolverSupportedBrowsers")
    public void setMobileBrowsers(final Map mobileOverrides) {
        // initialize the overrides variable to an empty map
        this.overrides = new HashMap<>();

        mobileOverrides.entrySet().forEach(e -> {
            final Map.Entry<String, String> entry = (Map.Entry<String, String>) e;
            this.overrides.put(Pattern.compile(entry.getKey()), entry.getValue());
        });
    }

    private static class CasThemeResourceBundleMessageSource extends ResourceBundleMessageSource {
        @Override
        protected ResourceBundle doGetBundle(final String basename, final Locale locale) {
            try {
                final ResourceBundle bundle = ResourceBundle.getBundle(basename, locale, getBundleClassLoader());
                if (bundle != null && !bundle.keySet().isEmpty()) {
                    return bundle;
                }
            } catch (final Exception e) {
                LOGGER.debug(e.getMessage(), e);
            }
            return null;
        }
    }
}
