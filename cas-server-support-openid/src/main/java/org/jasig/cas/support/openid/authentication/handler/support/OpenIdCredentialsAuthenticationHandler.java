/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.authentication.handler.support;

import org.jasig.cas.authentication.handler.AuthenticationException;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.support.openid.authentication.principal.OpenIdCredentials;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;

import javax.validation.constraints.NotNull;

/**
 * Ensures that the OpenId provided matches with the existing
 * TicketGrantingTicket. Otherwise, fail authentication.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class OpenIdCredentialsAuthenticationHandler implements
    AuthenticationHandler {

    @NotNull
    private TicketRegistry ticketRegistry;

    public boolean authenticate(final Credentials credentials)
        throws AuthenticationException {
        final OpenIdCredentials c = (OpenIdCredentials) credentials;

        final TicketGrantingTicket t = (TicketGrantingTicket) this.ticketRegistry
            .getTicket(c.getTicketGrantingTicketId(),
                TicketGrantingTicket.class);

        if (t.isExpired()) {
            return false;
        }

        return t.getAuthentication().getPrincipal().getId().equals(
            c.getUsername());
    }

    public boolean supports(final Credentials credentials) {
        return credentials instanceof OpenIdCredentials;
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
}
