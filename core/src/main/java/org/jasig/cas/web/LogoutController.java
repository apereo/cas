/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.web.support.WebConstants;
import org.jasig.cas.web.util.WebUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.CookieGenerator;

/**
 * Controller to delete ticket granting ticket cookie in order to log out of
 * single sign on. This controller implements the idea of the ESUP Portail's
 * Logout patch to allow for redirecting to a url on logout. It also exposes a
 * log out link to the view via the WebConstants.LOGOUT constant.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class LogoutController extends AbstractController implements
    InitializingBean {

    /** The CORE to which we delegate for all CAS functionality. */
    private CentralAuthenticationService centralAuthenticationService;

    /** CookieGenerator for TGT Cookie */
    private CookieGenerator ticketGrantingTicketCookieGenerator;

    /** CookieGenerator for Warn Cookie */
    private CookieGenerator warnCookieGenerator;

    /** Logout view name. */
    private String logoutView;

    /**
     * Boolean to determine if we will redirect to any url provided in the
     * service request parameter.
     */
    private boolean followServiceRedirects;

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.centralAuthenticationService,
            "centralAuthenticationService must be set on "
                + this.getClass().getName());
        Assert.notNull(this.ticketGrantingTicketCookieGenerator, "ticketGrantingTicketCookieGenerator cannot be null");
        Assert.notNull(this.warnCookieGenerator, "warnCookieGenerator cannot be null");
        Assert.hasText(this.logoutView, "logoutView must have text.");
    }

    protected ModelAndView handleRequestInternal(
        final HttpServletRequest request, final HttpServletResponse response)
        throws Exception {
        final String ticketGrantingTicketId = WebUtils.getCookieValue(request,
            this.ticketGrantingTicketCookieGenerator.getCookieName());
        final String service = request.getParameter(WebConstants.SERVICE);

        if (ticketGrantingTicketId != null) {
            this.centralAuthenticationService
                .destroyTicketGrantingTicket(ticketGrantingTicketId);

            this.ticketGrantingTicketCookieGenerator.removeCookie(response);
            this.warnCookieGenerator.removeCookie(response);
        }

        if (this.followServiceRedirects && service != null) {
            return new ModelAndView(new RedirectView(service));
        }

        if (StringUtils.hasText(request.getParameter(WebConstants.LOGOUT))) {
            return new ModelAndView(this.logoutView, WebConstants.LOGOUT,
                request.getParameter(WebConstants.LOGOUT));
        }

        return new ModelAndView(this.logoutView);
    }

    public void setTicketGrantingTicketCookieGenerator(
        final CookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    public void setWarnCookieGenerator(final CookieGenerator warnCookieGenerator) {
        this.warnCookieGenerator = warnCookieGenerator;
    }

    /**
     * @param centralAuthenticationService The centralAuthenticationService to
     * set.
     */
    public void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public void setFollowServiceRedirects(final boolean followServiceRedirects) {
        this.followServiceRedirects = followServiceRedirects;
    }

    public void setLogoutView(final String logoutView) {
        this.logoutView = logoutView;
    }
}
