/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.bind.BindUtils;
import org.springframework.web.flow.RequestContext;

/**
 * Webflow Action that allows for the non-interactive check for a credential.  
 * Specific uses may include the checking for Client Certificates.  Developers
 * need only supply a Credentials class and a custom CredentialsBinder.  The class
 * will them bind the Credentials using both Spring binding and the custom CredentialsBinder.
 * It will attempt to obtain a TicketGrantingTicket and a ServiceTicket.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class NonInteractiveLoginAction extends AbstractCasAction
    implements InitializingBean {

    private static final String DEFAULT_DOMAIN_OBJECT_NAME = "credentials";

    private Class domainObjectClass;

    private String domainObjectName;

    private CredentialsBinder credentialsBinder;

    private CentralAuthenticationService centralAuthenticationService;

    protected ModelAndEvent doExecuteInternal(
        final RequestContext requestContext, final Map attributes)
        throws Exception {

        if (ContextUtils.getHttpServletRequest(requestContext).getParameter(
            WebConstants.SERVICE) == null) {
            return new ModelAndEvent(error());
        }

        final Credentials credentials = (Credentials) createFormObject(requestContext);

        bind(requestContext, credentials);

        try {
            final String ticketGrantingTicketId = obtainTicketGrantingTicket(
                requestContext, credentials);
            final String serviceTicketId = obtainServiceTicket(requestContext,
                credentials, ticketGrantingTicketId);

            Map model = new HashMap();

            model.put(WebConstants.SERVICE, ContextUtils.getHttpServletRequest(
                requestContext).getParameter(WebConstants.SERVICE));
            model.put(WebConstants.TICKET, serviceTicketId);

            return new ModelAndEvent(success(), model);

        } catch (TicketException e) {
            return new ModelAndEvent(error());
        }
    }

    protected Object createFormObject(RequestContext context)
        throws InstantiationException, IllegalAccessException {
        return this.domainObjectClass.newInstance();
    }

    protected void bind(final RequestContext requestContext,
        Credentials credentials) {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(requestContext);
        BindUtils.bind(request, credentials, this.domainObjectName);

        if (this.credentialsBinder != null) {
            this.credentialsBinder.bind(request, credentials);
        }
    }

    protected String obtainTicketGrantingTicket(
        final RequestContext requestContext, final Credentials credentials)
        throws TicketException {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(requestContext);
        String ticketGrantingTicketId = WebUtils.getCookieValue(request,
            WebConstants.COOKIE_TGC_ID);

        if (ticketGrantingTicketId != null) {
            return ticketGrantingTicketId;
        }

        return this.centralAuthenticationService
            .createTicketGrantingTicket(credentials);
    }

    protected String obtainServiceTicket(final RequestContext requestContext,
        final Credentials credentials, final String ticketGrantingTicketId)
        throws TicketException {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(requestContext);
        final HttpServletResponse response = ContextUtils
            .getHttpServletResponse(requestContext);
        final boolean renew = Boolean.valueOf(
            request.getParameter(WebConstants.RENEW)).booleanValue();
        final String service = request.getParameter(WebConstants.SERVICE);
        String serviceTicketId = null;

        if (renew) {
            try {
                serviceTicketId = this.centralAuthenticationService
                    .grantServiceTicket(ticketGrantingTicketId,
                        new SimpleService(service), credentials);
            } catch (TicketException e) {
                // do nothing
            }
        }

        if (serviceTicketId == null) {
            String newTicketGrantingTicketId = this.centralAuthenticationService
                .createTicketGrantingTicket(credentials);

            serviceTicketId = this.centralAuthenticationService
                .grantServiceTicket(ticketGrantingTicketId, new SimpleService(
                    service));

            createCookie(WebConstants.COOKIE_TGC_ID, newTicketGrantingTicketId,
                request, response);
        }

        return serviceTicketId;
    }

    /**
     * Method to create a cookie and put it in the response.
     * 
     * @param id The id to name the cookie.
     * @param value The value to give the cookie.
     * @param request The HttpServletRequest
     * @param response TheHttpServletResponse to store the cookie.
     */
    private void createCookie(final String id, final String value,
        final HttpServletRequest request, final HttpServletResponse response) {
        final Cookie cookie = new Cookie(id, value);
        cookie.setSecure(true);
        cookie.setMaxAge(-1);
        cookie.setPath(request.getContextPath());
        response.addCookie(cookie);
    }

    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        final String name = this.getClass().getName();

        Assert.notNull(this.domainObjectClass,
            "Domain Object Class must be set on " + name);
        Assert.notNull(this.centralAuthenticationService,
            "CentralAuthenticationService must be set on " + name);

        if (this.domainObjectName == null) {
            this.domainObjectName = DEFAULT_DOMAIN_OBJECT_NAME;
        }
    }
}
