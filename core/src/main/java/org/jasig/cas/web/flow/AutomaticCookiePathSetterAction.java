/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.web.flow.util.ContextUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.AbstractAction;

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
public final class AutomaticCookiePathSetterAction extends AbstractAction
    implements InitializingBean {

    /** CookieGenerator for the Warnings. */
    private CookieGenerator warnCookieGenerator;

    /** CookieGenerator for the TicketGrantingTickets. */
    private CookieGenerator ticketGrantingTicketCookieGenerator;

    /** Boolean to note whether we've set the values on the generators or not. */
    private boolean pathPopulated = false;

    protected Event doExecute(final RequestContext context) throws Exception {
        if (!this.pathPopulated) {
            final HttpServletRequest request = ContextUtils
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

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.warnCookieGenerator,
            "warnCookieGenerator cannot be null.");
        Assert.notNull(this.ticketGrantingTicketCookieGenerator,
            "ticketGrantingTicketCookieGenerator cannot be null.");
    }

    public void setTicketGrantingTicketCookieGenerator(
        final CookieGenerator ticketGrantingTicketCookieGenerator) {
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
    }

    public void setWarnCookieGenerator(final CookieGenerator warnCookieGenerator) {
        this.warnCookieGenerator = warnCookieGenerator;
    }
}
