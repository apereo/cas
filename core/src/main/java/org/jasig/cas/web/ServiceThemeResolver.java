/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.services.AuthenticatedService;
import org.jasig.cas.services.ServiceRegistry;
import org.springframework.web.servlet.theme.AbstractThemeResolver;

/**
 * ThemeResolver to determine the theme for CAS based on the service provided.
 * If the Service is not found, the ThemeResolver will return the default theme.
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
        final AuthenticatedService service = this.serviceRegistry
            .getService(serviceId);

        return service != null && service.getTheme() != null ? service
            .getTheme() : getDefaultThemeName();
    }

    public void setThemeName(final HttpServletRequest request,
        final HttpServletResponse response, final String themeName) {
        // nothing to do here
    }
}
