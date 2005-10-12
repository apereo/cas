/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Utilities class for web related utility functions.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class WebUtils {

    private WebUtils() {
        // private constructor so we can't instanciate the class.
    }

    /**
     * Method to retrieve the requested Cookie value or null if the cookie does
     * not exist.
     * 
     * @param request the HttpServletRequest to grab the cookie from.
     * @param id the id of the cookie.
     * @return the cookie value or null if the cookie does not exist.
     */
    public static String getCookieValue(final HttpServletRequest request,
        final String id) {
        final Cookie cookie = org.springframework.web.util.WebUtils.getCookie(
            request, id);
        return cookie == null ? null : cookie.getValue();
    }
}
