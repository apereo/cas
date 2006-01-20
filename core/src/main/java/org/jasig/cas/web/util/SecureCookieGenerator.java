/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.util.CookieGenerator;
import org.springframework.web.util.WebUtils;

/**
 * Modified version of CookieGenerator to override protected
 * <code>createCookie</code> method to ensure that a cookie is marked as
 * secure.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class SecureCookieGenerator extends CookieGenerator {

    /** Default value for the secure flag. */
    private static final boolean DEFAULT_SECURE_VALUE = true;

    /** The cookie value to store (versus a different one for each request) */
    private String cookieValue;

    /** Flag of whether the cookie should be marked as secure or not. */
    private boolean cookieSecure = DEFAULT_SECURE_VALUE;

    protected Cookie createCookie(final String cookieValue) {
        final Cookie cookie = super.createCookie(cookieValue);
        cookie.setSecure(this.cookieSecure);
        return cookie;
    }

    public void addCookie(final HttpServletResponse response) {
        addCookie(response, this.cookieValue == null ? "" : this.cookieValue);
    }

    /**
     * Retrieve the cookie value from the request.
     * 
     * @param request the HttpServletRequest to retrieve the cookie value from.
     * @return the cookie value or null if the cookie does not exist.
     */
    public String getCookieValue(final HttpServletRequest request) {
        final Cookie cookie = WebUtils.getCookie(request, getCookieName());
        return cookie == null ? null : cookie.getValue();
    }

    /**
     * The cookie value we wish all cookies to have.
     * 
     * @return the cookie value.
     */
    public String getCookieValue() {
        return this.cookieValue;
    }

    public void setCookieValue(final String cookieValue) {
        this.cookieValue = cookieValue;
    }

    public void setCookieSecure(final boolean cookieSecure) {
        this.cookieSecure = cookieSecure;
    }
}
