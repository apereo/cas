/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Utilities class for web related utility functions.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class WebUtils {

    private WebUtils() {
        // private constructor so we can't instanciate the class.
    }

    /**
     * Retrieve a parameter from the request as a String.
     * 
     * @param request the HttpServletRequest object to get the parameter from.
     * @param parameter the name of the parameter.
     * @return the String version of the paramater.
     */
    public static String getRequestParameterAsString(
        final HttpServletRequest request, final String parameter) {
        return request.getParameter(parameter);
    }

    /**
     * Retrieve the parameter from the request as a boolean.
     * 
     * @param request the HttpServletRequest object to get the parameter from.
     * @param parameter the name of the parameter.
     * @return the parameter value as a boolean
     */
    public static boolean getRequestParameterAsBoolean(
        final HttpServletRequest request, final String parameter) {
        return request.getParameter(parameter) != null;
    }
    
    public static String getCookieValue(final HttpServletRequest request, final String cookieName) {
        final Cookie cookie = org.springframework.web.util.WebUtils.getCookie(request, cookieName);
        return cookie == null ? null : cookie.getValue();
    }
}
