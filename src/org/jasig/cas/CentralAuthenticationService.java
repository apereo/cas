/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas;

import org.jasig.cas.authentication.Assertion;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.Service;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.ticket.TicketCreationException;
import org.jasig.cas.ticket.TicketException;

/**
 * CentralAuthenticationService (CAS) is the published public interface/service facade.
 * CAS has the following responsibilities:
 * <ol>
 *   <li>Create, grant, find, and validate tickets</li>
 * </ol>
 * 
 * @author William G. Thompson, Jr.
 * @author Dmitry Kopylenko
 * @author Scott Battaglia
 */
public interface CentralAuthenticationService {

    /**
     * Create a TicketGrantingTicket for a principal given the credentials 
     * @param credentials The credentials to create the ticket for
     * @return The String identifier of the ticket.
     * @throws AuthenticationException
     */
    String createTicketGrantingTicket(Credentials credentials) throws AuthenticationException, TicketCreationException;

    /**
     * Grant a ServiceTicket for a Service.
     * 
     * @param tgtid Proof of prior authentication.
     * @param service The target service of the ServiceTicket.
     * @return the ServiceTicket for target Service.
     */
    String grantServiceTicket(String ticketGrantingTicketId, Service service) throws TicketCreationException;

    /**
     * Validate a ServiceTicket for a particular Service
     * 
     * @param ticket Proof of prior authentication.
     * @param service Service wishing to validate a prior authentication.
     * @param authenticationSpecification The specification of the authentication parameters we have defined that the ticket must meet to be valid.
     * @return ServiceTicket if valid for the service and satisifies AuthenticationSpecification.
     */
    Assertion validateServiceTicket(String serviceTicketId, Service service) throws TicketException;

    /**
     * Destroy a TicketGrantingTicket.  This has the effect of invalidating
     * any Ticket that was derived from the TicketGrantingTicket being destroyed.
     * 
     * @param ticketGrantingTicketId the id of the ticket we want to destroy
     */
    void destroyTicketGrantingTicket(String ticketGrantingTicketId);

    /**
     * Delegate a TicketGrantingTicket to a Service for proxying authentication
     * to other Services.
     * 
     * @return TicketGrantingTicket that can grant ServiceTickets that proxy authentication.
     */
    String delegateTicketGrantingTicket(String serviceTicketId, Credentials credentials) throws TicketException, AuthenticationException;

    /**
     * Return the CAS version.
     * 
     * @return the CAS version string.
     */
    String getVersion();

}