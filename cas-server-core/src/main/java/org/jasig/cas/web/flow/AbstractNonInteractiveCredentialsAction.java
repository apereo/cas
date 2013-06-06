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
package org.jasig.cas.web.flow;

import javax.validation.constraints.NotNull;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.util.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * Abstract class to handle the retrieval and authentication of non-interactive
 * credential such as client certificates, NTLM, etc.
 *
 * @author Scott Battaglia

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
        final Credential credential = constructCredentialsFromRequest(context);

        if (credential == null) {
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
                            credential);
                WebUtils.putServiceTicketInRequestScope(context,
                    serviceTicketId);
                return result("warn");
            } catch (final AuthenticationException e) {
                onError(context, credential);
                return error();
            } catch (final TicketException e) {
                this.centralAuthenticationService.destroyTicketGrantingTicket(ticketGrantingTicketId);
                logger.debug("Attempted to generate a ServiceTicket using renew=true with different credential", e);
            }
        }

        try {
            WebUtils.putTicketGrantingTicketInRequestScope(
                context,
                this.centralAuthenticationService
                    .createTicketGrantingTicket(credential));
            onSuccess(context, credential);
            return success();
        } catch (final Exception e) {
            onError(context, credential);
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
     * @param credential the credential for this request.
     */
    protected void onError(final RequestContext context,
        final Credential credential) {
        // default implementation does nothing
    }

    /**
     * Hook method to allow for additional processing of the response before
     * returning a success event.
     *
     * @param context the context for this specific request.
     * @param credential the credential for this request.
     */
    protected void onSuccess(final RequestContext context,
        final Credential credential) {
        // default implementation does nothing
    }

    /**
     * Abstract method to implement to construct the credential from the
     * request object.
     *
     * @param context the context for this request.
     * @return the constructed credential or null if none could be constructed
     * from the request.
     */
    protected abstract Credential constructCredentialsFromRequest(
        final RequestContext context);
}
