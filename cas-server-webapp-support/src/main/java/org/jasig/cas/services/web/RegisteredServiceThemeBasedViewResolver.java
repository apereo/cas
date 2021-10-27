/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.services.web;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

/**
 * {@link RegisteredServiceThemeBasedViewResolver} is an alternate Spring View Resolver that utilizes a service's
 * associated theme to selectively choose which set of UI views will be used to generate
 * the standard views (casLoginView.jsp, casLogoutView.jsp etc).
 *
 * <p>Views associated with a particular theme by default are expected to be found at:
 * {@link #DEFAULT_PATH_PREFIX}/<code>themeId/ui</code>. A starting point may be to
 * clone the default set of view pages into a new directory based on the theme id.</p>
 *
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
public final class RegisteredServiceThemeBasedViewResolver extends InternalResourceViewResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegisteredServiceThemeBasedViewResolver.class);
    private static final String DEFAULT_PATH_PREFIX = "/WEB-INF/view/jsp";

    /** The ServiceRegistry to look up the service. */
    private final ServicesManager servicesManager;

    private final String defaultThemeId;

    private String pathPrefix = DEFAULT_PATH_PREFIX;

    /**
     * The {@link RegisteredServiceThemeBasedViewResolver} constructor.
     * @param defaultThemeId the theme to apply if the service doesn't specific one or a service is not provided
     * @param servicesManager the serviceManager implementation
     * @see #setCache(boolean)
     */
    public RegisteredServiceThemeBasedViewResolver(final String defaultThemeId, final ServicesManager servicesManager) {
        super();
        super.setCache(false);

        this.defaultThemeId = defaultThemeId;
        this.servicesManager = servicesManager;
    }

    /**
     * Uses the viewName and the theme associated with the service.
     * being requested and returns the appropriate view.
     * @param viewName the name of the view to be resolved
     * @return a theme-based UrlBasedView
     * @throws Exception an exception
     */
    @Override
    protected AbstractUrlBasedView buildView(final String viewName) throws Exception {
        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        final WebApplicationService service = WebUtils.getService(requestContext);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        final String themeId = service != null && registeredService != null
                && registeredService.getAccessStrategy().isServiceAccessAllowed()
                && StringUtils.hasText(registeredService.getTheme()) ? registeredService.getTheme() : defaultThemeId;

        final String themePrefix = String.format("%s/%s/ui/", pathPrefix, themeId);
        LOGGER.debug("Prefix {} set for service {} with theme {}", themePrefix, service, themeId);

        //Build up the view like the base classes do, but we need to forcefully set the prefix for each request.
        //From UrlBasedViewResolver.buildView
        final InternalResourceView view = (InternalResourceView) BeanUtils.instantiateClass(getViewClass());
        view.setUrl(themePrefix + viewName + getSuffix());
        final String contentType = getContentType();
        if (contentType != null) {
            view.setContentType(contentType);
        }
        view.setRequestContextAttribute(getRequestContextAttribute());
        view.setAttributesMap(getAttributesMap());

        //From InternalResourceViewResolver.buildView
        view.setAlwaysInclude(false);
        view.setExposeContextBeansAsAttributes(false);
        view.setPreventDispatchLoop(true);

        LOGGER.debug("View resolved: {}", view.getUrl());

        return view;
    }

    /**
     * setCache is not supported in the {@link RegisteredServiceThemeBasedViewResolver} because each
     * request must be independently evaluated.
     * @param cache a value indicating whether the view should cache results.
     */
    @Override
    public void setCache(final boolean cache) {
        LOGGER.warn("The {} does not support caching. Turned off caching forcefully.", this.getClass().getSimpleName());
        super.setCache(false);
    }

    /**
     * Sets path prefix. This is the location where
     * views are expected to be found. The default
     * prefix is {@link #DEFAULT_PATH_PREFIX}.
     *
     * @param pathPrefix the path prefix
     */
    public void setPathPrefix(final String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }
}
