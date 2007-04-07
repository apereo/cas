/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.util.annotation.NotNull;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Class to automatically set the paths for the CookieGenerators.
 * <p>
 * Note: This is technically not threadsafe, but because its overriding with a
 * constant value it doesn't matter.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class AutomaticCookiePathSetterAction extends AbstractAction {

    /** CookieGenerator for the Warnings. */
    @NotNull
    private CookieGenerator warnCookieGenerator;

    /** CookieGenerator for the TicketGrantingTickets. */
    @NotNull
    private CookieGenerator ticketGrantingTicketCookieGenerator;

    /** Boolean to note whether we've set the values on the generators or not. */
    private boolean pathPopulated = false;

    protected Event doExecute(final RequestContext context) throws Exception {
        if (!this.pathPopulated) {
            final HttpServletRequest request = WebUtils
                .getHttpServletRequest(context);

            logger.info("Setting ContextPath for cookies to: "
                + request.getContextPath());
            this.warnCookieGenerator.setCookiePath(request.getContextPath());
            this.ticketGrantingTicketCookieGenerator.setCookiePath(request
                .getContextPath());
            this.pathPopulated = true;
        }

        return result("success");
    }

    public void setTicketGrantingTicketCookieGenerator(
        final CookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    public void setWarnCookieGenerator(final CookieGenerator warnCookieGenerator) {
        this.warnCookieGenerator = warnCookieGenerator;
    }
}
