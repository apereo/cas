/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;

/**
 * Abstract class to handle the retrieval and authentication of non-interactive
 * credentials such as client certificates, NTLM, etc.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public abstract class AbstractNonInteractiveCredentialsAction extends
    AbstractAction {
    
    /** Instance of CentralAuthenticationService. */
    @NotNull
    private CentralAuthenticationService centralAuthenticationService;
    
    protected final boolean isRenewPresent(final RequestContext context) {
        return StringUtils.hasText(context.getRequestParameters().get("renew"));
    }

    protected final Event doExecute(final RequestContext context) {
        final Credentials credentials = constructCredentialsFromRequest(context);

        if (credentials == null) {
            return error();
        }
        
        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        final Service service = WebUtils.getService(context);

        if (isRenewPresent(context)
            && ticketGrantingTicketId != null
            && service != null) {

            try {
                final String serviceTicketId = this.centralAuthenticationService
                    .grantServiceTicket(ticketGrantingTicketId,
                        service,
                        credentials);
                WebUtils.putServiceTicketInRequestScope(context,
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
            WebUtils.putTicketGrantingTicketInRequestScope(
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
