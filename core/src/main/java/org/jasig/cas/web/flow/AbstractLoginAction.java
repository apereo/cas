/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.AbstractAction;

/**
 * @author Scott
 * @version $Revision$ $Date$
 * @since 3.0.6
 */
public abstract class AbstractLoginAction extends AbstractAction {

    private ArgumentExtractor[] argumentExtractors;

    private CookieGenerator ticketGrantingTicketCookieGenerator;

    protected void initActionInternal() throws Exception {
        // nothing to do
    }

    public final void setArgumentExtractors(
        final ArgumentExtractor[] argumentExtractors) {
        this.argumentExtractors = argumentExtractors;
    }
    
    protected final ArgumentExtractor[] getArgumentExtractors() {
        return this.argumentExtractors;
    }
    
    public final void setTicketGrantingTicketCookieGenerator(final CookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator= ticketGrantingTicketCookieGenerator;
    }
    
    protected final CookieGenerator getTicketGrantingTicketCookieGenerator() {
        return this.ticketGrantingTicketCookieGenerator;
    }

    protected final void initAction() throws Exception {
        Assert.notNull(this.argumentExtractors,
            "argumentExtractors cannot be null.");
        Assert.notNull(this.ticketGrantingTicketCookieGenerator, "ticketGrantingTicketCookieGenerator cannot be null.");
        initActionInternal();
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
