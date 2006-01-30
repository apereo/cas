/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.SimpleService;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.util.StringUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Abstract class to handle the retrieval and authentication of non-interactive
 * credentials such as client certifices, NTML, etc.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public abstract class AbstractNonInteractiveCredentialsAction extends
    AbstractCasLoginAction {

    protected final Event doExecuteInternal(final RequestContext context,
        final String ticketGrantingTicketId, final String service,
        final boolean gateway, final boolean renew, final boolean warn) {

        final Credentials credentials = constructCredentialsFromRequest(context);
        
        if (credentials == null) {
            return error();
        }

        if (renew && StringUtils.hasText(ticketGrantingTicketId)
            && StringUtils.hasText(service)) {

            try {
                final String serviceTicketId = getCentralAuthenticationService()
                    .grantServiceTicket(ticketGrantingTicketId,
                        new SimpleService(service), credentials);
                ContextUtils.addAttribute(context, WebConstants.TICKET,
                    serviceTicketId);
                return success();
            } catch (final TicketException e) {
                if (e.getCause() != null
                    && AuthenticationException.class.isAssignableFrom(e
                        .getCause().getClass())) {
                    return error();
                }
                getCentralAuthenticationService().destroyTicketGrantingTicket(
                    ticketGrantingTicketId);
                if (logger.isDebugEnabled()) {
                    logger
                        .debug(
                            "Attempted to generate a ServiceTicket using renew=true with different credentials",
                            e);
                }
            }
        }

        try {
            final String newTicketGrantingTicketId = getCentralAuthenticationService()
                .createTicketGrantingTicket(credentials);
            ContextUtils.addAttribute(context,
                AbstractLoginAction.REQUEST_ATTRIBUTE_TICKET_GRANTING_TICKET,
                newTicketGrantingTicketId);
            return success();
        } catch (final TicketException e) {
            return error();
        }
    }

    protected abstract Credentials constructCredentialsFromRequest(
        final RequestContext context);
}
