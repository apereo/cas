/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.web.support.ViewNames;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;

/**
 * Controller to delete ticket granting ticket cookie in order to log out of
 * single sign on. This controller implements the idea of the ESUP Portail's
 * Logout patch to allow for redirecting to a url on logout.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class LogoutController extends AbstractController implements
    InitializingBean {

    /** The log instance. */
    private final Log log = LogFactory.getLog(getClass());

    /** The CORE to which we delegate for all CAS functionality. */
    private CentralAuthenticationService centralAuthenticationService;

    public void afterPropertiesSet() throws Exception {
        if (this.centralAuthenticationService == null) {
            throw new IllegalStateException(
                "centralAuthenticationService must be set on "
                    + this.getClass().getName());
        }
    }

    protected ModelAndView handleRequestInternal(
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        Cookie cookie = WebUtils.getCookie(request, WebConstants.COOKIE_TGC_ID);
        String service = request.getParameter(WebConstants.SERVICE);

        if (cookie != null) {
            this.centralAuthenticationService
                .destroyTicketGrantingTicket(cookie.getValue());
            destroyTicketGrantingTicketCookie(request, response);
        }

        if (service != null) {
            return new ModelAndView(new RedirectView(service));
        }

        return new ModelAndView(ViewNames.CONST_LOGOUT);
    }

    /**
     * Method to destroy the cookie for the TicketGrantingTicket
     * @param request The HttpServletRequest
     * @param response The HttpServletResponse
     */
    private void destroyTicketGrantingTicketCookie(
        final HttpServletRequest request, final HttpServletResponse response) {
        log.debug("Destroying TicketGrantingTicket cookie.");
        Cookie cookie = new Cookie(WebConstants.COOKIE_TGC_ID, "");
        cookie.setMaxAge(0);
        cookie.setPath(request.getContextPath());
        cookie.setSecure(true);
        response.addCookie(cookie);
    }

    /**
     * @param centralAuthenticationService The centralAuthenticationService to
     * set.
     */
    public void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }
}
