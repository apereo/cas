package org.apereo.cas.services.web;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.WebBasedRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.scripting.ScriptingUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.theme.AbstractThemeResolver;
import org.springframework.webflow.execution.RequestContextHolder;

import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

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
    private final ObjectProvider<ServicesManager> servicesManager;

    private final ObjectProvider<AuthenticationServiceSelectionPlan> authenticationRequestServiceSelectionStrategies;

    private final ObjectProvider<CasConfigurationProperties> casProperties;

    @Nonnull
    @Override
    public String resolveThemeName(
        @Nonnull
        final HttpServletRequest request) {
        val context = RequestContextHolder.getRequestContext();
        val serviceContext = WebUtils.getService(context);
        val service = authenticationRequestServiceSelectionStrategies.getObject().resolveService(serviceContext);
        if (service == null) {
            LOGGER.trace("No service is found in the request context. Falling back to the default theme [{}]", getDefaultThemeName());
            return rememberThemeName(request);
        }

        val rService = (WebBasedRegisteredService) servicesManager.getObject().findServiceBy(service);
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
    public void setThemeName(
        @Nonnull
        final HttpServletRequest request, final HttpServletResponse response, final String themeName) {
    }

    protected String determineThemeNameToChoose(final HttpServletRequest request,
                                                final Service service,
                                                final WebBasedRegisteredService rService) {
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
                val exec = HttpUtils.HttpExecutionRequest.builder()
                    .parameters(CollectionUtils.wrap("service", service.getId()))
                    .url(url)
                    .method(HttpMethod.GET)
                    .build();
                response = HttpUtils.execute(exec);
                if (response != null && response.getCode() == HttpStatus.SC_OK) {
                    val result = IOUtils.toString(((HttpEntityContainer) response).getEntity().getContent(), StandardCharsets.UTF_8);
                    return StringUtils.defaultIfBlank(result, getDefaultThemeName());
                }
            }
            val theme = resolveThemeForService(rService, request);
            if (theme != null) {
                LOGGER.trace("Found custom theme [{}] for service [{}]", theme, rService);
                return theme;
            }
            LOGGER.warn("Custom theme [{}] for service [{}] cannot be located. Falling back to default theme...",
                rService.getTheme(), rService.getName());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return getDefaultThemeName();
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
        val messageSource = new CasThemeResourceBundleMessageSource();
        val theme = SpringExpressionLanguageValueResolver.getInstance().resolve(registeredService.getTheme());
        messageSource.setBasename(theme);

        if (casProperties.getObject().getView().getTemplatePrefixes()
            .stream()
            .map(prefix -> StringUtils.appendIfMissing(prefix, "/").concat(theme).concat(".properties"))
            .anyMatch(ResourceUtils::doesResourceExist)) {
            LOGGER.trace("Found custom external theme [{}] for service [{}]", theme, registeredService.getName());
            return theme;
        }

        if (messageSource.doGetBundle(theme, request.getLocale()) != null) {
            LOGGER.trace("Found custom theme [{}] for service [{}]", theme, registeredService.getName());
            return theme;
        }
        
        LOGGER.warn("Theme [{}] for service [{}] cannot be located", theme, registeredService.getName());
        return null;
    }

}
