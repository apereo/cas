/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.ticket.TicketException;
import org.springframework.util.Assert;
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
    AbstractLoginAction {

    /** Instance of CentralAuthenticationService. */
    private CentralAuthenticationService centralAuthenticationService;

    protected final Event doExecute(final RequestContext context) {
        final Credentials credentials = constructCredentialsFromRequest(context);

        if (credentials == null) {
            return error();
        }

        if (getCasArgumentExtractor().isRenewPresent(context)
            && getCasArgumentExtractor().isTicketGrantingTicketCookiePresent(
                context) && getCasArgumentExtractor().isServicePresent(context)) {

            final String ticketGrantingTicketId = getCasArgumentExtractor()
                .extractTicketGrantingTicketFromCookie(context);

            try {
                final String serviceTicketId = this.centralAuthenticationService
                    .grantServiceTicket(ticketGrantingTicketId,
                        getCasArgumentExtractor().extractServiceFrom(context),
                        credentials);
                getCasArgumentExtractor().putServiceTicketIn(context,
                    serviceTicketId);
                return result("warn");
            } catch (final TicketException e) {
                if (e.getCause() != null
                    && AuthenticationException.class.isAssignableFrom(e
                        .getCause().getClass())) {
                    onError(context, credentials);
                    return error();
                }
                this.centralAuthenticationService
                    .destroyTicketGrantingTicket(ticketGrantingTicketId);
                if (logger.isDebugEnabled()) {
                    logger
                        .debug(
                            "Attempted to generate a ServiceTicket using renew=true with different credentials",
                            e);
                }
            }
        }

        try {
            getCasArgumentExtractor().putTicketGrantingTicketIn(
                context,
                this.centralAuthenticationService
                    .createTicketGrantingTicket(credentials));
            onSuccess(context, credentials);
            return success();
        } catch (final TicketException e) {
            onError(context, credentials);
            return error();
        }
    }

    public final void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        this.centralAuthenticationService = centralAuthenticationService;
    }

    protected void initActionInternal() throws Exception {
        Assert.notNull(this.centralAuthenticationService,
            "centralAuthenticationService cannot be null.");
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
