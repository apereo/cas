/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

/**
 * Exception thrown if there is an error creating a ticket.
 * 
 * @author Scott Battaglia
 * @version $Id: TicketCreationException.java,v 1.1 2005/02/15 05:06:38
 * sbattaglia Exp $
 */
public class TicketCreationException extends TicketException {

    private static final long serialVersionUID = 5501212207531289993L;

    /**
     * 
     */
    public TicketCreationException() {
        super();
    }

    /**
     * @param message
     */
    public TicketCreationException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public TicketCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public TicketCreationException(Throwable cause) {
        super(cause);
    }
}