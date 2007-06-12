/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.CookieGenerator;

/**
 * Extends CookieGenerator to allow you to retrieve a value from a request.
 *  
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class CookieRetrievingCookieGenerator extends CookieGenerator {

    public String retrieveCookieValue(final HttpServletRequest request) {
        final Cookie cookie = org.springframework.web.util.WebUtils.getCookie(
            request, getCookieName());

        return cookie == null ? null : cookie.getValue();
    }
}
