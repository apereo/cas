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

import java.security.GeneralSecurityException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;
import org.jasig.cas.authentication.Principal;
import org.jasig.cas.support.openid.authentication.principal.OpenIdCredential;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Ensures that the OpenId provided matches with the existing
 * TicketGrantingTicket. Otherwise, fail authentication.
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 */
public final class OpenIdCredentialsAuthenticationHandler implements AuthenticationHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @NotNull
    private TicketRegistry ticketRegistry;

    private String name;


    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException, PreventedException {
        final OpenIdCredential c = (OpenIdCredential) credential;

        final TicketGrantingTicket t = (TicketGrantingTicket) this.ticketRegistry.getTicket(
                c.getTicketGrantingTicketId(), TicketGrantingTicket.class);

        if (t == null || t.isExpired()) {
            throw new CredentialExpiredException();
        }
        logger.debug("Attempting to authenticate {}", c.getUsername());
        final Principal ticketPrincipal = t.getAuthentication().getPrincipal();
        if (ticketPrincipal.getId().equals(c.getUsername())) {
            return new HandlerResult(this, ticketPrincipal);
        }
        throw new FailedLoginException();
    }

    public boolean supports(final Credential credential) {
        return credential instanceof OpenIdCredential;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return StringUtils.hasText(this.name) ? this.name : getClass().getSimpleName();
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
}
