/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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

import org.jasig.cas.authentication.AbstractAuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.support.openid.authentication.principal.OpenIdCredential;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.security.auth.login.FailedLoginException;
import javax.validation.constraints.NotNull;

/**
 * Ensures that the OpenId provided matches with the existing
 * TicketGrantingTicket. Otherwise, fail authentication.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
@Component("openIdCredentialsAuthenticationHandler")
public final class OpenIdCredentialsAuthenticationHandler extends AbstractAuthenticationHandler {

    @NotNull
    @Autowired
    @Qualifier("ticketRegistry")
    private TicketRegistry ticketRegistry;

    @Override
    public HandlerResult authenticate(final Credential credential) throws GeneralSecurityException {
        final OpenIdCredential c = (OpenIdCredential) credential;

        final TicketGrantingTicket t = this.ticketRegistry.getTicket(c.getTicketGrantingTicketId(),
                        TicketGrantingTicket.class);

        if (t == null || t.isExpired()) {
            throw new FailedLoginException("TGT is null or expired.");
        }
        final Principal principal = t.getAuthentication().getPrincipal();
        if (!principal.getId().equals(c.getUsername())) {
            throw new FailedLoginException("Principal ID mismatch");
        }
        return new DefaultHandlerResult(this, new BasicCredentialMetaData(c), principal);
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential instanceof OpenIdCredential;
    }

    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
}
