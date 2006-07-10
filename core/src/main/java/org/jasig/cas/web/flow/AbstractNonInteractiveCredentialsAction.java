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
import org.jasig.cas.web.util.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;

/**
 * Abstract class to handle the retrieval and authentication of non-interactive
 * credentials such as client certifices, NTLM, etc.
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
                    .grantServiceTicket(
                        ticketGrantingTicketId,
                        new SimpleService(WebUtils
                            .stripJsessionFromUrl(service)), credentials);
                ContextUtils.addAttribute(context, WebConstants.TICKET,
                    serviceTicketId);
                return result("warn");
            } catch (final TicketException e) {
                if (e.getCause() != null
                    && AuthenticationException.class.isAssignableFrom(e
                        .getCause().getClass())) {
                    onError(context, credentials);
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
            onSuccess(context, credentials);
            return success();
        } catch (final TicketException e) {
            onError(context, credentials);
            return error();
        }
    }

    /**
     * Hook method to allow for additional processing of the response before
     * returning an error event.
     * 
     * @param context the context for this specific request.
     * @param credentials the credentials for this request.
     */
    protected void onError(final RequestContext context,
        final Credentials credentials) {
        // default implementation does nothing
    }

    /**
     * Hook method to allow for additional processing of the response before
     * returning a success event.
     * 
     * @param context the context for this specific request.
     * @param credentials the credentials for this request.
     */
    protected void onSuccess(final RequestContext context,
        final Credentials credentials) {
        // default implementation does nothing
    }

    /**
     * Abstract method to implement to construct the credentials from the
     * request object.
     * 
     * @param context the context for this request.
     * @return the constructed credentials or null if none could be constructed
     * from the request.
     */
    protected abstract Credentials constructCredentialsFromRequest(
        final RequestContext context);
}
