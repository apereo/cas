package org.apereo.cas.services.web;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.springframework.web.servlet.theme.AbstractThemeResolver;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private final ServicesManager servicesManager;

    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    private final ResourceLoader resourceLoader;
    /**
     * This sets a flag on the request called "isMobile" and also
     * provides the custom flag called browserType which can be mapped into the theme.
     * <p>
     * Themes that understand isMobile should provide an alternative stylesheet.
     */
    private final Map<Pattern, String> overrides;

    public ServiceThemeResolver(final ServicesManager servicesManager,
                                final Map<String, String> mobileOverrides,
                                final AuthenticationServiceSelectionPlan serviceSelectionStrategies,
                                final ResourceLoader resourceLoader) {
        super();
        this.servicesManager = servicesManager;
        this.authenticationRequestServiceSelectionStrategies = serviceSelectionStrategies;
        this.resourceLoader = resourceLoader;
        this.overrides = mobileOverrides.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> Pattern.compile(entry.getKey()), Map.Entry::getValue));
    }

    @Override
    public String resolveThemeName(final HttpServletRequest request) {
        if (this.servicesManager == null) {
            return getDefaultThemeName();
        }

        final String userAgent = HttpRequestUtils.getHttpServletRequestUserAgent(request);

        if (StringUtils.isBlank(userAgent)) {
            return getDefaultThemeName();
        }

        overrides.entrySet()
                .stream()
                .filter(entry -> entry.getKey().matcher(userAgent).matches())
                .findFirst()
                .ifPresent(entry -> {
                    request.setAttribute("isMobile", Boolean.TRUE.toString());
                    request.setAttribute("browserType", entry.getValue());
                });

        final RequestContext context = RequestContextHolder.getRequestContext();
        final Service serviceContext = WebUtils.getService(context);
        final Service service = this.authenticationRequestServiceSelectionStrategies.resolveService(serviceContext);
        if (service == null) {
            LOGGER.debug("No service is found in the request context. Falling back to the default theme [{}]", getDefaultThemeName());
            return getDefaultThemeName();
        }

        final RegisteredService rService = this.servicesManager.findServiceBy(service);
        if (rService == null || !rService.getAccessStrategy().isServiceAccessAllowed()) {
            LOGGER.warn("No registered service is found to match [{}] or service access is disallowed. Using default theme [{}]",
                    service, getDefaultThemeName());
            return getDefaultThemeName();
        }
        if (StringUtils.isBlank(rService.getTheme())) {
            LOGGER.debug("No theme name is specified for service [{}]. Using default theme [{}]", rService, getDefaultThemeName());
            return getDefaultThemeName();
        }

        return determineThemeNameToChoose(request, service, rService);

    }

    /**
     * Determine theme name to choose.
     *
     * @param request  the request
     * @param service  the service
     * @param rService the r service
     * @return the string
     */
    protected String determineThemeNameToChoose(final HttpServletRequest request,
                                                final Service service,
                                                final RegisteredService rService) {
        try {
            LOGGER.debug("Service [{}] is configured to use a custom theme [{}]", rService, rService.getTheme());
            
            final Resource resource = ResourceUtils.getRawResourceFrom(rService.getTheme());
            if (resource instanceof FileSystemResource && resource.exists()) {
                LOGGER.debug("Executing groovy script to determine theme for [{}]", service.getId());
                final String result = ScriptingUtils.executeGroovyScript(resource, new Object[]{service, rService,
                        request.getQueryString(), HttpRequestUtils.getRequestHeaders(request), LOGGER}, String.class);
                return StringUtils.defaultIfBlank(result, getDefaultThemeName());
            }
            if (resource instanceof UrlResource) {
                final String url = resource.getURL().toExternalForm();
                LOGGER.debug("Executing URL [{}] to determine theme for [{}]", url, service.getId());
                final HttpResponse response = HttpUtils.executeGet(url, CollectionUtils.wrap("service", service.getId()));
                if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    final String result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    return StringUtils.defaultIfBlank(result, getDefaultThemeName());
                }
            }
            
            final CasThemeResourceBundleMessageSource messageSource = new CasThemeResourceBundleMessageSource();
            messageSource.setBasename(rService.getTheme());
            if (messageSource.doGetBundle(rService.getTheme(), request.getLocale()) != null) {
                LOGGER.debug("Found custom theme [{}] for service [{}]", rService.getTheme(), rService);
                return rService.getTheme();
            }
            LOGGER.warn("Custom theme [{}] for service [{}] cannot be located. Falling back to default theme...", rService.getTheme(), rService);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return getDefaultThemeName();
    }

    @Override
    public void setThemeName(final HttpServletRequest request, final HttpServletResponse response, final String themeName) {
    }

    /**
     * An extension of the default where the exceptions are simply logged
     * so CAS can fall back onto default themes.
     */
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
