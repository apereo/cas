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

}