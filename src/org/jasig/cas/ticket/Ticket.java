/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import java.io.Serializable;

/**
 * Interface for the generic concept of a ticket.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface Ticket extends Serializable {

    /**
     * Method to retrieve the id.
     * 
     * @return the id
     */
    String getId();

    /**
     * Determines if the ticket is expired. Most common implementations might collaborate with <i>ExpirationPolicy </i> strategy.
     * 
     * @see org.jasig.cas.ticket.ExpirationPolicy
     */
    boolean isExpired();

    /**
     * Method to retrive the TicketGrantingTicket that granted this ticket
     * 
     * @return the ticket or null if it has no parent
     */
    TicketGrantingTicket getGrantingTicket();

    /**
     * Method to return the number of times a ticket was "used."
     * 
     * @return the number of times the ticket was used.
     */
    int getCountOfUses();

    /**
     * Method to return the last time a ticket was used.
     * 
     * @return the time the ticket was used.
     */
    long getLastTimeUsed();

    /**
     * Increment by one the number of uses.
     */
    void incrementCountOfUses();

    /**
     * Set the last time used to the current time.
     */
    void updateLastTimeUsed();
}