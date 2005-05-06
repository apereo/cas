/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class WebUtils {
    
    public static String getCookieValue(HttpServletRequest request, String id) {
        final Cookie cookie = org.springframework.web.util.WebUtils.getCookie(request, id); 
        return cookie == null ? null : cookie.getValue();
    }
}
