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
package org.jasig.cas.support.openid.web.flow;

import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.support.openid.authentication.principal.OpenIdCredential;
import org.jasig.cas.support.openid.authentication.principal.OpenIdService;
import org.jasig.cas.support.openid.web.support.DefaultOpenIdUserNameExtractor;
import org.jasig.cas.support.openid.web.support.OpenIdUserNameExtractor;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.web.flow.AbstractNonInteractiveCredentialsAction;
import org.jasig.cas.web.support.WebUtils;

import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;

/**
 * Attempts to utilize an existing single sign on session, but only if the
 * Principal of the existing session matches the new Principal. Note that care
 * should be taken when using credentials that are automatically provided and
 * not entered by the user.
 *
 * @author Scott Battaglia
 * @since 3.1
 */
public final class OpenIdSingleSignOnAction extends AbstractNonInteractiveCredentialsAction {

    private static final String OPENID_LOCALID = "openIdLocalId";

    @NotNull
    private OpenIdUserNameExtractor extractor = new DefaultOpenIdUserNameExtractor();

    @NotNull
    private final TicketRegistry ticketRegistry;

    /**
     * Constructor.
     *
     * @param ticketRegistry the ticket registry
     */
    public OpenIdSingleSignOnAction(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }

    public void setExtractor(final OpenIdUserNameExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    protected Credential constructCredentialsFromRequest(final RequestContext context) {
        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        final String openidIdentityParameter = context.getRequestParameters().get("openid.identity");
        String userName = null;
        if (OpenIdUserNameExtractor.SELECT_IDENTIFIER.equals(openidIdentityParameter)) {
            userName = OpenIdUserNameExtractor.SELECT_IDENTIFIER;
            context.getExternalContext().getSessionMap().remove(OPENID_LOCALID);
            // already authenticated: retrieve the username from the authentication
            if (ticketGrantingTicketId != null) {
                final TicketGrantingTicket tgt = ticketRegistry.getTicket(ticketGrantingTicketId, TicketGrantingTicket.class);
                if (tgt != null && !tgt.isExpired()) {
                    userName = tgt.getAuthentication().getPrincipal().getId();
                }
            }
        } else {
            userName = this.extractor.extractLocalUsernameFromUri(openidIdentityParameter);
            context.getExternalContext().getSessionMap().put(OPENID_LOCALID, userName);
        }
        final Service service = WebUtils.getService(context);

        // clear the service because otherwise we can fake the username
        if (service instanceof OpenIdService && userName == null) {
            context.getFlowScope().remove("service");
        }

        if (ticketGrantingTicketId == null || userName == null) {
            return null;
        }

        return new OpenIdCredential(
                ticketGrantingTicketId, userName);
    }
}
