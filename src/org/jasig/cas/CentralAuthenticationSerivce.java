/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas;

import org.jasig.cas.authentication.AuthenticationSpecification;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;

/**
 * CentralAuthenticationService (CAS) is the published public interface/service facade.
 * CAS has the following responsibilities:
 * <ol>
 *   <li>Create, grant, find, and validate tickets</li>
 * </ol>
 * 
 * 
 * @author William G. Thompson, Jr.
 */
public interface CentralAuthenticationSerivce {
    
    /**
     * Create TicketGrantingTicket to a Principal if Credentials authenticate.
     * Principals are the root of all Tickets.
     * 
     * @return the TicketGrantingTicket created for the prinicipal.
     */
     public TicketGrantingTicket createTicketGrantingTicket(Credentials credentials);

     /**
      * Grant a ServiceTicket for a Service.
      * 
      * @param tgtid Proof of prior authentication.
      * @param service The target service of the ServiceTicket.
      * @return the ServiceTicket for target Service.
      */
     public ServiceTicket grantServiceTicket(String ticketGrantingTicketId, Service service);
   
     /**
      * Validate a ServiceTicket for a particular Service
      * 
      * @param ticket Proof of prior authentication.
      * @param service Service wishing to validate a prior authentication.
      * @return ServiceTicket if valid for the service and satisifies AuthenticationSpecification.
      */
     public ServiceTicket validateServiceTicket(String serviceTicketId, Service service, AuthenticationSpecification authspec);
     
     /**
      * Destroy a TicketGrantingTicket.  This has the effect of invalidating
      * any Ticket that was derived from the TicketGrantingTicket being destroyed.
      */
     public void destroyTicketGrantingTicket(String ticketGrantingTicketId);
     
     /**
      * Grant a TicketGrantingTicket to a Service for proxying authentication
      * to other Services.
      * 
      * @return TicketGrantingTicket that can grant ServiceTickets that proxy authentication.
      */
     public TicketGrantingTicket grantTicketGrantingTicket(String serviceTicketId, Credentials credentials);

     /**
      * Return the CAS version.
      * 
      * @return the CAS version string.
      */
     public String getVersion();
     
}
