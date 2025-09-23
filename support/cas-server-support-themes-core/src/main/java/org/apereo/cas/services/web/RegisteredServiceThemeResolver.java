package org.apereo.cas.services.web;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.theme.AbstractThemeResolver;
import org.springframework.webflow.execution.RequestContextHolder;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * {@link org.springframework.web.servlet.ThemeResolver} to determine the theme for CAS based on the service provided.
 * The theme resolver will extract the service parameter from the Request object
 * and attempt to match the URL provided to a Service Id. If the service is
 * found and there is no theme specified or access to the service is not allowed,
 * the default theme will be used.
 * If the service does have a theme associated with it but the theme is a path to a
 * {@link FileSystemResource} system resource that exists, it will be executed as a
 * Groovy script that should return the name of the theme.
 * If the theme value is an HTTP {@link UrlResource}, then the contents referenced by the URL
 * will be read and the response used as the theme name.
 * Blank values returned from the script or the URL will result in the default theme being used.
 * If the theme attribute in the service is not a file or URL resource, it will be evaluated as
 * a Spring expression and then a search for property files with the theme name as the base name
 * is done using configured template prefix locations.
 * If nothing is found in template prefix locations, the {@link java.util.ResourceBundle#getBundle(String, Locale)} method
 * using the locale of the request is used to look for the theme properties to ensure it exists.
 * If theme properties don't exist for the specified theme name then the default theme will be used.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class RegisteredServiceThemeResolver extends AbstractThemeResolver {
    protected final ObjectProvider<ServicesManager> servicesManager;

    protected final ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    protected final ObjectProvider<CasConfigurationProperties> casProperties;

    @Nonnull
    @Override
    public String resolveThemeName(final HttpServletRequest request) {
        val context = RequestContextHolder.getRequestContext();
        val serviceContext = WebUtils.getService(context);
        val service = FunctionUtils.doUnchecked(() -> authenticationRequestServiceSelectionStrategies.getObject().resolveService(serviceContext));
        if (service == null) {
            LOGGER.trace("No service is found in the request context. Falling back to the default theme [{}]", getDefaultThemeName());
            return rememberThemeName(request);
        }

        val registeredService = (WebBasedRegisteredService) servicesManager.getObject().findServiceBy(service);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed(registeredService, service)) {
            LOGGER.warn("No registered service is found to match [{}] or access is denied. Using default theme [{}]", service, getDefaultThemeName());
            return rememberThemeName(request);
        }
        if (StringUtils.isBlank(registeredService.getTheme())) {
            LOGGER.trace("No theme name is specified for service [{}]. Using default theme [{}]", registeredService, getDefaultThemeName());
            return rememberThemeName(request);
        }

        val themeName = determineThemeNameToChoose(request, service, registeredService);
        return rememberThemeName(request, themeName);
    }
    
    protected String determineThemeNameToChoose(final HttpServletRequest request,
                                                final Service service,
                                                final WebBasedRegisteredService registeredService) {
        HttpResponse response = null;
        try {
            LOGGER.debug("Service [{}] is configured to use a custom theme [{}]", registeredService, registeredService.getTheme());

            val resource = ResourceUtils.getRawResourceFrom(registeredService.getTheme());
            if (resource.isFile() && resource.exists()) {
                val result = determineThemeFromGroovyResource(request, service, registeredService, resource);
                return StringUtils.defaultIfBlank(result, getDefaultThemeName());
            }
            if (resource instanceof UrlResource) {
                val url = resource.getURL().toExternalForm();
                LOGGER.debug("Executing URL [{}] to determine theme for [{}]", url, service.getId());
                val exec = HttpExecutionRequest.builder()
                    .parameters(CollectionUtils.wrap("service", service.getId()))
                    .url(url)
                    .method(HttpMethod.GET)
                    .build();
                response = HttpUtils.execute(exec);
                if (response != null && response.getCode() == HttpStatus.SC_OK) {
                    try (val content = ((HttpEntityContainer) response).getEntity().getContent()) {
                        val result = IOUtils.toString(content, StandardCharsets.UTF_8);
                        return StringUtils.defaultIfBlank(result, getDefaultThemeName());
                    }
                }
            }
            val theme = resolveThemeForService(registeredService, request);
            if (theme != null) {
                LOGGER.trace("Found custom theme [{}] for service [{}]", theme, registeredService);
                return theme;
            }
            LOGGER.warn("Custom theme [{}] for service [{}] cannot be located. Falling back to default theme...",
                registeredService.getTheme(), registeredService.getName());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return getDefaultThemeName();
    }

    protected String determineThemeFromGroovyResource(final HttpServletRequest request,
                                                      final Service service,
                                                      final WebBasedRegisteredService registeredService,
                                                      final AbstractResource resource) {
        LOGGER.debug("Executing groovy script to determine theme for [{}]", service.getId());
        return ApplicationContextProvider.getScriptResourceCacheManager()
            .map(cacheManager -> FunctionUtils.doUnchecked(() -> {
                val filePath = resource.getFile().getCanonicalPath();
                val script = cacheManager.resolveScriptableResource(resource.getFile().toURI().toString(), filePath);
                val args = CollectionUtils.<String, Object>wrap(
                    "service", service,
                    "registeredService", registeredService,
                    "queryString", StringUtils.defaultString(request.getQueryString()),
                    "headers", HttpRequestUtils.getRequestHeaders(request),
                    "logger", LOGGER);
                script.setBinding(args);
                return script.execute(args.values().toArray(), String.class);
            })).orElse(StringUtils.EMPTY);
    }

    protected String rememberThemeName(final HttpServletRequest request) {
        return rememberThemeName(request, getDefaultThemeName());
    }

    protected String rememberThemeName(final HttpServletRequest request, final String themeName) {
        val attributeName = casProperties.getObject().getTheme().getParamName();
        LOGGER.trace("Storing theme [{}] as a request attribute under [{}]", themeName, attributeName);
        request.setAttribute(attributeName, themeName);
        return themeName;
    }

    protected String resolveThemeForService(final WebBasedRegisteredService registeredService,
                                            final HttpServletRequest request) {
        val theme = SpringExpressionLanguageValueResolver.getInstance().resolve(registeredService.getTheme());
        if (casProperties.getObject().getView().getTemplatePrefixes()
            .stream()
            .map(prefix -> Strings.CI.appendIfMissing(prefix, "/").concat(theme).concat(".properties"))
            .anyMatch(ResourceUtils::doesResourceExist)) {
            LOGGER.trace("Found custom external theme [{}] for service [{}]", theme, registeredService.getName());
            return theme;
        }
        val messageSource = new CasThemeResourceBundleMessageSource();
        messageSource.setBasename(theme);
        if (messageSource.doGetBundle(theme, request.getLocale()) != null) {
            LOGGER.trace("Found custom theme [{}] for service [{}]", theme, registeredService.getName());
            return theme;
        }

        LOGGER.warn("Theme [{}] for service [{}] cannot be located", theme, registeredService.getName());
        return null;
    }

}
