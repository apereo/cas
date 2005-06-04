/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistry;
import org.springframework.web.servlet.theme.AbstractThemeResolver;

/**
 * ThemeResolver to determine the theme for CAS based on the service provided.
 * The theme resolver will extract the service parameter from the Request object
 * and attempt to match the URL provided to a Service Id. If the service is
 * found, the theme associated with it will be used. If no these is associated
 * with the service or the service was not found, a default theme will be used.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ServiceThemeResolver extends AbstractThemeResolver {

    /** Constant to define where we look for the service id in the request. */
    public static final String SERVICE_THEME_KEY = "service";

    /** The ServiceRegistry to look up the service. */
    private ServiceRegistry serviceRegistry;

    public String resolveThemeName(final HttpServletRequest request) {
        if (this.serviceRegistry == null) {
            return getDefaultThemeName();
        }

        final String serviceId = request.getParameter(SERVICE_THEME_KEY);
        final RegisteredService service = this.serviceRegistry
            .getService(serviceId);

        return service != null && service.getTheme() != null ? service
            .getTheme() : getDefaultThemeName();
    }

    public void setThemeName(final HttpServletRequest request,
        final HttpServletResponse response, final String themeName) {
        // nothing to do here
    }

    public void setServiceRegistry(final ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
}
