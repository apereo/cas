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
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;

/**
 * The aim of this class is to generate service ticket and ticket granting ticket
 * in order to decouple implementation and interface of tickets.
 *
 * @author Vincenzo Barrea.
 */
public interface TicketGenerator {

    /**
     * Generate a ticket granting ticket for the specified Authentication request
     * May throw an {@link IllegalArgumentException} if the Authentication object is null.
     *
     * @param authentication The Authentication request for this ticket
     * @return A new ticket granting ticket
     */
    TicketGrantingTicket generateTicketGrantingTicket(Authentication authentication);

    /**
     * Generate a ticket granting ticket.
     *
     * @param id The id of the new ticket
     * @param grantingTicket The related ticket granting ticket
     * @param authentication The Authentication request for this ticket
     * @param expirationPolicy The ExpirationPolicy for this ticket
     * @return a new ticket granting ticket
     *
     * @throws java.lang.IllegalArgumentException in case of missing authentication or expiration policy
     */
    TicketGrantingTicket generateTicketGrantingTicket(String id,
                                                      TicketGrantingTicket grantingTicket,
                                                      Authentication authentication,
                                                      ExpirationPolicy expirationPolicy);

    /**
     * Generate a new ServiceTicket with a Unique Id, a TicketGrantingTicket,
     * a Service.
     *
     * @param ticketGrantingTicket the TicketGrantingTicket parent.
     * @param service the service this ticket is for.
     * @param credentialsProvided if the credentials are provided.
     * @return a new service ticket
     * @throws IllegalArgumentException if the TicketGrantingTicket or the
     * Service are null.
     */
    ServiceTicket generateServiceTicket(TicketGrantingTicket ticketGrantingTicket,
                                        Service service,
                                        boolean credentialsProvided);

    /**
     * Generate a new ServiceTicket with a Unique Id, a TicketGrantingTicket,
     * a Service, Expiration Policy and a flag to determine if the ticket
     * creation was from a new Login or not.
     *
     * @param id the unique identifier for the ticket.
     * @param ticketGrantingTicket the TicketGrantingTicket parent.
     * @param service the service this ticket is for.
     * @param fromNewLogin is it from a new login.
     * @param expirationPolicy the expiration policy for the Ticket.
     * @return a new service ticket
     * @throws IllegalArgumentException if the TicketGrantingTicket or the
     * Service are null.
     */
    ServiceTicket generateServiceTicket(String id,
                                        TicketGrantingTicket ticketGrantingTicket,
                                        Service service,
                                        boolean fromNewLogin,
                                        ExpirationPolicy expirationPolicy);

    /**
     * Generate a new proxy granting ticket for the specified service and authentication.
     *
     * @param serviceTicket The service which will generate the ticket
     * @param authentication The Authentication we wish to grant a ticket for.
     * @return a new proxy granting ticket
     */
    TicketGrantingTicket generateProxyGrantingTicket(ServiceTicket serviceTicket, Authentication authentication);
}
