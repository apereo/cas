/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */

package org.jasig.cas.ticket.registry;

import java.util.Collection;

import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.Ticket;

/**
 * Interface for a registry that stores tickets.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface TicketRegistry {

    /**
     * Add a ticket to the registry. Ticket storage is based on the ticket id. If a ticket already exists with that id in the cache, a
     * DuplicateTicketException is thrown.
     * 
     * @param ticket The ticket we wish to add to the cache.
     */
    void addTicket(Ticket ticket);

    /**
     * Retrieve a ticket from the registry. If the ticket retrieved does not match the expected class, an InvalidTicketException is thrown.
     * 
     * @param ticketId the id of the ticket we wish to retrieve
     * @param clazz The expected class of the ticket we wish to retrieve.
     * @return the requested ticket.
     * @throws InvalidTicketException
     */
    Ticket getTicket(String ticketId, Class clazz) throws InvalidTicketException;

    /**
     * Retrieve a ticket from the registry.
     * 
     * @param ticketId the id of the ticket we wish to retrieve
     * @return the requested ticket.
     * @throws InvalidTicketException
     */
    Ticket getTicket(String ticketId);
    
    /**
     * Remove a specific ticket from the registry.
     * 
     * @param ticketId The id of the ticket to delete.
     * @return true if the ticket was removed and false if the ticket did not exist.
     */
    boolean deleteTicket(String ticketId);

    /**
     * Retrieve all tickets from the registry
     * 
     * @return collection of tickets currently stored in the registry. Tickets might or might not be valid i.e. expired.
     */
    Collection getTickets();
}