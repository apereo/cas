/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.authentication.principal.RememberMeCredentials;
import org.springframework.util.StringUtils;
import org.springframework.web.util.CookieGenerator;

/**
 * Extends CookieGenerator to allow you to retrieve a value from a request.
 * <p>
 * Also has support for RememberMe Services
 *  
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class CookieRetrievingCookieGenerator extends CookieGenerator {
    
    /** The maximum age the cookie should be remembered for.
     * The default is three months (7889231 in seconds, according to Google) */
    private int rememberMeMaxAge = 7889231;
    
    public void addCookie(final HttpServletRequest request, final HttpServletResponse response, final String cookieValue) {
        
        if (!StringUtils.hasText(request.getParameter(RememberMeCredentials.REQUEST_PARAMETER_REMEMBER_ME))) {
            super.addCookie(response, cookieValue);
        } else {
            final Cookie cookie = createCookie(cookieValue);
            cookie.setMaxAge(this.rememberMeMaxAge);
            if (isCookieSecure()) {
                cookie.setSecure(true);
            }
            response.addCookie(cookie);
        }
    }

    public String retrieveCookieValue(final HttpServletRequest request) {
        final Cookie cookie = org.springframework.web.util.WebUtils.getCookie(
            request, getCookieName());

        return cookie == null ? null : cookie.getValue();
    }
    
    public void setRememberMeMaxAge(final int maxAge) {
        this.rememberMeMaxAge = maxAge;
    }
}
