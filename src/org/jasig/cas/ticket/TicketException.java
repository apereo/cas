/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

/**
 * Generic ticket exception. Top of the TicketException heirarchy.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class TicketException extends RuntimeException {

    private static final long serialVersionUID = -6000583436059919480L;

    /**
     *  
     */
    public TicketException() {
        super();
    }

    /**
     * @param message
     */
    public TicketException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public TicketException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public TicketException(Throwable cause) {
        super(cause);
    }
}
