/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.validation.ValidationRequest;


/**
 * Interface for the management of tickets.  All ticket creation, validation and deletion
 * happens through the manager.  The manager is in charge of maintaining the registry of
 * tickets.
 * 
 * Ticket Validation currently constitutes a "use" of a ticket and the state will be updating
 * accordingly.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface TicketManager
{
	/**
	 * 
	 * Method to create a a proxy granting ticket and add it to the registry.
	 * 
	 * @param ticketCreationAttributes The initial setup attributes for a ticket.
	 * @param ticket The parent ticket of the ProxyGrantingTicket
	 * @return the ticket created.
	 */
	ProxyGrantingTicket createProxyGrantingTicket(Principal principal, CasAttributes casAttributes, ServiceTicket ticket);
	
	/**
	 * 
	 * Method to create a proxy ticket and add it to the registry.
	 * 
	 * @param ticketCreationAttributes The initial setup attributes for a ticket.
	 * @param ticket The parent ticket of the ProxyTicket
	 * @return the ticket created.
	 */
	ProxyTicket createProxyTicket(Principal principal, CasAttributes casAttributes, ProxyGrantingTicket ticket);
	
	/**
	 * 
	 * Method to create a service ticket and add it to the registry.
	 * 
	 * @param ticketCreationAttributes The initial setup attributes for a ticket.
	 * @param ticket The parent ticket of the ServiceTIcket
	 * @return the ticket created.
	 */
	ServiceTicket createServiceTicket(Principal principal, CasAttributes casAttributes, TicketGrantingTicket ticket);
	
	/**
	 * 
	 * Method to create a a ticket granting ticket and add it to the registry.
	 * 
	 * @param ticketCreationAttributes The initial setup attributes for a ticket.
	 * @return the ticket created.
	 */
	TicketGrantingTicket createTicketGrantingTicket(Principal principal, CasAttributes casAttributes);
	
	/**
	 * 
	 * Method to delete a ticket based on the ticket id
	 * 
	 * @param ticketId The id of the ticket to delete.
	 * @return true if the ticket was deleted.  False otherwise.
	 */
	boolean deleteTicket(String ticketId);
	
	/**
	 * 
	 * Method to remove a ticket.
	 * 
	 * @param ticket The ticket to remove.
	 * @return true if the ticket was deleted.  False otherwise.
	 */
	boolean deleteTicket(Ticket ticket);
	
	/**
	 * 
	 * Method to retrieve a ticket from the registry, validate it for the request,
	 * check its expiration and if it passes, return the ticket.  Otherwise return
	 * null.
	 * 
	 * @param validationRequest
	 * @return the valid ticket.
	 * @throws InvalidTicketException
	 */
	ProxyGrantingTicket validateProxyGrantingTicket(ValidationRequest validationRequest) throws InvalidTicketException;
	
	/**
	 * 
	 * Method to retrieve a ticket from the registry, validate it for the request,
	 * check its expiration and if it passes, return the ticket.  Otherwise return
	 * null.
	 * 
	 * @param validationRequest
	 * @return the valid ticket.
	 * @throws InvalidTicketException
	 */
	ProxyTicket validateProxyTicket(ValidationRequest validationRequest) throws InvalidTicketException;
	
	/**
	 * 
	 * Method to retrieve a ticket from the registry, validate it for the request,
	 * check its expiration and if it passes, return the ticket.  Otherwise return
	 * null.
	 * 
	 * @param validationRequest
	 * @return the valid ticket.
	 * @throws InvalidTicketException
	 */
	ServiceTicket validateServiceTicket(ValidationRequest validationRequest) throws InvalidTicketException;
	
	/**
	 * 
	 * Method to retrieve a ticket from the registry, validate it for the request,
	 * check its expiration and if it passes, return the ticket.  Otherwise return
	 * null.
	 * 
	 * @param validationRequest
	 * @return the valid ticket.
	 * @throws InvalidTicketException
	 */
	TicketGrantingTicket validateTicketGrantingTicket(ValidationRequest validationRequest) throws InvalidTicketException;
}
