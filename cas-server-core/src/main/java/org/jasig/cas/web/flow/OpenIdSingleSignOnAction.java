/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.OpenIdCredentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.util.annotation.NotNull;
import org.jasig.cas.web.support.DefaultOpenIdUserNameExtractor;
import org.jasig.cas.web.support.OpenIdUserNameExtractor;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Attempts to utilize an existing single sign on session, but only if the
 * Principal of the existing session matches the new Principal. Note that care
 * should be taken when using credentials that are automatically provided and
 * not entered by the user.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public class OpenIdSingleSignOnAction extends AbstractAction {

    @NotNull
    private CookieGenerator ticketGrantingTicketCookieGenerator;

    @NotNull
    private CentralAuthenticationService centralAuthenticationService;

    @NotNull
    private OpenIdUserNameExtractor extractor = new DefaultOpenIdUserNameExtractor();

    protected Event doExecute(final RequestContext requestContext)
        throws Exception {
        final HttpServletRequest request = WebUtils
            .getHttpServletRequest(requestContext);
        
        final String ticketGrantingTicketId = WebUtils.getCookieValue(request,
            this.ticketGrantingTicketCookieGenerator.getCookieName());
        final String userName = this.extractor
            .extractLocalUsernameFromUri(request
                .getParameter("openid.identity"));
        final Service service = WebUtils.getService(requestContext);

        requestContext.getFlowScope().put("openIdLocalId", userName);
        
        if (ticketGrantingTicketId == null) {
            return error();
        }

        if (service == null || userName == null) {
            return error();
        }

        final OpenIdCredentials credentials = new OpenIdCredentials(
            ticketGrantingTicketId, userName);

        try {
            final String serviceTicketId = this.centralAuthenticationService
                .grantServiceTicket(ticketGrantingTicketId, service,
                    credentials);
            WebUtils.putServiceTicketInRequestScope(requestContext,
                serviceTicketId);
        } catch (final TicketException e) {
            return error();
        }

        return success();
    }

    public void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    public void setTicketGrantingTicketCookieGenerator(
        final CookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }
}
