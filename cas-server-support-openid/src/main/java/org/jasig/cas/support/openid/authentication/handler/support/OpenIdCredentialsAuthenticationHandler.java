/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

        boolean result = false;
        final TicketGrantingTicket t = (TicketGrantingTicket) this.ticketRegistry
                .getTicket(c.getTicketGrantingTicketId(),
                        TicketGrantingTicket.class);

        if (t == null || t.isExpired()) {
            result = false;
        } else {
            result = t.getAuthentication().getPrincipal().getId().equals(
                    c.getUsername());
        }
        return result;
    }

    public boolean supports(final Credentials credentials) {
        return credentials instanceof OpenIdCredentials;
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
}
