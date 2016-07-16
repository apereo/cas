package org.apereo.cas.services.web;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.template.TemplateLocation;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.thymeleaf.spring4.view.AbstractThymeleafView;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

/**
 * {@link RegisteredServiceThemeBasedViewResolver} is an alternate Spring View Resolver that utilizes a service's
 * associated theme to selectively choose which set of UI views will be used to generate
 * the standard views.
 *
 * @author John Gasper
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public class RegisteredServiceThemeBasedViewResolver extends ThymeleafViewResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceThemeBasedViewResolver.class);
    
    private ServicesManager servicesManager;
    private List argumentExtractors;
    private String prefix;
    private String suffix;
    
    /**
     * Instantiates a new Registered service theme based view resolver.
     */
    public RegisteredServiceThemeBasedViewResolver() {
    }
    
    @Override
    protected View loadView(final String viewName, final Locale locale) throws Exception {
        final View view = super.loadView(viewName, locale);

        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        final WebApplicationService service;

        if (requestContext != null) {
            service = WebUtils.getService(this.argumentExtractors, requestContext);
        } else {
            final HttpServletRequest request = WebUtils.getHttpServletRequestFromRequestAttributes();
            service = WebUtils.getService(this.argumentExtractors, request);
        }

        if (service == null) {
            return view;
        }

        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService != null) {
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service, registeredService);
            if (StringUtils.hasText(registeredService.getTheme()) && view instanceof AbstractThymeleafView) {
                LOGGER.debug("Attempting to locate views for service [{}] with theme [{}]",
                        registeredService.getServiceId(), registeredService.getTheme());

                final AbstractThymeleafView thymeleafView = (AbstractThymeleafView) view;
                final String viewUrl = registeredService.getTheme() + '/' + thymeleafView.getTemplateName();
                
                final String viewLocationUrl = getPrefix().concat(viewUrl).concat(getSuffix());
                LOGGER.debug("Attempting to locate view at {}", viewLocationUrl);
                final TemplateLocation location = new TemplateLocation(viewLocationUrl);
                if (location.exists(getApplicationContext())) {
                    LOGGER.debug("Found view {}", viewUrl);
                    thymeleafView.setTemplateName(viewUrl);
                } else {
                    LOGGER.debug("View {} does not exist. Fallling back to default view at {}",
                            viewLocationUrl, thymeleafView.getTemplateName());
                }

            }
        }
        return view;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setArgumentExtractors(final List argumentExtractors) {
        this.argumentExtractors = argumentExtractors;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }
}
