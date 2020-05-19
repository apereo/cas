package org.apereo.cas.services.web;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.servlet.theme.AbstractThemeResolver;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;
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
@Slf4j
@RequiredArgsConstructor
public class RegisteredServiceThemeResolver extends AbstractThemeResolver {
    private final ServicesManager servicesManager;

    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    private final CasConfigurationProperties casProperties;

    /**
     * This sets a flag on the request called "isMobile" and also
     * provides the custom flag called browserType which can be mapped into the theme.
     * <p>
     * Themes that understand isMobile should provide an alternative stylesheet.
     */
    private final Map<Pattern, String> overrides;

    @Override
    public String resolveThemeName(final HttpServletRequest request) {
        if (this.servicesManager == null) {
            return rememberThemeName(request);
        }

        val userAgent = HttpRequestUtils.getHttpServletRequestUserAgent(request);

        if (StringUtils.isBlank(userAgent)) {
            return rememberThemeName(request);
        }

        overrides.entrySet()
            .stream()
            .filter(entry -> entry.getKey().matcher(userAgent).matches())
            .findFirst()
            .ifPresent(entry -> {
                request.setAttribute("isMobile", Boolean.TRUE.toString());
                request.setAttribute("browserType", entry.getValue());
            });

        val context = RequestContextHolder.getRequestContext();
        val serviceContext = WebUtils.getService(context);
        val service = this.authenticationRequestServiceSelectionStrategies.resolveService(serviceContext);
        if (service == null) {
            LOGGER.trace("No service is found in the request context. Falling back to the default theme [{}]", getDefaultThemeName());
            return rememberThemeName(request);
        }

        val rService = this.servicesManager.findServiceBy(service);
        if (rService == null || !rService.getAccessStrategy().isServiceAccessAllowed()) {
            LOGGER.warn("No registered service is found to match [{}] or access is denied. Using default theme [{}]", service, getDefaultThemeName());
            return rememberThemeName(request);
        }
        if (StringUtils.isBlank(rService.getTheme())) {
            LOGGER.trace("No theme name is specified for service [{}]. Using default theme [{}]", rService, getDefaultThemeName());
            return rememberThemeName(request);
        }

        val themeName = determineThemeNameToChoose(request, service, rService);
        return rememberThemeName(request, themeName);
    }

    @Override
    public void setThemeName(final HttpServletRequest request, final HttpServletResponse response, final String themeName) {
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
        HttpResponse response = null;
        try {
            LOGGER.debug("Service [{}] is configured to use a custom theme [{}]", rService, rService.getTheme());

            val resource = ResourceUtils.getRawResourceFrom(rService.getTheme());
            if (resource instanceof FileSystemResource && resource.exists()) {
                LOGGER.debug("Executing groovy script to determine theme for [{}]", service.getId());
                val result = ScriptingUtils.executeGroovyScript(resource, new Object[]{service, rService,
                    request.getQueryString(), HttpRequestUtils.getRequestHeaders(request), LOGGER}, String.class, true);
                return StringUtils.defaultIfBlank(result, getDefaultThemeName());
            }
            if (resource instanceof UrlResource) {
                val url = resource.getURL().toExternalForm();
                LOGGER.debug("Executing URL [{}] to determine theme for [{}]", url, service.getId());
                response = HttpUtils.executeGet(url, CollectionUtils.wrap("service", service.getId()));
                if (response != null && response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    val result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
                    return StringUtils.defaultIfBlank(result, getDefaultThemeName());
                }
            }

            val messageSource = new CasThemeResourceBundleMessageSource();
            messageSource.setBasename(rService.getTheme());
            if (messageSource.doGetBundle(rService.getTheme(), request.getLocale()) != null) {
                LOGGER.trace("Found custom theme [{}] for service [{}]", rService.getTheme(), rService);
                return rService.getTheme();
            }
            LOGGER.warn("Custom theme [{}] for service [{}] cannot be located. Falling back to default theme...", rService.getTheme(), rService);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        } finally {
            HttpUtils.close(response);
        }
        return getDefaultThemeName();
    }

    /**
     * Remember/save the theme in the request.
     *
     * @param request the HTTP request
     * @return the remembered theme
     */
    protected String rememberThemeName(final HttpServletRequest request) {
        return rememberThemeName(request, getDefaultThemeName());
    }

    /**
     * Remember/save the theme in the request.
     *
     * @param request   the HTTP request
     * @param themeName the theme to remember
     * @return the remembered theme
     */
    protected String rememberThemeName(final HttpServletRequest request, final String themeName) {
        val attributeName = casProperties.getTheme().getParamName();
        LOGGER.trace("Storing theme [{}] as a request attribute under [{}]", themeName, attributeName);
        request.setAttribute(attributeName, themeName);
        return themeName;
    }

    /**
     * An extension of the default where the exceptions are simply logged
     * so CAS can fall back onto default themes.
     */
    private static class CasThemeResourceBundleMessageSource extends ResourceBundleMessageSource {
        @Override
        protected ResourceBundle doGetBundle(final String basename, final Locale locale) {
            try {
                val bundle = ResourceBundle.getBundle(basename, locale, getBundleClassLoader());
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
