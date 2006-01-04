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
    
    private static final boolean DEFAULT_SECURE_VALUE = true; 
    
    private String cookieValue;
    
    private boolean cookieSecure = DEFAULT_SECURE_VALUE;

    protected Cookie createCookie(final String cookieValue) {
        final Cookie cookie = super.createCookie(cookieValue);
        cookie.setSecure(this.cookieSecure);
        return cookie;
    }
    
    public void addCookie(final HttpServletResponse response) {
        addCookie(response, this.cookieValue == null ? "" : this.cookieValue);
    }
    
    public String getCookieValue(final HttpServletRequest request) {
        final Cookie cookie = WebUtils.getCookie(
            request, getCookieName());
        return cookie == null ? null : cookie.getValue();
    }

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
