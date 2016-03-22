package org.jasig.cas.services.web;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAccessStrategySupport;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.View;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.spring4.view.AbstractThymeleafView;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
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
@RefreshScope
@Component("registeredServiceViewResolver")
public class RegisteredServiceThemeBasedViewResolver extends ThymeleafViewResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceThemeBasedViewResolver.class);

    @Autowired
    private ThymeleafProperties properties;

    @Autowired
    @Qualifier("thymeleafViewResolver")
    private ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Resource(name = "argumentExtractors")
    private List argumentExtractors;
    
    /**
     * Instantiates a new Registered service theme based view resolver.
     */
    public RegisteredServiceThemeBasedViewResolver() {
    }
    
    /**
     * The {@link RegisteredServiceThemeBasedViewResolver} constructor.
     *
     * @param servicesManager the serviceManager implementation
     */
    public RegisteredServiceThemeBasedViewResolver(final ServicesManager servicesManager) {
        super();
        this.servicesManager = servicesManager;
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        setApplicationContext(this.thymeleafViewResolver.getApplicationContext());
        setCache(this.properties.isCache());
        if (!isCache()) {
            setCacheLimit(0);
        }
        setCacheUnresolved(this.thymeleafViewResolver.isCacheUnresolved());
        setCharacterEncoding(this.thymeleafViewResolver.getCharacterEncoding());
        setContentType(this.thymeleafViewResolver.getContentType());
        setExcludedViewNames(this.thymeleafViewResolver.getExcludedViewNames());
        setOrder(this.thymeleafViewResolver.getOrder());
        setRedirectContextRelative(this.thymeleafViewResolver.isRedirectContextRelative());
        setRedirectHttp10Compatible(this.thymeleafViewResolver.isRedirectHttp10Compatible());
        setStaticVariables(this.thymeleafViewResolver.getStaticVariables());

        final SpringTemplateEngine engine = this.thymeleafViewResolver.getTemplateEngine();
        setTemplateEngine(engine);
        setViewNames(this.thymeleafViewResolver.getViewNames());
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
            RegisteredServiceAccessStrategySupport.ensureServiceAccessIsAllowed(service, registeredService);
            if (StringUtils.hasText(registeredService.getTheme())  && view instanceof AbstractThymeleafView) {
                LOGGER.debug("Attempting to locate views for service [{}] with theme [{}]",
                        registeredService.getServiceId(), registeredService.getTheme());

                final AbstractThymeleafView thymeleafView = (AbstractThymeleafView) view;
                final String viewUrl = registeredService.getTheme() + '/' + thymeleafView.getTemplateName();
                thymeleafView.setTemplateName(viewUrl);
            }
        }
        return view;
    }
}
