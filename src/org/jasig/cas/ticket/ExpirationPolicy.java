/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

/**
 * Strategy that determines if the ticket is expired.
 * 
 * @author Scott Battaglia
 * @version $Id$
 * @see org.jasig.cas.ticket.AbstractTicket
 */
public interface ExpirationPolicy {

    /**
     * Method to determine if a Ticket has expired or not, based on the policy.
     * 
     * @param ticket The ticket to check.
     * @return true if the ticket is expired, false otherwise.
     */
    public boolean isExpired(Ticket ticket);
}