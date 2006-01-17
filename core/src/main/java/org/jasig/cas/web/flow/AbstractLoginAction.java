/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.jasig.cas.web.util.SecureCookieGenerator;
import org.jasig.cas.web.util.WebUtils;
import org.springframework.util.Assert;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.AbstractAction;

/**
 * Abstract class to retrieve common attributes from request and expose them so
 * that implementing classes do not need to replicate same behavior.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public abstract class AbstractLoginAction extends AbstractAction {

    private static final String EVENT_AUTHENTICATION_REQUIRED = "authenticationRequired";

    private static final String EVENT_CONTINUE_CHECK = "continueCheck";

    private static final String EVENT_AUTHENTICATED_BUT_NO_SERVICE = "authenticatedButNoService";

    private static final String EVENT_GATEWAY = "gateway";

    private static final String EVENT_HAS_SERVICE = "hasService";

    private static final String EVENT_REDIRECT = "redirect";

    protected static final String REQUEST_PARAM_GATEWAY = "gateway";

    protected static final String REQUEST_ATTRIBUTE_TICKET_GRANTING_TICKET = "ticketGrantingTicketId";

    private SecureCookieGenerator ticketGrantingTicketCookieGenerator;

    private SecureCookieGenerator warnCookieGenerator;

    protected final Event doExecute(final RequestContext context)
        throws Exception {
        final HttpServletRequest request = ContextUtils
            .getHttpServletRequest(context);
        final String ticketGrantingTicketId = this.ticketGrantingTicketCookieGenerator
            .getCookieValue(request);
        final String service = WebUtils.getRequestParameterAsString(request,
            WebConstants.SERVICE);
        final boolean gateway = WebUtils.getRequestParameterAsBoolean(request,
            REQUEST_PARAM_GATEWAY);
        final boolean renew = WebUtils.getRequestParameterAsBoolean(request,
            WebConstants.RENEW);
        final boolean warn = Boolean.valueOf(
            this.warnCookieGenerator.getCookieValue(request)).booleanValue();

        return doExecuteInternal(context, ticketGrantingTicketId, service,
            gateway, renew, warn);
    }

    protected final Event authenticationRequired() {
        return result(EVENT_AUTHENTICATION_REQUIRED);
    }

    protected final Event continueCheck() {
        return result(EVENT_CONTINUE_CHECK);
    }

    protected final Event authenticatedButNoService() {
        return result(EVENT_AUTHENTICATED_BUT_NO_SERVICE);
    }

    protected final Event gateway() {
        return result(EVENT_GATEWAY);
    }

    protected final Event hasService() {
        return result(EVENT_HAS_SERVICE);
    }

    protected final Event redirect() {
        return result(EVENT_REDIRECT);
    }

    protected abstract Event doExecuteInternal(final RequestContext context,
        final String ticketGrantingTicketId, final String service,
        final boolean gateway, final boolean renew, final boolean warn);

    protected final void initAction() {
        Assert.notNull(this.ticketGrantingTicketCookieGenerator);
        Assert.notNull(this.warnCookieGenerator);
        initActionInternal();
    }

    public final void setTicketGrantingTicketCookieGenerator(
        final SecureCookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    public final void setWarnCookieGenerator(
        final SecureCookieGenerator warnCookieGenerator) {
        this.warnCookieGenerator = warnCookieGenerator;
    }

    
    protected final SecureCookieGenerator getTicketGrantingTicketCookieGenerator() {
        return this.ticketGrantingTicketCookieGenerator;
    }

    
    protected final SecureCookieGenerator getWarnCookieGenerator() {
        return this.warnCookieGenerator;
    }

    protected void initActionInternal() {
        // to be overwritten as needed
    }
}
