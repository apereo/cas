/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas;

import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.authentication.Service;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.ticket.TicketCreationException;
import org.jasig.cas.ticket.TicketException;
import org.jasig.cas.validation.Assertion;

/**
 * The main runtime interface between a Java application and CAS. This is the
 * central public API class abstracting the notion of an authentication service.
 * <p>
 * The main function of the CentralAuthenticationService is to offer create,
 * grant, find and validate operations for instances CAS tickets.
 * 
 * @author William G. Thompson, Jr.
 * @author Dmitry Kopylenko
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface CentralAuthenticationService {

    /**
     * Create a TicketGrantingTicket for a principal given the credentials
     * 
     * @param credentials The credentials to create the ticket for
     * @return The String identifier of the ticket.
     * @throws AuthenticationException if credentials do not authenticate
     * @throws TicketCreationException if ticket cannot be created
     */
    String createTicketGrantingTicket(Credentials credentials)
        throws AuthenticationException, TicketCreationException;

    /**
     * Grant a ServiceTicket for a Service.
     * 
     * @param ticketGrantingTicketId Proof of prior authentication.
     * @param service The target service of the ServiceTicket.
     * @return the ServiceTicket for target Service.
     * @throws TicketCreationException
     */
    String grantServiceTicket(String ticketGrantingTicketId, Service service)
        throws TicketCreationException;

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
     * @throws TicketCreationException
     * @throws AuthenticationException
     */
    String grantServiceTicket(String ticketGrantingTicketId, Service service,
        Credentials credentials) throws AuthenticationException,
        TicketCreationException;

    /**
     * Validate a ServiceTicket for a particular Service
     * 
     * @param serviceTicketId Proof of prior authentication.
     * @param service Service wishing to validate a prior authentication.
     * @return ServiceTicket if valid for the service
     * @throws TicketException
     */
    Assertion validateServiceTicket(String serviceTicketId, Service service)
        throws TicketException;

    /**
     * Destroy a TicketGrantingTicket. This has the effect of invalidating any
     * Ticket that was derived from the TicketGrantingTicket being destroyed.
     * 
     * @param ticketGrantingTicketId the id of the ticket we want to destroy
     */
    void destroyTicketGrantingTicket(String ticketGrantingTicketId);

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
     * @throws TicketException
     * @throws AuthenticationException
     */
    String delegateTicketGrantingTicket(String serviceTicketId,
        Credentials credentials) throws TicketException,
        AuthenticationException;
}
