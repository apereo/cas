package org.jasig.cas.services.web;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractCachingViewResolver;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import java.util.Locale;

/**
 * {@link RegisteredServiceThemeBasedViewResolver} is an alternate Spring View Resolver that utilizes a service's
 * associated theme to selectively choose which set of UI views will be used to generate
 * the standard views (casLoginView.jsp, casLogoutView.jsp etc).
 * <p>Views associated with a particular theme by default are expected to be found at:
 * {@link #getPrefix()}/{@code themeId/ui}. A starting point may be to
 * clone the default set of view pages into a new directory based on the theme id.</p>
 * <p>Note: There also exists a {@link org.jasig.cas.services.web.ServiceThemeResolver}
 * that attempts to resolve the view name based on the service theme id. The difference
 * however is that {@link org.jasig.cas.services.web.ServiceThemeResolver} only decorates
 * the default view pages with additional tags and coloring, such as CSS and JS. The
 * component presented here on the other hand has the ability to load an entirely new
 * set of pages that are may be totally different from that of the default's. This
 * is specially useful in cases where the set of pages for a theme that are targetted
 * for a different type of audience are entirely different structurally that simply
 * using the {@link org.jasig.cas.services.web.ServiceThemeResolver} is not practical
 * to augment the default views. In such cases, new view pages may be required.</p>
 *
 * @author John Gasper
 * @author Misagh Moayyed
 * @since 4.1.0
 */
public final class RegisteredServiceThemeBasedViewResolver extends AbstractCachingViewResolver implements Ordered {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceThemeBasedViewResolver.class);
    private static final String THEME_LOCATION_PATTERN = "%s/%s/ui/";

    @Autowired
    private ResourceLoader resourceLoader;
    
    private final ServicesManager servicesManager;
    private String prefix;
    private String suffix;
    private int order;
    
    /**
     * The {@link RegisteredServiceThemeBasedViewResolver} constructor.
     *
     * @param servicesManager the serviceManager implementation
     * @see #setCache(boolean)
     */
    public RegisteredServiceThemeBasedViewResolver(final ServicesManager servicesManager) {
        super();
        super.setCache(false);

        this.servicesManager = servicesManager;
    }
    
    @Override
    protected View loadView(final String viewName, final Locale locale) throws Exception {
        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        final WebApplicationService service = WebUtils.getService(requestContext);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        if (service != null && registeredService != null
                && registeredService.getAccessStrategy().isServiceAccessAllowed()
                && StringUtils.isNotBlank(registeredService.getTheme())) {

            final InternalResourceView view = BeanUtils.instantiateClass(InternalResourceView.class);

            LOGGER.debug("Attempting to locate views for service [{}] with theme [{}]",
                    registeredService.getServiceId(), registeredService.getTheme());

            final String themePrefix = String.format(THEME_LOCATION_PATTERN, getPrefix(), registeredService.getTheme());
            LOGGER.debug("Prefix [{}] set for service [{}] with theme [{}]", themePrefix, service,
                    registeredService.getTheme());
            final String viewUrl = StringUtils.replace(themePrefix + viewName + getSuffix(), "//", "/");
            final Resource resource = this.resourceLoader.getResource(viewUrl);
            
            if (resource.exists()) {
                view.setUrl(viewUrl);
                view.setAlwaysInclude(false);
                view.setExposeContextBeansAsAttributes(false);
                view.setPreventDispatchLoop(true);
                LOGGER.debug("View resolved: {}", view.getUrl());

                return view;
            }
        }
        return null;
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

    @Override
    public int getOrder() {
        return order;
    }

    public void setOrder(final int order) {
        this.order = order;
    }
}
