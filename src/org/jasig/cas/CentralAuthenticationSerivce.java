/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas;

import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.ProxyGrantingTicket;
import org.jasig.cas.ticket.ProxyTicket;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
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
    
    /********** Grant Tickets **********/
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
      * @param tgt Proof of prior authentication.
      * @param service The target service of the ServiceTicket.
      * @return the ServiceTicket for target Service.
      */
     public ServiceTicket grantServiceTicket(TicketGrantingTicket tgt, Service service);
     
     /**
      * Grant a ProxyGrantingTicket for a Service.
      * 
      * @param st ServiceTicket issued for service requesting to proxy authentication.
      * @return the ProxyGrantingTicket for the service wishing to proxy authentication.
      */
     public ProxyGrantingTicket grantProxyGrantingTicket(ServiceTicket st);

     /**
      * Grant a ProxyGrantingTicket for a Service.
      * 
      * @param pgt ProxyTicket issued for service requesting proxy authentication.
      * @return the ProxyGrantingTicket for the service wishing to proxy authentication.
      */
     public ProxyGrantingTicket grantProxyGrantingTicket(ProxyTicket pt);

     /**
      * Grant a ProxyTicket for a Service.
      * 
      * @param pgt Proof of prior delegated authentication.
      * @return a ProxyTicket for the Service.
      */
     public ProxyTicket grantProxyTicket(ProxyGrantingTicket pgt, Service service);
          
     
     /********** Validate Tickets for Services **********/
     /**
      * Validate a Ticket for a particular Service
      * 
      * @param ticket Proof of prior authentication.
      * @param service Service wishing to validate a prior authentication.
      * @return true if Ticket is valid for the service, otherwise false.
      */
     boolean validate(Ticket ticket, Service service);
     
     
     /********* Finder Methods **********/
     /**
      * Finder method for TicketGrantingTickets
      * 
      * @param pgtid Id of a ProxyGrantingTicket.
      * @return the ProxyGrantingTicket for the Id.
      */
     public TicketGrantingTicket lookupTicketGrantingTicketForId(String tgtid);

     /**
      * Finder method for ServiceTickets
      * 
      * @param stid Id of a ServiceTicket.
      * @return the ServiceTicket for the Id.
      */
     public ServiceTicket lookupServiceTicketForId(String stid);

     /**
      * Finder method for ProxyGrantingTickets
      * 
      * @param pgtid Id of a ProxyGrantingTicket.
      * @return the ProxyGrantingTicket for the Id.
      */
     public ProxyGrantingTicket lookupProxyGrantingTicketForId(String pgtid);

     /**
      * Finder method for ProxyTickets
      * 
      * @param ptid Id of a ProxyTicket.
      * @return the ProxyTicket for the Id.
      */
     public ProxyTicket lookupProxyTicketForId(String ptid);
     
     
     /********** Version Method **********/
     /**
      * Return the CAS version.
      * 
      * @return the CAS version string.
      */
     public String getVersion();
     
}
