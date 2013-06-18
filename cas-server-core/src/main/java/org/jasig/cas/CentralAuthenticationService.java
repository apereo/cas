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
package org.jasig.cas;

import java.util.List;

import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.logout.LogoutRequest;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.validation.Assertion;

/**
 * CAS viewed as a set of services to generate and validate Tickets.
 * <p>
 * This is the interface between a Web HTML, Web Services, RMI, or any other
 * request processing layer and the CAS Service viewed as a mechanism to
 * generate, store, validate, and retrieve Tickets containing Authentication
 * information. The features of the request processing layer (the HttpXXX
 * Servlet objects) are not visible here or in any modules behind this layer. In
 * theory, a standalone application could call these methods directly as a
 * private authentication service.
 * </p>
 *
 * @author William G. Thompson, Jr.
 * @author Dmitry Kopylenko
 * @author Scott Battaglia
 * @author Marvin S. Addison
 *
 * @since 3.0
 */
public interface CentralAuthenticationService {

    /**
     * Create a {@link org.jasig.cas.ticket.TicketGrantingTicket} by authenticating credentials.
     * The details of the security policy around credential authentication and the definition
     * of authentication success are dependent on the implementation, but it SHOULD be safe to assume
     * that at least one credential MUST be authenticated for ticket creation to succeed.
     *
     * @param credentials One or more credentials that may be authenticated in order to create the ticket.
     *
     * @return Non-null ticket-granting ticket identifier.
     *
     * @throws AuthenticationException on errors authenticating the credentials
     * @throws TicketException if ticket cannot be created
     */
    String createTicketGrantingTicket(Credential... credentials)
        throws AuthenticationException, TicketException;

    /**
     * Grants a {@link org.jasig.cas.ticket.ServiceTicket} that may be used to access the given service.
     *
     * @param ticketGrantingTicketId Proof of prior authentication.
     * @param service The target service of the ServiceTicket.
     *
     * @return Non-null service ticket identifier.
     *
     * @throws TicketException if the ticket could not be created.
     */
    String grantServiceTicket(String ticketGrantingTicketId, Service service) throws TicketException;

    /**
     * Grant a {@link org.jasig.cas.ticket.ServiceTicket} that may be used to access the given service
     * by authenticating the given credentials.
     * The details of the security policy around credential authentication and the definition
     * of authentication success are dependent on the implementation, but it SHOULD be safe to assume
     * that at least one credential MUST be authenticated for ticket creation to succeed.
     * <p>
     * The principal that is resolved from the authenticated credentials MUST be the same as that to which
     * the given ticket-granting ticket was issued.
     * </p>
     *
     * @param ticketGrantingTicketId Proof of prior authentication.
     * @param service The target service of the ServiceTicket.
     * @param credentials One or more credentials to authenticate prior to granting the service ticket.
     *
     * @return Non-null service ticket identifier.
     *
     * @throws AuthenticationException on errors authenticating the credentials
     * @throws TicketException if the ticket could not be created.
     */
    String grantServiceTicket(
            final String ticketGrantingTicketId, final Service service, final Credential... credentials)
            throws AuthenticationException, TicketException;

    /**
     * Validate a ServiceTicket for a particular Service.
     *
     * @param serviceTicketId Proof of prior authentication.
     * @param service Service wishing to validate a prior authentication.
     *
     * @return Non-null ticket validation assertion.
     *
     * @throws TicketException if there was an error validating the ticket.
     */
    Assertion validateServiceTicket(final String serviceTicketId, final Service service) throws TicketException;

    /**
     * Destroy a TicketGrantingTicket and perform back channel logout. This has the effect of invalidating any
     * Ticket that was derived from the TicketGrantingTicket being destroyed. May throw an
     * {@link IllegalArgumentException} if the TicketGrantingTicket ID is null.
     *
     * @param ticketGrantingTicketId the id of the ticket we want to destroy
     * @return the logout requests.
     */
    List<LogoutRequest> destroyTicketGrantingTicket(final String ticketGrantingTicketId);

    /**
     * Delegate a TicketGrantingTicket to a Service for proxying authentication
     * to other Services.
     *
     * @param serviceTicketId The service ticket identifier that will delegate to a
     * {@link org.jasig.cas.ticket.TicketGrantingTicket}.
     * @param credentials One or more credentials to authenticate prior to delegating the ticket.
     *
     * @return Non-null ticket-granting ticket identifier that can grant {@link org.jasig.cas.ticket.ServiceTicket}
     * that proxy authentication.
     *
     * @throws AuthenticationException on errors authenticating the credentials
     * @throws TicketException if there was an error creating the ticket
     */
    String delegateTicketGrantingTicket(final String serviceTicketId, final Credential... credentials)
            throws AuthenticationException, TicketException;
}
