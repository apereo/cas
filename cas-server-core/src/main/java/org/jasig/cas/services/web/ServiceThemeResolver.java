/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.services.web;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.theme.AbstractThemeResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Pattern;

/**
 * ThemeResolver to determine the theme for CAS based on the service provided.
 * The theme resolver will extract the service parameter from the Request object
 * and attempt to match the URL provided to a Service Id. If the service is
 * found, the theme associated with it will be used. If not, these is associated
 * with the service or the service was not found, a default theme will be used.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class ServiceThemeResolver extends AbstractThemeResolver {

    /** The ServiceRegistry to look up the service. */
    private ServicesManager servicesManager;

    private List<ArgumentExtractor> argumentExtractors;

    private List<Pattern> overrides = new ArrayList<Pattern>();

    public String resolveThemeName(final HttpServletRequest request) {
        if (this.servicesManager == null) {
            return getDefaultThemeName();
        }

        final Service service = WebUtils.getService(this.argumentExtractors, request);

        final RegisteredService rService = this.servicesManager.findServiceBy(service);

        // retrieve the user agent string from the request
        String userAgent = request.getHeader("User-Agent");

        for (final Pattern pattern : this.overrides) {
            if (pattern.matcher(userAgent).matches()) {
                request.setAttribute("isMobile", "true");
                break;
            }
        }

        return service != null && rService != null && StringUtils.hasText(rService.getTheme()) ? rService.getTheme() : getDefaultThemeName();
    }

    public void setThemeName(final HttpServletRequest request, final HttpServletResponse response, final String themeName) {
        // nothing to do here
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setArgumentExtractors(final List<ArgumentExtractor> argumentExtractors) {
        this.argumentExtractors = argumentExtractors;
    }

    /**
     * Sets the map of mobile browsers.  This sets a flag on the request called "isMobile".
     * <p>
     * Themes that understand isMobile should provide an alternative stylesheet.
     *
     * @param mobileOverrides the list of mobile browsers.
     */
    public void setMobileBrowsers(final List<String> mobileOverrides) {
        // initialize the overrides variable to an empty map
        this.overrides = new ArrayList<Pattern>();

        for (final String mobileOverride : mobileOverrides) {

            this.overrides.add(Pattern.compile(mobileOverride));
        }
    }
}
