/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

/**
 * Exception thrown by the TicketFactory if it is given a ticket type that it cannot create a ticket for.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class TicketCreatorNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 4652884415953490001L;

    /**
     * 
     */
    public TicketCreatorNotFoundException() {
        super();
    }

    /**
     * @param message
     */
    public TicketCreatorNotFoundException(final String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public TicketCreatorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public TicketCreatorNotFoundException(Throwable cause) {
        super(cause);
    }
}
