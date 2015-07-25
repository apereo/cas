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
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;

import java.util.List;
import java.util.Map;

/**
 * Interface for a ticket granting ticket. A TicketGrantingTicket is the main
 * access into the CAS service layer. Without a TicketGrantingTicket, a user of
 * CAS cannot do anything.
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public interface TicketGrantingTicket extends Ticket {

    /** The prefix to use when generating an id for a Ticket Granting Ticket. */
    String PREFIX = "TGT";

    /** The prefix to use when generating an id for a Proxy Granting Ticket. */
    String PROXY_GRANTING_TICKET_PREFIX = "PGT";

    /** The prefix to use when generating an id for a Proxy Granting Ticket IOU. */
    String PROXY_GRANTING_TICKET_IOU_PREFIX = "PGTIOU";

    /**
     * Method to retrieve the authentication.
     *
     * @return the authentication
     */
    Authentication getAuthentication();

    /**
     * Gets a list of supplemental authentications associated with this ticket.
     * A supplemental authentication is one other than the one used to create the ticket,
     * for example, a forced authentication that happens after the beginning of a CAS SSO session.
     *
     * @return Non-null list of supplemental authentications.
     */
    List<Authentication> getSupplementalAuthentications();

    /**
     * Grant a ServiceTicket for a specific service.
     *
     * @param id The unique identifier for this ticket.
     * @param service The service for which we are granting a ticket
     * @param expirationPolicy the expiration policy.
     * @param credentialsProvided if the credentials are provided.
     * @return the service ticket granted to a specific service for the
     * principal of the TicketGrantingTicket
     */
    ServiceTicket grantServiceTicket(String id, Service service,
        ExpirationPolicy expirationPolicy, boolean credentialsProvided);

    /**
     * Gets an immutable map of service ticket and services accessed by this ticket-granting ticket.
     *
     * @return an immutable map of service ticket and services accessed by this ticket-granting ticket.
    */
    Map<String, Service> getServices();

    /**
     * Remove all services of the TGT (at logout).
     */
    void removeAllServices();

    /**
     * Mark a ticket as expired.
     */
    void markTicketExpired();

    /**
     * Convenience method to determine if the TicketGrantingTicket is the root
     * of the hierarchy of tickets.
     *
     * @return true if it has no parent, false otherwise.
     */
    boolean isRoot();

    /**
     * Gets the ticket-granting ticket at the root of the ticket hierarchy.
     *
     * @return Non-null root ticket-granting ticket.
     */
    TicketGrantingTicket getRoot();

    /**
     * Gets all authentications ({@link #getAuthentication()}, {@link #getSupplementalAuthentications()}) from this
     * instance and all dependent tickets that reference this one.
     *
     * @return Non-null list of authentication associated with this ticket in leaf-first order.
     */
    List<Authentication> getChainedAuthentications();


    /**
    * Gets the service that produced a proxy-granting ticket.
    *
    * @return  Service that produced proxy-granting ticket or null if this is
    * not a proxy-granting ticket.
     * @since 4.1
    */
    Service getProxiedBy();

}
