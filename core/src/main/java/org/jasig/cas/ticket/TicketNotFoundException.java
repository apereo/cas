/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

/**
 * TicketException to alert that a Ticket was not found.
 * 
 * @author Scott Battaglia
 * @version $Revison$ $Date$
 * @since 3.0
 *
 */
public class TicketNotFoundException extends TicketException {
    
    /** The Unique Serializable ID. */
    private static final long serialVersionUID = 3256723974594508849L;

    /** The code description. */
    private static final String CODE = "NOT_FOUND";
    
    public TicketNotFoundException() {
        super(CODE);
    }
    
    public TicketNotFoundException(final Throwable throwable) {
        super(CODE, throwable);
    }
}
