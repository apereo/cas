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

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Service;
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
 * @version $Revision$ $Date$
 * @since 3.0
 * <p>
 * This is a published and supported CAS Server 3 API.
 * </p>
 */
public interface CentralAuthenticationService {

    /**
     * Create a TicketGrantingTicket based on opaque credentials supplied by the
     * caller.
     * 
     * @param credentials The credentials to create the ticket for
     * @return The String identifier of the ticket (may not be null).
     * @throws TicketException if ticket cannot be created
     */
    String createTicketGrantingTicket(Credentials credentials)
        throws TicketException;

    /**
     * Grant a ServiceTicket for a Service.
     * 
     * @param ticketGrantingTicketId Proof of prior authentication.
     * @param service The target service of the ServiceTicket.
     * @return the ServiceTicket for target Service.
     * @throws TicketException if the ticket could not be created.
     */
    String grantServiceTicket(String ticketGrantingTicketId, Service service)
        throws TicketException;

    /**
     * Grant a ServiceTicket for a Service *if* the principal resolved from the
     * credentials matches the principal associated with the
     * TicketGrantingTicket.
     * 
     * @param ticketGrantingTicketId Proof of prior authentication.
     * @param service The target service of the ServiceTicket.
     * @param credentials the Credentials to present to receive the
     * ServiceTicket
     * @return the ServiceTicket for target Service.
     * @throws TicketException if the ticket could not be created.
     */
    String grantServiceTicket(final String ticketGrantingTicketId,
        final Service service, final Credentials credentials)
        throws TicketException;

    /**
     * Validate a ServiceTicket for a particular Service.
     * 
     * @param serviceTicketId Proof of prior authentication.
     * @param service Service wishing to validate a prior authentication.
     * @return ServiceTicket if valid for the service
     * @throws TicketException if there was an error validating the ticket.
     */
    Assertion validateServiceTicket(final String serviceTicketId,
        final Service service) throws TicketException;

    /**
     * Destroy a TicketGrantingTicket. This has the effect of invalidating any
     * Ticket that was derived from the TicketGrantingTicket being destroyed.
     * 
     * @param ticketGrantingTicketId the id of the ticket we want to destroy
     */
    void destroyTicketGrantingTicket(final String ticketGrantingTicketId);

    /**
     * Delegate a TicketGrantingTicket to a Service for proxying authentication
     * to other Services.
     * 
     * @param serviceTicketId The service ticket that will delegate to a
     * TicketGrantingTicket
     * @param credentials The credentials of the service that wishes to have a
     * TicketGrantingTicket delegated to it.
     * @return TicketGrantingTicket that can grant ServiceTickets that proxy
     * authentication.
     * @throws TicketException if there was an error creating the ticket
     */
    String delegateTicketGrantingTicket(final String serviceTicketId,
        final Credentials credentials) throws TicketException;
}
