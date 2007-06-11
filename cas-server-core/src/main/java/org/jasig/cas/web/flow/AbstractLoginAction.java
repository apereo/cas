/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.util.annotation.NotNull;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.RequestContext;

/**
 * Abstract class to make available common collaborators such as CookieGenerators and
 * argumentExtractors.
 * 
 * @author Scott
 * @version $Revision$ $Date$
 * @since 3.1
 */
public abstract class AbstractLoginAction extends AbstractAction {

    /** Instance of CentralAuthenticationService. */
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;
    
    @NotNull
    private CookieGenerator ticketGrantingTicketCookieGenerator;
    
    public final void setTicketGrantingTicketCookieGenerator(final CookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator= ticketGrantingTicketCookieGenerator;
    }
    
    protected final CookieGenerator getTicketGrantingTicketCookieGenerator() {
        return this.ticketGrantingTicketCookieGenerator;
    }

    protected final CentralAuthenticationService getCentralAuthenticationService() {
        return this.centralAuthenticationService;
    }

    public final void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    protected final boolean isGatewayPresent(final RequestContext context) {
        return StringUtils.hasText(context.getExternalContext()
            .getRequestParameterMap().get("gateway"));
    }
    
    protected final boolean isRenewPresent(final RequestContext context) {
        return StringUtils.hasText(context.getRequestParameters().get("renew"));
    }

    protected final String extractTicketGrantingTicketFromCookie(
        final RequestContext context) {
        final HttpServletRequest request = WebUtils
            .getHttpServletRequest(context);
        return WebUtils.getCookieValue(request,
            this.ticketGrantingTicketCookieGenerator.getCookieName());
    }
}
