/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.util;

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
    
    public static String getRequestParameterAsString(final HttpServletRequest request, final String parameter) {
        return request.getParameter(parameter);
    }
    
    public static boolean getRequestParameterAsBoolean(final HttpServletRequest request, final String parameter) {
        return request.getParameter(parameter) != null;
    }
}
